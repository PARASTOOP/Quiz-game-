# Paras2
This is a implemention of an interactive application using a client-server architecture simulating a quiz program. 
Assessment Requirements / Tasks
You are required to implement an interactive application using a client-server architecture simulating a quiz program. 
Fig. 1 illustrates a high-level overview of the required system. 
 
Figure 1: Client-server architecture for the interactive multiplayer quiz program
As depicted in Fig. 1, the system consists of software simulating server component and a client component (that can be executed multiple times simulating N number of clients).

 The server software component is designed to listen to a given port and can serve requests from multiple clients. The expected functionality of the server and client components are given below. 

1.1 Server:
-	The server is initiated by establishing a server socket on a given port number. 

o	The port number can be given as a command line argument

-	The server should have the facility to read and parse a given XML game script. The filename should be given as a command line argument (more details on XML game script given below)
-	The number of players accepted for the game (N) is indicated in the game script
-	The server should facilitate up to N client connections. i.e., The server should accept connections from N number of players who will be able to play the game simultaneously. 

o	Hint: Each client connection to the server should treated as a separate thread. 

-	Once a client is connected, the server performs the following:

o	Greets the client with a welcome message 
o	Collects the details of the player (e.g., first name, last name, age etc)

	The server could send messages to the client requesting data (first name, age) and the client can reply with user inputs
-	The server should wait until all N clients are connected before the game starts
-	Once all N clients are connected, the server initiates the quiz program. 

Loop: For each question given in the XML game script:
o	The server sends the question and MCQ answers given in the XML game script to all connected clients 
o	The server gives clients T number of seconds to answer the question. 
o	If the client answers before the time T, the server checks the answer, computes the score and moves on to the next question
o	If the client fails to answer, the server sends a suitable message to the client and moves on to the next question
End Loop

-	This process repeats until all questions given in the XML game script are presented to the client. 
-	Once all the questions are presented and answered by the client, the server computes the total scores for each client and communicates them along with the result (whether the client has won or lost the game)
-	Finally, the server closes the game and disconnects all the clients

1.2 Client:
-	Once the server is initiated, clients can connect to the server using the server’s host IP address and port number. 
o	These should be provided as command line arguments when initiating a client.

-	Once connected to the server, each client gets a greeting message from the server, followed by requests to provide player details (i.e., name, age etc.)

-	Once all clients are connected, the server will start sending the questions with multiple choice answers.
o	Questions are displayed in the terminal standard output.

-	Clients will then answer the questions within the given time window (T seconds).
o	Answers are given as user inputs through the terminal

1.3 XML game script
-	This is an XML file containing key details that control the flow of the game.
-	The XML file contains, number of clients/players that should be registered for the game, questions and their multiple choice answers, the correct answer for the question etc.
