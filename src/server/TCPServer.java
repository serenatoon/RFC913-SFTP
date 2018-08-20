/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/
package server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

class TCPServer {
	private static final int port = 6789;

    private static Socket connectionSocket;
    private static BufferedReader inFromClient;
    private static DataOutputStream outToClient;

    private static String jsonPath = System.getProperty("user.dir") + File.separator + "res" + File.separator + "users.json";
    private static String serverDir = System.getProperty("user.dir") + File.separator + "res" + File.separator + "server" + File.separator;
    private static String currentDir = serverDir;
    private static String cdirSaved = null;
    private static String toRenamePath = null;
    private static String toRenameFilename = null;
    private static boolean isRetrieving = false;
    private static String toRetrieve = null;
    private static String toStore = null;
    private static String storeMode = null;
    private static boolean isStoring = false;
    private static boolean overwrite = false;
    private static long storeSize = 0;
    private static String toStoreFilename = null;

    private static boolean loggedIn = false;
    private static String currentUser = null;
    private static String currentAcc = null;
    private static String currentPassword = null;
    private static boolean userAccepted = false;
    private static boolean accAccepted = false;
    private static boolean passwordAccepted = false;

    private static char type = 0;
    
    public static void main(String argv[]) throws Exception
    { 
		String clientInput;
		String capitalizedSentence;
		String serverResponse = "";

		String greeting = "+localhost SFTP Service"; // "-localhost Out to lunch"

		// create server socket on port 6789
		ServerSocket welcomeSocket = new ServerSocket(port);

		// establish connection
        System.out.println("waiting for connection.........");
        connectionSocket = welcomeSocket.accept(); // wait for connection
        System.out.println("connection made!");

        inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); // to read messages from client
        outToClient = new DataOutputStream(connectionSocket.getOutputStream()); // to send messages to client

        // send greeting to client once connection has been made
        sendResponse(greeting);

