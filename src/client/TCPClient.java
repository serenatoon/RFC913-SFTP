/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/
package client;
import java.io.*; 
import java.net.*; 
class TCPClient {

    private static final String hostAddress = "localhost";
    private static final int port = 6789;

    private static BufferedReader inFromUser;
    private static Socket clientSocket;
    private static DataOutputStream outToServer;
    private static BufferedReader inFromServer;
    
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

         while (true) {
             System.out.println("Enter command...............");
             userInput = inFromUser.readLine().toUpperCase();
             // send user command to server
             sendMessage(userInput);
             response = getResponse();
             System.out.println(response);

             if (userInput.equals("DONE") && response.charAt(0) == '+') {
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
} 
