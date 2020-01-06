package cmet.ac.imq.server;

import java.lang.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * IMQ = Interactive Multiplayer Quiz 
 * Class represents a Server component. 
 * 
 * @author Parastoo  
 * @version 20.11.2019
 */
public class IMQServer extends AbstractServerComponent implements Runnable {
	// reference variable for server socket. 
	private ServerSocket 			serverSocket;
	// reference variable for ClientHandler for the server. 
	private ClientManager 			clientHandler;
	// boolean flag to indicate the server stop.
	private boolean 				stopServer;
	// reference variabale for the Thread
	private Thread 					serverListenerThread;
	// reference variable for ThreadGroup when handling multiple clients
	private ThreadGroup 			clientThreadGroup;

	// variable to store server's port number
	private int port;

	/**
	 * Constructor.
	 * 
	 */
	private IMQXmlParser imqXmlParser;
	private IMQPlayer[] IMQPlayers;

	private int connectedIMQPlayerCount = 0;
	
	private long currentQuestionPublishedCount = 0;
	private long currentQuestionPublishedTime = 0;
	//count the player
	private int MaxIMQPlayerCount = 0;
	//set a 10000 ms time to answare the question
	private int MAX_QUESTION_RESPONSE_TIME  = 10000;
		

	public IMQServer() {
		
		this.stopServer = false;
		
		/**
		 * Initializes the ThreadGroup. 
		 * Use of a ThreadGroup is easier when handling multiple clients. 
		 */
		
		this.clientThreadGroup = new ThreadGroup("ClientManager threads");

	}
	/**
	 * Initializes the server. Takes port number, creates a new serversocket instance. 
	 * Starts the server's listening thread. 
	 * @param port
	 * @throws IOException
	 */
	
	public void initializeServer(int port) throws IOException {

		
		imqXmlParser = new IMQXmlParser();
		imqXmlParser.getList();

		MaxIMQPlayerCount = imqXmlParser.getMaxIMQPlayerCount();
		System.out.println("IMQ-Server:: Number of player " + MaxIMQPlayerCount);
		IMQPlayers = new IMQPlayer[MaxIMQPlayerCount];

		currentQuestionPublishedTime = System.currentTimeMillis();
		
		this.port = port;
		if (serverSocket == null) {
			serverSocket = new ServerSocket(port);
		}

		stopServer = false;
		serverListenerThread = new Thread(this);
		serverListenerThread.start();
		try{
			serverListenerThread.join();
		} catch (InterruptedException e) {
			System.err.println("IMQ-Server:: Failed to terminate the IMQ-server thread.");
		}
	}
	/**
	 * handles messages from each client. In this case messages are simply displayed. 
	 * This is a shared resource among all client threads, so it has to be synchronized.
	 * 
	 * 
	 * @param msg
	 * @param client
	 */
	

	public synchronized void loginNewIMQPlayer(ClientManager client) {
		
		IMQPlayers[connectedIMQPlayerCount] = new IMQPlayer(client.getClientID(), client.getClientFirstName(), client.getClientSurName(), client.getClientAge());		
		
		String response = "IMQ-Server::Succeed to Login:" + client.getClientFirstName() + " " + client.getClientSurName();	
		display(response);

		sendMessageToClient(client, response);

		connectedIMQPlayerCount++;
	}


	public synchronized void recordIMQPlayerResponse(ClientManager client, String msg) {
		
        //String formattedMessage = String.format("IMQ-Client [%d]:: %s", client.getClientID(), msg); 
		
        //display(formattedMessage);
		 //prepare a response for the client. 
		String response = "IMQ-Server:: Your response is:" + msg.toUpperCase();					
		sendMessageToClient(client, response);
		
	}
	
	public synchronized boolean areAllClientConnected() {
		boolean result = false;
		if (connectedIMQPlayerCount == MaxIMQPlayerCount){
			result = true;
		}
		return result;
	}
	
	public synchronized int getMaxQuestionAnswerTime(){
		return MAX_QUESTION_RESPONSE_TIME;
	}

	public synchronized String getNextQuestion(ClientManager client) {
		String qi = "";
	
		if(getIMQPlayer(client).getCurrentQuestionIndex() + 1 < imqXmlParser.getQuestionListCount()) {
			getIMQPlayer(client).incQuestionIndex();
			qi = imqXmlParser.getQuestion(getIMQPlayer(client).getCurrentQuestionIndex());
		} else {
			getIMQPlayer(client).setCompletedTheGame();
			qi = "end...";
		}			
		
		System.out.println("To Client[" + client.getClientID() + "]:" + qi);
		return qi;
	}

	public synchronized String getAnswerOption(ClientManager client, String rIndex) {
		String ri = "";
		ri = imqXmlParser.getAnswerOption(getIMQPlayer(client).getCurrentQuestionIndex(), rIndex);
		return ri;
	}

