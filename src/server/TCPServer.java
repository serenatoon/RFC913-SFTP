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

    
    public static void main(String argv[]) throws Exception
    { 
		String clientSentence;
		String capitalizedSentence;

		String greeting = "+localhost SFTP Service"; // "- localhost Out to lunch"

		// create server socket on port 6789
		ServerSocket welcomeSocket = new ServerSocket(port);

		while(true) {

			System.out.println("waiting for connection.........");
			connectionSocket = welcomeSocket.accept(); // wait for connection
			System.out.println("connection made!");

			inFromClient =
			new BufferedReader(new
				InputStreamReader(connectionSocket.getInputStream())); // to read messages from client

			outToClient =
			new DataOutputStream(connectionSocket.getOutputStream()); // to send messages to client

			// send greeting to client once connection has been made
			sendMessage(greeting);


			clientSentence = inFromClient.readLine();

			capitalizedSentence = clientSentence.toUpperCase() + '\n';

			outToClient.writeBytes(capitalizedSentence);
        } 
    }

    // helper functions

    // send string to client
    private static void sendMessage(String msg) {
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
} 

