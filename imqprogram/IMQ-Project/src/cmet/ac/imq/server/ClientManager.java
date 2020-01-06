package cmet.ac.imq.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.*;


/**
 * Class represents a handler for each Client for the Server. Each client to be treated as a separate thread. 
 * 
 * @author 
 * @version 2019
 */
public class ClientManager extends Thread {
	// reference variable to store client socket
	private Socket 					clientSocket;
	// reference for the Sever
	private AbstractServerComponent	server;
	//// boolean flag to indicate whether to stop the connection
	private boolean					stopConnection;
	// Input Output streams to communicate with the client using Serialized objects
	private ObjectOutputStream 		out;
	private ObjectInputStream 		in;
	// store an incrementing ID for the client.
	private int 					clientID;
	/**
	 * Constructor to be called, when handling multiple clients. Requires a ThreadGroup instance from the Server
	 * 
	 * @param threadgroup
	 * @param socket
	 * @param clientID
	 * @param server
	 */
	private String 					clientFirstName;
	private String 					clientSurName;
	private String 					clientAge;
	private boolean                 endOfGame;
	
	public ClientManager(ThreadGroup threadgroup, Socket socket, int clientID, AbstractServerComponent server) {
		super(threadgroup, (Runnable) null);
		
		this.clientSocket = socket;
		this.server = server;
		this.stopConnection = false;
		this.clientID = clientID;
		
		System.out.println("IMQ-Server::ClientManager:: new client request received, port " + socket.getPort());
		try {
			this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
			this.in = new ObjectInputStream(this.clientSocket.getInputStream());			
		}
		catch(IOException e) {
			// TODO Auto-generated catch block
			System.err.println("IMQ-Server::ClientManager:: error when establishing IO streams on client socket.");
			try {
				closeAll();
			} catch (IOException e1) {
				System.err.println("IMQ-Server::ClientManager:: error when closing connections..." + e1.toString());

			}
		}
		start();	
	}
	/**
	 * Performs the function of sending a message from Server to remote Client#
	 * Uses ObectOutputStream 
	 * 
	 * @param msg
	 * @throws IOException
	 */
	public void sendMessageToClient(String msg) throws IOException {
		if (this.clientSocket == null || this.out == null)
			throw new SocketException("socket does not exist");
		
		this.out.writeObject(msg);
	}
	/**
	 * Closes all connections for the client. 
	 * @throws IOException
	 */
	public void closeAll() throws IOException {
		try {
			// Close the socket
			if (this.clientSocket != null)
				this.clientSocket.close();
			// Close the output stream
			if (this.out != null)
				this.out.close();
			// Close the input stream
			if (this.in != null)
				this.in.close();
		} finally {
			// Set the streams and the sockets to NULL no matter what.
			this.in = null;
			this.in = null;
			this.clientSocket = null;
			
		}
	}
	/**
	 * Receive messages (String) from the client, passes the message to Sever's handleMessagesFromClient() method.
	 * Works in a loop until the boolean flag to stop connection is set to true. 
	 */
	@Override
	public void run() {
		// The message from the client
		String nextQuestion = "";
		String answer = "";
		this.endOfGame = false;
		
		try {
			sendMessageToClient("IMQ-Server:: Welcome");
			sendMessageToClient("IMQ-Server:: What is your first name:");
			this.clientFirstName = (String)this.in.readObject();

			sendMessageToClient("IMQ-Server:: What is your surname:");
			this.clientSurName = (String)this.in.readObject();
			
			sendMessageToClient("IMQ-Server:: How old are you:");
			this.clientAge = (String)this.in.readObject();
			this.server.loginNewIMQPlayer(this);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			this.stopConnection = true;
			System.err.println("IMQ-Server::ClientManager:: error when reading response from client.." + e.toString());
			try {
				closeAll();
			} 
			catch (Exception ex) 
			{
				System.err.println("IMQ-Server::ClientManager:: error when closing the connections.." + ex.toString());
			}
		}
		
		try {
		
			this.clientSocket.setSoTimeout(this.server.getMaxQuestionAnswerTime());
			
			while (!this.stopConnection) {
				// This block waits until it reads a message from the client
				// and then sends it for handling by the server,
				// thread indefinitely waits at the following
				// statement until something is received from the server
				
				if(this.server.areAllClientConnected() && !this.endOfGame) {
					nextQuestion = this.server.getNextQuestion(this);
					//System.out.println(System.currentTimeMillis() + ":: >>" + nextQuestion);

					
					if(!nextQuestion.equals("") && !nextQuestion.equals("end...") && !nextQuestion.equals("wait...")){

						sendMessageToClient(">>  " + nextQuestion);

						String r1 = this.server.getAnswerOption(this, "1");
						String r2 = this.server.getAnswerOption(this, "2");
						String r3 = this.server.getAnswerOption(this, "3");
						String r4 = this.server.getAnswerOption(this, "4");

						sendMessageToClient(">>    " + r1);
						sendMessageToClient(">>    " + r2);
						sendMessageToClient(">>    " + r3);
						sendMessageToClient(">>    " + r4);

						//System.out.println(">>  " + nextQuestion);
						//System.out.println(">>    " + r1);
						//System.out.println(">>    " + r2);
						//System.out.println(">>    " + r3);
						//System.out.println(">>    " + r4);
						
						try {
							answer = (String)this.in.readObject();
							this.server.checkAnswer(this, answer);

						} catch (SocketTimeoutException e) {
							try{
								sendMessageToClient("IMQ-Server:: Time out to answer the question!");
							} catch (IOException e2) {
								e2.printStackTrace();
							} 
						} finally {
							if ( !answer.equals("")){
								this.server.recordIMQPlayerResponse(this, answer);
							}
							answer = "";
						}
					} else if(nextQuestion.equals("")){
						Thread.sleep(10);
					} else if(nextQuestion.equals("wait...")){
						Thread.sleep(10);
					} else if(nextQuestion.equals("end...")){
						this.endOfGame = true;
						sendMessageToClient("IMQ-Server:: End of questions.");
						//System.out.println("IMQ-Server:: End of questions.");
						
						
						
						while(!this.server.AreAllPlayerFinishedtheGame()){
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {	}
						}

						
						int score = this.server.getScore(this);
						String winner = this.server.getWinner();
						sendMessageToClient("IMQ-Server:: Your score is " + score);
						sendMessageToClient("IMQ-Server:: The game winner is/are [" + winner + "]");
						
						sendMessageToClient("end...");
					}
				} else {
					Thread.sleep(10);
				}
			}
			
			System.out.println("IMQ-Server:: Stopping the client connection ID: " + this.clientID);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("IMQ-Server::ClientManager:: Failed to get client response." + e.toString());

			if (!this.stopConnection) {
				try {
					closeAll();
				} 
				catch (Exception ex) 
				{
					System.err.println("IMQ-Server::ClientManager:: error when closing the connections.." + ex.toString());
				}
			}
		}
		finally {
			if(this.stopConnection) {
				try {
					closeAll();
				} catch (IOException e) {
					System.err.println("IMQ-Server::ClientManager:: error when closing the connections.." + e.toString());
					/**
					 * If there is an error, while the connection is not stopped, close all. 
					 */
					}				
			}
		}
	}
	/**
	 * @return a description of the client, including IP address and host name
	 */
	public String toString() {
		return this.clientSocket == null ? null : this.clientSocket.getInetAddress().getHostName() + " ("
				+ this.clientSocket.getInetAddress().getHostAddress() + ")";
	}
////////GETTERS AND SETTERS ////////////
	
	public int getClientID() {
		return this.clientID;
	}

	public String getClientFirstName(){
		return this.clientFirstName;
	}

	public String getClientSurName(){
		return this.clientSurName;
	}

	public String getClientAge(){
		return this.clientAge;
	}
	
}