	public synchronized void checkAnswer(ClientManager client, String clientAnswer) {
		String answer = "";
		answer = imqXmlParser.getCorrectAnswer(getIMQPlayer(client).getCurrentQuestionIndex());
		if(answer.equals(clientAnswer)){
			
			getIMQPlayer(client).incScore();
			
		}
	}

	public synchronized int getScore(ClientManager client) {
		int result = 0;

		result = getIMQPlayer(client).getScore();

		return result;
	}

	public synchronized String getWinner() {
		String winner = "";
		int topScore = 1; //The min player's score is one;
				
		for(int i = 0; i < IMQPlayers.length; i++){
			if(IMQPlayers[i].getScore() >= topScore) {
				topScore = IMQPlayers[i].getScore();
				
				if (winner.equals("")) {
					winner = IMQPlayers[i].getName();
				} else {
					winner = winner + ", "+ IMQPlayers[i].getName();
				}
			}
		}
		if (winner.equals("")) {
			winner = "No One.";
		}
		return winner;
	}

	public synchronized boolean AreAllPlayerFinishedtheGame(){
		boolean result = true;
		for(int i = 0; i < IMQPlayers.length; i++){
			if(!IMQPlayers[i].isCompletedTheGame()) {
				result = false;
			}
		}
		return result;
	}
	
	public IMQPlayer getIMQPlayer(ClientManager client){

		IMQPlayer player = null;
		for(int i = 0; i < IMQPlayers.length; i++){
			if(IMQPlayers[i].getId() == client.getClientID()){
				player = IMQPlayers[i];
			}
		}
		return player;
	}

	public void display(String message) {
		System.out.println(">> " + message);
	}
	
	
	public void handleUserInput(String msg) {
		
		if(msg.equals(new String("over"))) {
			this.stopServer = true;
			close();
			return;
		}
		
		Thread[] clientThreadList = getClientConnections();
		for (int i = 0; i < clientThreadList.length; i++) {
			try {
				((ClientManager)clientThreadList[i]).sendMessageToClient(msg);
			}
			// Ignore all exceptions when closing clients.
			catch (Exception ex) {
				
			}
		}
	}
	
	public synchronized void sendMessageToClient(ClientManager client, String msg) {
		try {
			client.sendMessageToClient(msg);
		} catch (IOException e) {
			System.err.println("IMQ-Server:: Server-to-client message sending failed...");
		}
	}
	
	/**
	 * 
	 * @return list of Thread[] pertaining to the clients connected to the server
	 */
	
	
	public Thread[] getClientConnections() {
		
		Thread[] clientThreadList = new Thread[clientThreadGroup.activeCount()];
		clientThreadGroup.enumerate(clientThreadList);

		return clientThreadList;
	}
	/**
	 * Close the server and associated connections. 
	 */
	public void close() {
		
		if (this.serverSocket == null)
			return;

		try {
			this.stopServer = true;
			this.serverSocket.close();

		} catch (IOException e) {
			System.err.println("IMQ-Server:: Error in closing server connection...");
		} finally {
			// Close the client sockets of the already connected clients
			Thread[] clientThreadList = getClientConnections();
			for (int i = 0; i < clientThreadList.length; i++) {
				try {
					((ClientManager) clientThreadList[i]).closeAll();
				}	// Ignore all exceptions when closing clients.
				catch (Exception ex) {
					
				}
			}
			this.serverSocket = null;
			
		}

	}
	/**
	 * Represents the thread that listens to the port, and creates client connections. 
	 * Here, each connection is treated as a separate thread, and each client is associated with the ThreadGroup. 
	 * 
	 */
	
	@Override
	public void run() {
		
		System.out.println("IMQ-Server:: Start to listen on port: " + port);
		// increments when a client connects. 
		int clientCount = 0;
		// loops until stopserver flag is set to true. 
		while (!this.stopServer) {

			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e1) {
				System.err.println("IMQ-Server:: Failed to accept client connections on port " + port);
			}

			ClientManager cm = new ClientManager(this.clientThreadGroup, clientSocket, clientCount, this);
			// new ClientManager(clientSocket, this);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println("IMQ-Server:: server listner thread interruped.");
			}

			clientCount++;

		}		
	}
	/**
	 * 
	 * @return returns the status of the server; i.e., whether the server has stopped.
	 */
	public boolean getServerStatus() {
		return this.stopServer;
	}

	/**
	 * Main() to start the IMQServer. 
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		IMQServer server = new IMQServer();
		// port number to listen
		int port = Integer.parseInt(args[0]); //7778;

		try {
			server.initializeServer(port);

		} catch (IOException e) {
			System.err.println("IMQ-Server:: Error in initializing the server on port " + port);
		}
		
              

		System.err.println("IMQ-Server:: End of program.");

	}
}