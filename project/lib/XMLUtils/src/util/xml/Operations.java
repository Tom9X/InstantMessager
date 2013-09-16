package util.xml;

import java.text.*;
import java.util.*;
import java.io.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Contains methods to send/receive messages, authorization, active users list, history by XML
 */
public class Operations {

	public static void sendMessage(Message message, DataOutputStream output) throws ParserConfigurationException,
			TransformerConfigurationException, ParseException, TransformerException, SAXException, IOException {
		
		Schema schema = getSchema();
		
		Document document = getDocumentBuilder(schema).newDocument();
			
		Element root = document.createElement("document");
		document.appendChild(root);
		Element messageElement = document.createElement("message");
		root.appendChild(messageElement);
		Element time = document.createElement("time");
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		time.appendChild(document.createTextNode(dateFormat.format(new Date())));
		messageElement.appendChild(time);	
		Element to = document.createElement("to");
		to.appendChild(document.createTextNode(message.getToUser()));
		messageElement.appendChild(to);
		Element from = document.createElement("from");
		from.appendChild(document.createTextNode(message.getFromUser()));
		messageElement.appendChild(from);
		Element text = document.createElement("text");
		text.appendChild(document.createTextNode(message.getMessage()));
		messageElement.appendChild(text);

		DOMSource source = validate(document, schema);
		
		String send = DOMtoString(source);
		output.writeUTF(send);
		output.flush();
	}
	
	public static Message receiveMessage(DataInputStream input) throws ParserConfigurationException,
			IOException, ParseException, SAXException {
		
		String receive = input.readUTF();
	
		Schema schema = getSchema();
		
		Document document = getDocumentBuilder(schema).parse(new InputSource(new StringReader(receive))); 
			
		Element root = document.getDocumentElement();
		Element messageElement = (Element) root.getFirstChild();
		NodeList childs = messageElement.getChildNodes();
		Element timeElement = (Element) childs.item(0);
		String time = timeElement.getFirstChild().getNodeValue();
		Element fromElement = (Element) childs.item(1);
		String from = fromElement.getFirstChild().getNodeValue();
		Element toElement = (Element) childs.item(2);
		String to = toElement.getFirstChild().getNodeValue();
		Element textElement = (Element) childs.item(3);
		String text = textElement.getFirstChild().getNodeValue();

		return new Message(DateFormat.getDateInstance().parse(time), from, to, text);
	}
	
	public static void receiveUserNamesList(List<String> userNames, DataInputStream input) throws ParserConfigurationException,
			SAXException, IOException {
		
		String receive = input.readUTF();
	
		Schema schema = getSchema();
		
		Document document = getDocumentBuilder(schema).parse(new InputSource(new StringReader(receive)));
		
		Element root = document.getDocumentElement();		
		Element userList = (Element) root.getFirstChild();
		Element amount = (Element) userList.getFirstChild();
		int count = Integer.valueOf(amount.getFirstChild().getNodeValue());
		userNames.clear();
		for (int i = 1; i < count; i++) {
			Element element = (Element) userList.getNextSibling();
			String userName = element.getFirstChild().getNodeValue();
			userNames.add(userName);
		}	
	}
	
	public static void sendUserNamesList(List<String> userNames, DataOutputStream output) throws ParserConfigurationException,
			TransformerConfigurationException, TransformerException, SAXException, IOException {
		
		Schema schema = getSchema();
		
		Document document = getDocumentBuilder(schema).newDocument();
		
		Element root = document.createElement("document");
		document.appendChild(root);
		Element userList = document.createElement("userList");
		root.appendChild(userList);
		Element amount = document.createElement("amount");
		amount.appendChild(document.createTextNode(String.valueOf(userNames.size())));
		userList.appendChild(amount);
		for (int i = 0; i < userNames.size(); i++) {
			Element user = document.createElement("user");
			user.appendChild(document.createTextNode(userNames.get(i)));
			userList.appendChild(user);
		}
		
		DOMSource source = validate(document, schema);
		
		String send = DOMtoString(source);
		output.writeUTF(send);
		output.flush();
	}
	
	public static void sendAuthorize(String userName, DataOutputStream output) throws ParserConfigurationException, SAXException, IOException, 
			TransformerConfigurationException, TransformerException {
			
		Schema schema = getSchema();
		
		Document document = getDocumentBuilder(schema).newDocument();
		
		Element root = document.createElement("document");
		document.appendChild(root);
		Element authorize = document.createElement("authorize");
		root.appendChild(authorize);
		Element user = document.createElement("userName");
		user.appendChild(document.createTextNode(userName));
		authorize.appendChild(user);
		
		DOMSource source = validate(document, schema);
		
		String send = DOMtoString(source);
		output.writeUTF(send);
		output.flush();
	}
	
	public static String receiveAuthorize(DataInputStream input) throws SAXException, IOException, ParserConfigurationException {
		String receive = input.readUTF();
	
		Schema schema = getSchema();
		
		Document document = getDocumentBuilder(schema).parse(new InputSource(new StringReader(receive)));
		
		Element root = document.getDocumentElement();
		Element authorize = (Element) root.getFirstChild();
		Element user = (Element) authorize.getFirstChild();
		
		return user.getFirstChild().getNodeValue();
	}
	
