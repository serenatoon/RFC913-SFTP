/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/
package server;
import java.io.*; 
import java.net.*; 

class TCPServer {
	private static final int port = 6789;

    private static Socket connectionSocket;
    private static BufferedReader inFromClient;
    private static DataOutputStream outToClient;

    private static boolean loggedIn = false;

    
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

        if (id.equals("GUEST")) { // if guest, don't need pw
            loggedIn = true;
            response = "!" + id + " logged in";
        }

        return response;
    }
} 

