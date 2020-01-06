package cmet.ac.imq.server;

import java.io.InputStream;
import java.io.FileInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * IMQ = Interactive Multiplayer Quiz 
 * Class represents a XMLParser. 
 * 
 * @author Parastoo  
 * @version 2019
 */
public class IMQXmlParser {

	private NodeList questionBank = null;
	private NodeList questionList = null;
	
	private int questionListCount = -1;
	private int questionListIndex = -1;
	public void getList() {
		
	  try {
		//creating a constructor of file class and parsing an XML file
		 InputStream is = new FileInputStream("questionaire.xml");
		//an instance of factory that gives a document builder 
		 DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//an instance of builder to parse the specified xml file
		 DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		 Document doc = dBuilder.parse(is);

		 Element element = doc.getDocumentElement();
		 element.normalize();

		 questionBank = doc.getElementsByTagName("QuestionBank");
		 questionList = doc.getElementsByTagName("Question");
			
		 questionListCount = questionList.getLength();
		 
		 System.out.println("IMQXmlParser:: # of questions:" + questionListCount);
		
			
	  } catch (Exception e) {
		  e.printStackTrace();
	  }

	}
		
	private String getAttributeValue(Node node, String attribute) {
	  String value = node.getAttributes().getNamedItem(attribute).getNodeValue();
	  return value;
	}

	private String getTagValue(Element element, String tag) {
	  NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
	  Node node = nodeList.item(0);
	  return node.getNodeValue();
	}

	private String getTagIdValue(Element element, String tag, String id) {
	  String result = "";
	  NodeList nodeList = element.getElementsByTagName(tag);
	  for (int i=0; i < nodeList.getLength(); i++) {
		Node node = nodeList.item(i);
		if(getAttributeValue(node, "id").equals(id)) {
			
			result = getAttributeValue(node, "id") + " - " + node.getTextContent();
		}
	  }
	  return result;
	}
   
	public int getMaxIMQPlayerCount(){
		String result = "";
		Node node = questionBank.item(0);
		result = getAttributeValue(node, "players");
		return Integer.parseInt(result);
	}
	
	public int getQuestionListCount(){
		return this.questionListCount;
	}
	
	public String getQuestion(int questionIndex){
		String result = "";
		try {	
			Node node = questionList.item(questionIndex);
			if (node.getNodeType() == Node.ELEMENT_NODE) {

				Element currentElement = (Element) node;

				result = getAttributeValue(node, "id") + "-" + getTagValue(currentElement, "text");
				//System.out.println("Question: " + result + "\n");
		   
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getCorrectAnswer(int questionIndex){
		String result = "";
		try {	
			Node node = questionList.item(questionIndex);
			if (node.getNodeType() == Node.ELEMENT_NODE) {

				Element currentElement = (Element) node;

				result = getAttributeValue(node, "answerid");
				//System.out.println("answerid: " + result + "\n");
		   
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getAnswerOption(int questionIndex, String optionIndex){
		String result = "";
		try {	
			Node qNode = questionList.item(questionIndex);
			if (qNode.getNodeType() == Node.ELEMENT_NODE) {

				Element qElement = (Element) qNode;
				
				result = getTagIdValue(qElement, "option", optionIndex);
				//System.out.println("option : " + result +"\n");
		   
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	
}