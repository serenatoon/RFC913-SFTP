/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/
package client;
import java.io.*; 
import java.net.*;
import java.nio.file.Files;

class TCPClient {

    private static final String hostAddress = "localhost";
    private static final int port = 6789;

    private static BufferedReader inFromUser;
    private static Socket clientSocket;
    private static DataOutputStream outToServer;
    private static BufferedReader inFromServer;

    private static String currentDir = System.getProperty("user.dir") + File.separator + "res" + File.separator + "client" + File.separator;
    
    public static void main(String argv[]) throws Exception 
    { 
        String userInput;
        String modifiedSentence;

	
        inFromUser = new BufferedReader(new InputStreamReader(System.in)); // reads user input
	
        clientSocket = new Socket(hostAddress, port); // connect to socket that server is listening
	
        outToServer = new DataOutputStream(clientSocket.getOutputStream()); // init DataOutputStream to send msg to server

	    inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // init BufferedReader, reads msg from server

        // connect to server, get initial greeting
        boolean isConnected = false;
        String response;
        while (!isConnected) {
            System.out.println("trying to connect..........");
            response = getResponse();
            System.out.println("Server greeting: " + response);
            if (response.charAt(0) == '+') {
                isConnected = true;
                System.out.println("Connection established!");
            }
        }

         while (isConnected) {
             System.out.println("Enter command...............");
             userInput = inFromUser.readLine();
             String[] input = userInput.split(" "); // split input
             String cmd = input[0].toUpperCase();

             // commands that need client-side logic
             switch (cmd) {
                 case "STOR":
                     sendMessage(userInput);
                     response = getResponse();
                     System.out.println("Response: " + response);

                     // if positive response, send SIZE command
                     if (response.charAt(0) == '+') {
                         File fileToSend = new File(currentDir + input[2]);
                         sendMessage("SIZE " + fileToSend.length());
                         response = getResponse();
                         System.out.println("response: " + response);
                         if (response.charAt(0) == '+') {
                             sendFile(fileToSend);
                             response = getResponse();
                             System.out.println("Server response: " + response);
                         }
                     }
                     else if (response.charAt(0) == '-') {
                         isConnected = false;
                     }
                     break;
                 case "RETR":
                     sendMessage(userInput);
                     response = getResponse();
                     System.out.println("Response: " + response);

                     // if positive response, send SEND or STOP
                     if (response.charAt(0) != '-') {
                         long filesize = Long.parseLong(response);
                         if (hasEnoughDiskSpace(filesize)) {
                             sendMessage("SEND");
                             receiveFile(input[1], filesize);
                         }
                         else {
                             sendMessage("STOP");
                         }
                     }
                     else {
                         isConnected = false;
                     }
                     break;
                 default:
                     // send user command to server
                     sendMessage(userInput);
                     response = getResponse();
                     System.out.println("Server response: " + response);
                     // if closing connection
                     if (userInput.equals("DONE") && response.charAt(0) == '+') {
                         isConnected = false;
                     }
                     else if (response.charAt(0) == '-') {
                         isConnected = false;
                     }
                     break;
             }
         }
//
//        outToServer.writeBytes(sentence + '\n');
//
//        modifiedSentence = inFromServer.readLine();
//
//        System.out.println("FROM SERVER: " + modifiedSentence);
	
        clientSocket.close();
        System.out.println("Connection closed!");
	
    }

    // helper functions

    /*
    read msg from server
    one char at a time
    return when '\0'
    */
    private static String getResponse() {
        String response = "";
        int count = 0;
        char ch = 0;

        while (true) {
            try {
                ch = (char) inFromServer.read(); // read in one char at a time, make sure it is a char
            }
            catch (IOException e){
                e.printStackTrace();
            }
            // check for null termination or length exceeded
            if ((ch == '\0') || (count >= Integer.MAX_VALUE)) {
                break;
            }
            else {
                response += ch;
                count++;
            }
        }
        return response;
    }

    // send command to server
    // append null terminator
    private static void sendMessage(String msg) {
        try {
            outToServer.writeBytes(msg + "\0");
        }
        catch (IOException e) { // socket closed
            try {
                clientSocket.close(); // close this one too
            } catch (IOException e2) {
                // do nothing
            }
        }
    }

    // send file
    private static void sendFile(File file) {
        byte[] bytestream = new byte[(int) file.length()];

        try {
            FileInputStream filestream = new FileInputStream(file);
            BufferedInputStream buf = new BufferedInputStream(filestream);

            int data = 0;
            while ((data = buf.read(bytestream)) >= 0) {
                outToServer.write(bytestream, 0, data);
            }
            buf.close();
            filestream.close();
            outToServer.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // receive file
    private static void receiveFile(String filename, long filesize) {
        File file = new File(currentDir + filename);
        try {
            FileOutputStream filestream = new FileOutputStream(file, false);
            BufferedOutputStream buf = new BufferedOutputStream(filestream);

            for (long i = 0; i < filesize; i++) {
                buf.write(inFromServer.read());
            }
            buf.close();
            filestream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
