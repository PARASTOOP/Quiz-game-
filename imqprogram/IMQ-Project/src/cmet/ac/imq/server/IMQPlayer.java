package cmet.ac.imq.server;
/**
 * IMQ = Interactive Multiplayer Quiz 
 * Class represents a IMQPlayer. 
 * 
 * @author Parastoo  
 * @version 2019
 */
public class IMQPlayer{

	private int Id;
	private String FirstName;
	private String SurName;
	private String Age;
	
	private int currentQuestionIndex;
	private boolean completedTheGame;
	private int Score;
	
	IMQPlayer(int id, String firstName, String surName, String age){
		
		Id = id;
		FirstName = firstName;
		SurName = surName;
		Age  = age;
		currentQuestionIndex = -1;
		Score = 0;
		completedTheGame = false;
	}
	
	public int getId(){
		return Id;
	}

	public String getName(){
		return this.FirstName + "-" + this.SurName;
	}
	
	public int getCurrentQuestionIndex(){
		return currentQuestionIndex;
	}

	public void incQuestionIndex(){
		currentQuestionIndex++;
	}

	public int getScore(){
		return Score;
	}
	
	public void incScore(){
		Score++;
	}
	
	public void setCompletedTheGame(){
		completedTheGame = true;
	}

	public boolean isCompletedTheGame(){
		return completedTheGame;
	}
}

