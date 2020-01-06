/**
 * 
 */
package cmet.ac.imq.server;

/**
 * @author Parastoo
 *
 */
public abstract class AbstractServerComponent {

	public abstract void loginNewIMQPlayer(ClientManager client);
	public abstract void recordIMQPlayerResponse(ClientManager client, String msg);
	public abstract void sendMessageToClient(ClientManager client, String msg);
	public abstract int getMaxQuestionAnswerTime();
	public abstract boolean areAllClientConnected();
	public abstract String getNextQuestion(ClientManager client);
	public abstract String getAnswerOption(ClientManager client, String rIndex);
	public abstract void checkAnswer(ClientManager client, String clientAnswer);
	public abstract int getScore(ClientManager client);
	public abstract boolean AreAllPlayerFinishedtheGame();
	public abstract String getWinner();


	
}
