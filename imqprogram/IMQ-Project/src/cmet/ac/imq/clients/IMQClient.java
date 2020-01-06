package cmet.ac.imq.clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;


/**
 * IMQ client. 
 * * Represents a client. 
 * Allows user inputs through keyboard and pass them to the server. 
 * Receives responses from the user. 
 * @author Parastoo
 * @version 2019
 */
public class IMQClient implements Runnable {
	// reference variable for client socket
	private Socket 					clientSocket;
	// reference variable to store object IO streams, should be used when working with serialized objects.
	private ObjectOutputStream 		output;
	private ObjectInputStream 		input;
	// boolean variable to store stopclient flag.
	private boolean 				stopClient;

	// reference variable for Thread
	private Thread 					clientReader;
	// variables to store Host IP and port number
	private String 					host;
	private int 					port;
	/**
	 * opens a connection to the server
	 * setup Object IO streams for the socket.
	 * 
	 * @throws IOException
	 */
	public IMQClient(String host, int port) throws IOException {
		this.host = host;
		this.port = port;
		openConnection();
	}
	
	public void openConnection() throws IOException {

		try {
			// Create the sockets and the data streams
			this.clientSocket = new Socket(this.host, this.port);
			this.output = new ObjectOutputStream(this.clientSocket.getOutputStream());
			this.input = new ObjectInputStream(this.clientSocket.getInputStream());

		} catch (IOException ex) {
			try {
				closeAll();
			} catch (Exception exc) {
				System.err.println("IMQClient:: error in opening a connection to: " + this.host + " on port: " + this.port);
			}

			throw ex; // Rethrow the exception.
		}
		// creates a Thread instance and starts the thread.
		this.clientReader = new Thread(this);
		this.stopClient = false;
		this.clientReader.start();

	}
	/**
	 * Handles sending a message to server. In this case, it is a String. 
	 * @param msg
	 * @throws IOException
	 */
	
	public void sendMessageToServer(String msg) throws IOException {
		if (this.clientSocket == null || this.output == null)
			throw new SocketException("socket does not exist");

		this.output.writeObject(msg);
	}
	/**
	 * Handle message from the server. In this case, simply display them. 
	 * @param msg
	 */
	public void handleMessageFromServer(String msg) {
		display(msg);

	}
	
	public void display(String message) {
		System.out.println("> " + message);
	}
	/**
	 * Simply display a String message in the terminal. 
	 * @param message
	 */
	
	private void closeAll() throws IOException {
		try {
			// Close the socket
			if (this.clientSocket != null)
				this.clientSocket.close();
			// Close the output stream
			if (this.output != null)
				this.output.close();
			// Close the input stream
			if (this.input != null)
				this.input.close();
			
		} finally {
			// Set the streams and the sockets to NULL no matter what.
			this.output = null;
			this.input = null;
			this.clientSocket = null;
		}
	}
	/**
	 * handles user inputs from the terminal. 
	 * This should run as a separate thread. In this case, main thread. 
	 * 
	 */
	
	public void runClient() {
		try {
			BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
			String message = null;

			while (true) {
				message = fromConsole.readLine();
				onRecieve(message);
				if(message.equals("end..."))
					break;
			}
			
			System.out.println("IMQClient:: stopping client...");
			this.stopClient = true;
			fromConsole.close();
			closeAll();
		} catch (Exception ex) {
			System.out.println("IMQClient:: unexpected error while reading from console!");
		}

	}
	/**
	 * Can perform any pre-processing or checking of the user input before sending it to server. 
	 * 
	 * @param userResponse
	 */

	public void onRecieve(String userResponse) {

		if (!this.stopClient) {
			try {
				sendMessageToServer(userResponse);
			} catch (IOException e) {
				System.err.println("IMQClient:: error when sending message to server: " + e.toString());

				try {
					closeAll();
				} catch (IOException ex) {
					System.err.println("IMQClient:: error closing the client connections: " + ex.toString());
				}
			}
		}
	}
	/**
	 * The thread that communicates with the server. 
	 * receives a message from the server, passes it to handleMessageFromServer(). 
	 * 
	 */
	
	@Override
	public void run() {

		String msg;
		// Loop waiting for data

		try {
			while (!this.stopClient) {
				// Get data from Server and send it to the handler
				// The thread waits indefinitely at the following
				// statement until something is received from the server
				msg = (String) input.readObject();
				// Concrete subclasses do what they want with the
				// msg by implementing the following method
				handleMessageFromServer(msg);
			}
			
			System.out.println("IMQClient:: client stopped..");
		} catch (Exception exception) {
			if (!this.stopClient) {
				try {
					closeAll();
				} catch (Exception ex) {
					System.err.println("IMQClient:: Failed to client connection...");
				}
			}
		} finally {
			clientReader = null;
		}
		
		System.out.println("IMQClient:: exiting thread...");
	}
	/**
	 * Main() to initiate the client.
	 * @param args
	 */

	public static void main(String[] args) {
		// hardcoded server IP and port number. 
		String ip; // = "127.0.0.1";
		int port; // = 7777;
		
		ip = args[0];
		port = Integer.parseInt(args[1]);
		

		IMQClient player = null;
		// thread to communicate with the server starts here.
		try {
			player = new IMQClient(ip, port);
		} catch (IOException e) {
			System.err.println("IMQClient:: Failed to connect to " + ip + ":" + port);
		}
		// Main thread continues and in this case used to handle user inputs from the terminal.
		player.runClient();
		System.err.println("IMQClient:: End of program.");

	}

}