		while(true) {

            // stop if connection closed
            if (connectionSocket.isClosed()) {
                System.out.println("Connection closed!");
                return;
            }

			clientInput = getMessage(); // get input
            System.out.println("Client input: " + clientInput);
            String[] input = clientInput.split(" "); // split input
            String cmd = input[0].toUpperCase();
            System.out.println("input0: " + cmd);

            // DONE COMMAND
            if (cmd.equals("DONE")) {
                if (input.length == 1) {
                    serverResponse = "+localhost closing connection";
                    System.out.println("Connection closed!");
                    sendResponse(serverResponse);
                    return;
                }
                else {
                    serverResponse = "-Too many arguments";
                }
            }
            // USER COMMAND
            else if (cmd.equals("USER")) {
                if (input.length == 2) {
                    // log in
                    serverResponse = login(input[1]);
                }
                else if (input.length < 2) {
                    serverResponse = "-Too few arguments";
                }
                else if (input.length > 2) {
                    serverResponse = "-Too many arguments";
                }
            }
            // ACCT COMMAND
            else if (cmd.equals("ACCT")) {
                if (input.length == 2) {
                    // check if account is valid
                    serverResponse = tryAcct(input[1]);
                }
                else if (input.length < 2) {
                    serverResponse = "-Too few arguments";
                }
                else if (input.length > 2) {
                    serverResponse = "-Too many arguments";
                }
            }
            // PASS COMMAND
            else if (cmd.equals("PASS")) {
                if (input.length == 2) {
                    // check if pw is correct
                    serverResponse = checkPassword(input[1]);
                }
                else if (input.length < 2) {
                    serverResponse = "-Too few arguments";
                }
                else if (input.length > 2) {
                    serverResponse = "-Too many arguments";
                }
            }
            // TYPE command
            else if (cmd.equals("TYPE")) {
                if (!loggedIn) {
                    serverResponse = "-Not logged in";
                }
                else {
                    if (input.length == 2) {
                        switch (input[1].toUpperCase()) {
                            case "A":
                                serverResponse = "+Using ASCII mode";
                                type = 'A';
                                break;
                            case "B":
                                serverResponse = "+Using Binary mode";
                                type = 'B';
                                break;
                            case "C":
                                serverResponse = "+Using Continuous mode";
                                type = 'C';
                                break;
                            default:
                                serverResponse = "-Type not valid";
                                type = 0;
                                break;
                        }
                    }
                    else if (input.length < 2) {
                        serverResponse = "-Too few arguments";
                    }
                    else if (input.length > 2) {
                        serverResponse = "-Too many arguments";
                    }
                }
            }
            // CDIR command
            else if (cmd.equals("CDIR")) {
                if (input.length < 2) {
                    serverResponse = "-Too few arguments";
                }
                else if (input.length > 2) {
                    serverResponse = "-Too many arguments";
                }
                else {
                    serverResponse = changeDir(input[1]);
                }
            }
            // LIST command
            else if (cmd.equals("LIST")) {
                String listPath = null;
                if (input.length == 2) {
                    listPath = currentDir;
                }
                else if (input.length == 3) {
                    listPath = currentDir + input[2];
                }
                else if (input.length < 2) {
                    serverResponse = "-Too few arguments";
                }
                else if (input.length > 3) {
                    serverResponse = "-Too many arguments";
                }

                if (listPath != null) {
                    switch (input[1].toUpperCase()) {
                        case "F":
                            if (dirExists(listPath)) {
                                serverResponse = getFormattedListing(listPath);
                            }
                            else {
                                serverResponse = "-Could not get directory listing because directory does not exist";
                            }
                            break;
                        case "V":
                            if (dirExists(listPath)) {
                                serverResponse = getVerboseListing(listPath);
                            }
                            else {
                                serverResponse = "-Could not get directory listing because directory does not exist";
                            }
                            break;
                        default:
                            serverResponse = "-Invalid LIST query";
                            break;
                    }
                }
                else {
                    serverResponse = "-listPath is null??";
                }
            }
            // NAME command
            else if (cmd.equals("NAME")) {
                String path = currentDir + input[1];
                if (dirExists(path)) {
                    serverResponse = "+File exists";
                    toRenamePath = path;
                    toRenameFilename = input[1];
                }
                else {
                    serverResponse = "-Can't find " + input[1];
                }
            }
            // TOBE command
            else if (cmd.equals("TOBE")) {
                if (toRenamePath == null) {
                    serverResponse = "-File wasn't renamed because you never issued NAME command";
                }
                else {
                    serverResponse = renameFile(input[1]);
                }
            }
            // RETR command
            else if (cmd.equals("RETR")) {
                String path = currentDir + input[1];
                if (dirExists(path)) {
                    serverResponse = String.valueOf(getFileSize(path));
                    toRetrieve = path;
                }
                else {
                    serverResponse = "-File doesn't exist";
                    toRetrieve = null;
                }
            }
            // STOP command
            else if (cmd.equals("STOP")) {
                toRetrieve = null;
                serverResponse = "+ok, RETR aborted";
            }
            // SEND command
            else if (cmd.equals("SEND")) {
                sendFile(toRetrieve);
                toRetrieve = null;
            }
            // STOR command
            else if (cmd.equals("STOR")) {
                String path = currentDir + input[2];
                String mode = input[1].toUpperCase();
                boolean isValidMode = false;
                if (input.length == 3) {
                    switch (mode) {
                        case "NEW":
                            if (dirExists(path)) {
                                serverResponse = "-File exists, but system doesn't support generations";
                            } else {
                                serverResponse = "+File does not exist, will create new file";
                            }
                            isValidMode = true;
                            break;
                        case "OLD":
                            if (dirExists(path)) {
                                serverResponse = "+Will write over old file";
                                overwrite = true;
                            } else {
                                serverResponse = "+Will create new file";
                            }
                            isValidMode = true;
                            break;
                        case "APP":
                            if (dirExists(path)) {
                                serverResponse = "+Will append to file";
                            } else {
                                serverResponse = "+Will create file";
                            }
                            isValidMode = true;
                            break;
                        default:
                            serverResponse = "-Invalid use of STOR command";
                            isValidMode = false;
                            break;
                    }

                    // assign global path and mode; prepare for future commands
                    if (isValidMode) {
                        toStore = path;
                        toStoreFilename = input[2];
                    } else {
                        toStore = null;
                    }
                }
                else {
                    serverResponse = "-Invalid use of STOR command";
                }
            }
            // SIZE command
            else if (cmd.equals("SIZE")) {
                try {
                    long filesize = Long.parseLong(input[1]); // might throw NumberFormatException if too big

                    if (hasEnoughDiskSpace(filesize)) {
                        System.out.println("Enough space");
                        isStoring = true;
                        storeSize = filesize;
                        sendResponse("+ok, waiting for file");
                        System.out.println("Storing file....");
                        serverResponse = storeFile();
                    }
                    else {
                        System.out.println("Not enough space");
                        serverResponse = "-Not enough room, don't send it";
                        isStoring = false;
                        storeSize = 0;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    serverResponse = "-Not enough room, don't send it";
                    storeSize = 0;
                }
            }
            
            // send response back to client
            sendResponse(serverResponse);

//			capitalizedSentence = clientInput.toUpperCase() + '\n';
//
//			outToClient.writeBytes(capitalizedSentence);
        } 
    }

