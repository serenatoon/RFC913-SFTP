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
                }
                else {
                    serverResponse = "-File doesn't exist";
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

}