	public static void sendAnswer(String answer, DataOutputStream output) throws ParserConfigurationException, SAXException, IOException,
		TransformerConfigurationException, TransformerException {
	
		Schema schema = getSchema();
		
		Document document = getDocumentBuilder(schema).newDocument();
		
		Element root = document.createElement("document");
		document.appendChild(root);
		Element answerElement = document.createElement("answer");
		root.appendChild(answerElement);
		Element code = document.createElement("code");
		code.appendChild(document.createTextNode(answer));
		answerElement.appendChild(code);
		
		DOMSource source = validate(document, schema);
		
		String send = DOMtoString(source);
		output.writeUTF(send);
		output.flush();
	}
	
	public static String receiveAnswer(DataInputStream input) throws SAXException, IOException, ParserConfigurationException {
		String receive = input.readUTF();
	
		Schema schema = getSchema();
		
		Document document = getDocumentBuilder(schema).parse(new InputSource(new StringReader(receive)));
		
		Element root = document.getDocumentElement();
		Element answerElement = (Element) root.getFirstChild();
		Element code = (Element) answerElement.getFirstChild();
		
		return code.getFirstChild().getNodeValue();
	}
	
	public static void sendHistory(List<String> messages, DataOutputStream output) throws SAXException, IOException, ParserConfigurationException,
		TransformerConfigurationException, TransformerException {
		
		Schema schema = getSchema();
		
		Document document = getDocumentBuilder(schema).newDocument();
		
		Element root = document.createElement("document");
		document.appendChild(root);
		Element history = document.createElement("history");
		root.appendChild(history);
		for (String str : messages) {
			Element message = document.createElement("message");
			message.appendChild(document.createTextNode(str));
			root.appendChild(message);
		}
		
		DOMSource source = validate(document, schema);
		
		String send = DOMtoString(source);
		output.writeUTF(send);
		output.flush();
	}
	
	public static void receiveHistory(List<String> messages, DataInputStream input) throws SAXException, IOException, ParserConfigurationException {
		String receive = input.readUTF();
	
		Schema schema = getSchema();
		
		Document document = getDocumentBuilder(schema).parse(new InputSource(new StringReader(receive)));
		
		Element root = document.getDocumentElement();
		Element history = (Element) root.getFirstChild();
		messages.clear();
		for (Element message = (Element) history.getFirstChild(); message != null; message = (Element) history.getNextSibling()) {
			messages.add(message.getFirstChild().getNodeValue());
		}
	}
	
	public static void sendConnectUser(String name, DataOutputStream output) throws SAXException, IOException, ParserConfigurationException,
		TransformerConfigurationException, TransformerException {
		
		Schema schema = getSchema();
		
		Document document = getDocumentBuilder(schema).newDocument();
		
		Element root = document.createElement("root");
		document.appendChild(root);
		Element connectUser = document.createElement("connectUser");
		root.appendChild(connectUser);
		Element nameElement = document.createElement("name");
		nameElement.appendChild(document.createTextNode(name));
		connectUser.appendChild(nameElement);
		
		DOMSource source = validate(document, schema);
		
		String send = DOMtoString(source);
		output.writeUTF(send);
		output.flush();
	}
	
	public static String receiveConnectUser(DataInputStream input) throws SAXException, IOException, ParserConfigurationException {
		String receive = input.readUTF();
	
		Schema schema = getSchema();
		
		Document document = getDocumentBuilder(schema).parse(new InputSource(new StringReader(receive)));
		
		Element root = document.getDocumentElement();
		Element connectUser = (Element) root.getFirstChild();
		Element nameElement = (Element) connectUser.getFirstChild();
		return nameElement.getFirstChild().getNodeValue();
	}
	
	private static DocumentBuilder getDocumentBuilder(Schema schema) throws ParserConfigurationException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setSchema(schema);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		return builder;
	}
	
	private static Schema getSchema() throws SAXException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema = schemaFactory.newSchema(new File("res/document.xsd"));
		return schema;
	}
	
	private static DOMSource validate(Document document, Schema schema) throws SAXException, IOException {
		DOMSource source = new DOMSource(document);
		Validator validator = schema.newValidator();
		validator.validate(source);
		return source;
	}
	
	private static void transform(DOMSource source, OutputStream output) throws TransformerConfigurationException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();	
		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(source, new StreamResult(output));
	}
	
	private static String DOMtoString(DOMSource source) {
		try
        {
           StringWriter writer = new StringWriter();
           StreamResult result = new StreamResult(writer);
           TransformerFactory tf = TransformerFactory.newInstance();
           Transformer transformer = tf.newTransformer();
           transformer.transform(source, result);
           writer.flush();
           return writer.toString();
        }
        catch(TransformerException ex)
        {
           ex.printStackTrace();
           return null;
        }
	}
}