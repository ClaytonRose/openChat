/*
 * OpenChat Server
 * March 2014
 * Clayton Rose
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static void main(String args[]) {
    	int port = 4451;
    	Server server = new Server(port);
    	server.startServer();
    }
    
    ServerSocket serverSocket;
    Socket clientSocket;
    int port;
    int numConnections = 0;
    ArrayList<ConnectionThread> connectionList;

    //Constructor
    public Server(int port) {
    	this.port = port;
    }
    
    // Starts the server
	public void startServer(){
		//List of connections
		connectionList = new ArrayList<ConnectionThread>();
		
		System.out.println("STARTING OPENCHAT SERVER");
		
		try {
			//Server socket
			serverSocket = new ServerSocket(port);
			
			// Get and display address
			InetAddress inetAddr = InetAddress.getLocalHost();
			System.out.println(" - Address: " + inetAddr);
			
			// Get and display host name
			String hostname = inetAddr.getHostName();
			System.out.println(" - Hostname: " + hostname);
			
		} catch (IOException e) {
			System.out.println("Could not listen on port " + port);
		}
		
		System.out.println("SERVER RUNNING ON PORT: " + port);
		
		// Constantly accept new clients and add them to the list of connections
		while (true) {
			try {
				System.out.println("\nWAITING FOR CLIENTS...");
				
				//Accept client connection
				clientSocket = serverSocket.accept();
				System.out.println("Accepted...");
				numConnections++;
				
				// Start new thread
				ConnectionThread connection = new ConnectionThread(clientSocket, numConnections, this, connectionList);
				connectionList.add(connection);
				System.out.println("New Thread:");
				new Thread(connection).start();
				
			} catch (IOException e) {
				System.out.println(e);
			}
		}

	}
}

// New thread that is started each time a new client connects. 
class ConnectionThread implements Runnable {

	BufferedReader bufferedReader;
	InputStreamReader inputStreamReader;
	Socket clientSocket;
	int id;
	PrintStream printstream;
	Server server;
	ArrayList<ConnectionThread> connectionList;

	//Constructor
	public ConnectionThread(Socket clientSocket, int id, Server server, ArrayList<ConnectionThread> connectionList) {
		this.clientSocket = clientSocket;
		this.id = id;
		this.server = server;
		this.connectionList = connectionList;
	}

	public void run() {
		String message;
		
		try {
			inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
			bufferedReader = new BufferedReader(inputStreamReader);

			System.out.println(" - Connection " + id + " established with: " + clientSocket);

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while (true) {
			try {
				// Get message
				message = bufferedReader.readLine();
				System.out.println("Thread " + id + " Read: " + message);

				if (message != null) {
					System.out.println(message);
					
					// Send message to all clients except the one that sent the message
					for (int i = 0; i < connectionList.size(); i++) {
						//System.out.println("ID: " + id + "ConnectionListSize: " + connectionList.size());
						if (i != id) {
							//System.out.println("ID: " + id + " inside the print");
							try {
								PrintWriter printWriter = new PrintWriter(connectionList.get(i).clientSocket.getOutputStream());
								printWriter.write(message);
								printWriter.flush();

							} catch (IOException e) {
								e.printStackTrace();

							}
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