    // helper functions

    // send string to client
    private static void sendResponse(String msg) {
        try {
            outToClient.writeBytes(msg + "\0");
        }
        catch (IOException e) { // client socket closed
            try {
                connectionSocket.close();
            }
            catch (IOException e2) {
                // do nothing
            }
        }
    }

    private static String getMessage() {
        String msg = "";
        char ch = 0;
        int count = 0;

        while (true) {
            try {
                ch = (char) inFromClient.read(); // read in one char at a time
            }
            catch (Exception e) {
                e.printStackTrace(); // connection closed
                try {
                    connectionSocket.close(); // close this connection too
                }
                catch (IOException e1) { // do nothing
                }
            }
            // check for null termination or exceed length
            if ((ch == '\0') || count >= Integer.MAX_VALUE) {
                break;
            }
            else {
                msg += ch;
                count++;
            }
        }
        return msg;
    }

    // log in
    private static String login(String userid) {
        String response = "";
        String id = userid.toUpperCase();

        if (loggedIn && id.equals(currentUser)) {
            response = "!" + id + " already logged in";
        }
        else if (id.equals("ADMIN")) { // if admin, don't need pw
            loggedIn = true;
            currentUser = id;
            response = "!" + id + " logged in";
        }
        else {
            try {
                // json
                FileReader reader = new FileReader(jsonPath);
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(reader);
                JSONObject jsonObject = new JSONObject(obj.toString());
                JSONArray users = jsonObject.getJSONArray("users");
                JSONObject jsonUser;

                // if not found
                response = "-Invalid user-id, try again";
                // iterate through userlist
                for (int i = 0; i < users.length(); i++) {
                    jsonUser = users.getJSONObject(i);
                    if (id.equals(jsonUser.getString("user").toUpperCase())) { // user found in json
                        currentUser = id;
                        currentAcc = jsonUser.getString("acc");
                        currentPassword = jsonUser.getString("pw");
                        userAccepted = true;
                        return "+User-id valid, send account and password";
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return response;
    }

    // ACCT command
    // RFC 913 protocol doesn't explicitly mention that user had to be found first?
    private static String tryAcct(String acct) {
        String response = "";

        try {
            // json
            FileReader reader = new FileReader(jsonPath);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(reader);
            JSONObject jsonObject = new JSONObject(obj.toString());
            JSONArray users = jsonObject.getJSONArray("users");
            JSONObject jsonUser;
            System.out.println("looking for: " + acct);
            response = "-Invalid account, try again";
            // iterate through userlist
            for (int i = 0; i < users.length(); i++) {
                jsonUser = users.getJSONObject(i);
                if (acct.equalsIgnoreCase(jsonUser.getString("acc"))) { // user found in json
                    currentAcc = acct;
                    currentUser = jsonUser.getString("user");
                    currentPassword = jsonUser.getString("pw");
                    accAccepted = true;
                    userAccepted = true; // ?
                    if (loggedIn) {
                        if (cdirSaved == null) { // check that we didn't try execute cdir before
                            return "! Account valid, logged-in";
                        }
                        else {
                            return changeDir(cdirSaved);
                        }
                    }
                    else {
                        return "+Account valid, send password";
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    // checks if password is in system
    private static String checkPassword(String pw) {
        if (pw.equals(currentPassword)) {
            if (accAccepted) {
                if (cdirSaved == null) { // check that we didn't try to run cdir command before we were logged in
                    loggedIn = true;
                    passwordAccepted = true;
                    return "! Logged in";
                }
                else { // execute cdir
                    loggedIn = true;
                    passwordAccepted = true;
                    return changeDir(cdirSaved);
                }
            }
            else {
                return "+Send account";
            }
        }
        else {
            return "-Wrong password, try again";
        }
    }

    // CDIR command
    // attempts to change dir, checks if logged in
    private static String changeDir(String dir) {
        String path = currentDir + dir;
        try {
            if (new File(path).exists()) {
                if (!loggedIn) { // save cdir command
                    cdirSaved = dir;
                    return "+directory ok, send account/password";
                }
                else { // currently logged in, so can change dir
                    currentDir = path;
                    cdirSaved = null;
                    return "!Changed working dir to " + dir;
                }
            }
            else {
                return "-Can't connect to working directory because: Directory does not exist";
            }
        }
        catch (Exception e) {
            return "-Cannot connect to working directory because: " + e.toString();
        }
    }

    // get formatted directory listing
    private static String getFormattedListing(String path) {
        String response = "";
        System.out.println("path: " + path);
        try {
            File dir = new File(path);
            response += "+" + path + System.getProperty("line.separator"); // first response is current dir
            System.out.println(response);
            File[] fileList = dir.listFiles();
            for (int i =0; i < fileList.length; i++) {
                if (fileList[i].isFile()) {
                    response += fileList[i].getName() + System.getProperty("line.separator");
                }
            }
            return response + '\0';
        }
        catch (Exception e) {
            return "-Could not get formatted listing because: " + e.toString();
        }
    }

    // get vecbose directory listing
    private static String getVerboseListing(String path) {
        String response = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm");
        try {
            File dir = new File(path);
            response += "+" + path + System.getProperty("line.separator"); // first response is current dir
            System.out.println(response);
            File[] fileList = dir.listFiles();
            for (int i =0; i < fileList.length; i++) {
                if (fileList[i].isFile()) {
                    long lastModified = fileList[i].lastModified();
                    String dateModified = dateFormat.format(new Date(lastModified));
                    String filesize = String.valueOf(fileList[i].length());
                    String owner = null;
                    // get file owner
                    try {
                        FileOwnerAttributeView attribute = Files.getFileAttributeView(fileList[i].toPath(), FileOwnerAttributeView.class);
                        owner = attribute.getOwner().getName();
                    }
                    catch (IOException e) {
                        e.printStackTrace();;
                    }
                    response += fileList[i].getName() + "    " + dateModified + "    " + filesize + "    " + owner + System.getProperty("line.separator");
                }
            }
            return response + '\0';
        }
        catch (Exception e) {
            return "-Could not get formatted listing because: " + e.toString();
        }
    }

    private static String renameFile(String newFileName) {
        File fileToRename = new File(toRenamePath);
        File newFile = new File(currentDir + newFileName);
        try {
            if (fileToRename.renameTo(newFile)) {
                String response = "+" + toRenameFilename + " renamed to " + newFileName;
                toRenameFilename = null;
                toRenamePath = null;
                return response;
            }
            else {
                return "-File could not be renamed!";
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return "-File wasn't renamed because " + e.toString();
        }
    }

    // send file
    private static boolean sendFile(String dir) {
        System.out.println("Sending " + dir);
        File file = new File(dir);
        byte[] bytestream = new byte[(int) file.length()];

        try {
            FileInputStream filestream = new FileInputStream(file);
            BufferedInputStream buf = new BufferedInputStream(filestream);

            int data = 0;
            // read file, as long as there is data, send it
            while ((data = buf.read(bytestream)) >= 0) {
                outToClient.write(bytestream, 0, data); // send
            }
            buf.close();
            filestream.close();
            outToClient.flush();

            return true; // successfully sent
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // store file
    private static String storeFile() {
        System.out.println("Storing file at: " + toStore);
        try {
            FileOutputStream filestream = new FileOutputStream(toStore, overwrite);
            BufferedOutputStream buf = new BufferedOutputStream(filestream);

            for (int i = 0; i < storeSize; i++) {
                buf.write(inFromClient.read()); // read bytes from client
            }
            buf.close();
            filestream.close();

            return "+Saved " + toStoreFilename;
        }
        catch (Exception e) {
            e.printStackTrace();
            return "-Couldn't save because " + e.toString();
        }
    }

    // check dir exists
    private static boolean dirExists(String dir) {
        String path = dir;
        if (new File(path).exists()) {
            return true;
        }
        else { return false; }
    }

    // get filesize in bytes
    private static long getFileSize(String dir) {
        String path = dir;
        return new File(dir).length();
    }

    // checks whether or not there is enough room for the file on disk
    private static boolean hasEnoughDiskSpace(long filesize) {
        File currDir = new File(currentDir);
        try {
            long space = Files.getFileStore(currDir.toPath().toRealPath()).getUsableSpace();
            System.out.println("Free space: " + space);
            return (space > filesize);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}