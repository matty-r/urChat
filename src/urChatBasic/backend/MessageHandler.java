package urChatBasic.backend;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import urChatBasic.base.Constants;
import urChatBasic.base.MessageHandlerBase;
import urChatBasic.frontend.IRCServer;

/** This class will Handle the message it has received and assign an approriate 
 * class that will parse the string and then
 * @author Matt
 *
 */
public class MessageHandler {
	static Set<Range> messageRanges = new HashSet<Range>(); 
	IRCServer myServer;
	private static final char CHANNEL_DELIMITER = '#';
	private static final char CTCP_DELIMITER = '\001';
	private static final char SPACES_AHEAD_DELIMITER = ':';
	private static final int MESSAGE_LIMIT = 510;
	private static final String END_MESSAGE = "\r\n";
	
	/**
	 * Assign the correct 
	 * @param server
	 * @param messageID
	 */
	public MessageHandler(String receivedText){
		if(messageRanges.isEmpty())
			addRanges();
		
		Message receivedMessage = new Message(receivedText);
		
		boolean handled = false;
		if(receivedMessage.type == MessageIdType.NUMBER_ID){
			for(Range testRange : messageRanges)
				if(testRange.inRange(receivedMessage.idCommandNumber)){
					testRange.handlerType.messageExec(receivedMessage);
					handled = true;
					break;
				}
			if(handled == false)
				handleDefault("["+receivedMessage.idCommandNumber+"] "+receivedMessage.body);
		} else
			handleDefault(receivedMessage.toString());
		
	}
	
	public static void main(String[] args){
		runTests();
	}
	
	private static void runTests(){
		new MessageHandler(":super!duper@127.0.0.1 003 matty_r @ #testingonly :@matty_r");
		new MessageHandler(":irc.freenode.net 003 matty_r @ #testingonly :@matty_r");
		new MessageHandler(":super!duper@SOME:IPV6:GOES:HERE:DEAD 003 matty_r @ #testingonly :@matty_r");
		new MessageHandler(":super!~duper@127.0.0.1 003 matty_r @ #testingonly :@matty_r");
		new MessageHandler("super!duper@127.0.0.1 001 matty_r @ #testingonly :@matty_r");
		new MessageHandler(":super!duper@127.0.0.1 003 matty_r @ #testingonly :@matty_r");
		new MessageHandler(":super!duper@127.0.0.1 003 matty_r @ #testingonly :@matty_r");
		new MessageHandler(":super!duper@127.0.0.1 003 matty_r @ #testingonly :@matty_r");
		new MessageHandler(":super!duper@127.0.0.1 003 matty_r @ #testingonly :@matty_r");
		new MessageHandler(":super!duper@127.0.0.1 003 matty_r @ #testingonly :@matty_r");
	}
	
	private int posnOfOccurrence(String str, char c, int n) {
	    int pos = 0;
	    int matches = 0;

	    for(char myChar : str.toCharArray()){
	    	if(myChar == c){
	    		matches++;
	    		if(matches == n)
	    			break;
	    	}
	    	pos++;
	    }
	    return pos;
	}
	
	private int countOfOccurrences(String str, char c) {
	    int matches = 0;

	    for(char myChar : str.toCharArray()){
	    	if(myChar == c)
	    		matches++;
	    }
	    return matches;
	}
	
	private Boolean isBetween(String line,char start,String middle,char end) {
		int startIndex = line.indexOf(start);
		int middleIndex = line.indexOf(middle);
		int endIndex = line.substring(startIndex+1).indexOf(end) + startIndex;
		
		if(startIndex >= 0 && middleIndex >= 0 && endIndex >= 0)
			if(middleIndex > startIndex && middleIndex < endIndex)
				return true;

	    return false;
	}

	
	public static class Range { 
		private final int min, max; 
		private final MessageHandlerBase handlerType;
		
		public Range(int min, int max,MessageHandlerBase handlerType) { 
			this.min = min; 
			this.max = max; 
			this.handlerType = handlerType;
		}
		
		public boolean inRange(int checkNumber){
			if(checkNumber >= this.min && checkNumber <= this.max)
				return true;
			else return false;
		}
	} 
	
	
	private void addRanges(){
		messageRanges.add(new Range(1, 4,new UserRegistrationMessage())); 
		messageRanges.add(new Range(312, 322,new CommandResponseMessage()));
	}
	
	public enum MessageIdType{
		NUMBER_ID,STRING_ID
	}
	
	public class Message{
		String prefix;
		String idCommand;
		int idCommandNumber;
		String channel;
		String body;
		MessageIdType type;
		String rawMessage;
		String host;
		String server;
		String nick;
		
		public Message(String fullMessage){
			this.rawMessage = fullMessage;
			setPrefix();
			setChannel();
			setMessageBody();
			setIdCommand();
			setServer();
			setHost();
			setNick();
			
			try{
				this.idCommandNumber = Integer.parseInt(this.idCommand);
				this.type = MessageIdType.NUMBER_ID;
			} catch(Exception e){
				this.type = MessageIdType.STRING_ID;
			}	
			
		}
		
		public String toString(){
			return rawMessage;
		}
		

		private void setPrefix(){
			prefix = rawMessage.split(" ")[0];
		}
		
		private void setNick(){
			if(isBetween(rawMessage,':',"!",'@'))
				this.nick = rawMessage.substring(1, rawMessage.indexOf("!")).trim();
		}
		
		private void setHost(){
			String tempMessage = rawMessage.split(" ")[0];
			if(tempMessage.indexOf('@') > -1)
				this.host = tempMessage.substring(tempMessage.indexOf('@')+1).trim();

		}
		
		private void setServer(){
			String tempMessage = rawMessage.split(" ")[0];
			if(tempMessage.charAt(0) == ':')
				if(countOfOccurrences(tempMessage, '.') == 2)
					this.server = tempMessage.substring(1).trim();
		}
		
		private void setChannel(){
			int messageBegin = posnOfOccurrence(rawMessage, SPACES_AHEAD_DELIMITER, 2);
			
			int channelBegin;
			if((channelBegin = rawMessage.indexOf(CHANNEL_DELIMITER)) < messageBegin)
				this.channel = rawMessage.substring(channelBegin, messageBegin).split(" ")[0].trim();
		}
		
		private void setMessageBody(){
			try{
			if(countOfOccurrences(rawMessage, ':') > 1)
				this.body = rawMessage.substring(posnOfOccurrence(rawMessage, ':', 2)+1).trim();
			else
				this.body = rawMessage.substring(posnOfOccurrence(rawMessage, ':', 1)+1).trim();
			} catch(IndexOutOfBoundsException e) {
				Constants.LOGGER.log(Level.SEVERE, "Failed to extract a message from received text. " + e.getLocalizedMessage());
			}
		}
		
		private void setIdCommand(){
			idCommand = rawMessage.split(" ")[1];
		}
	}
	
	/**
	 * MessageHandlerBase simply contains two abstract methods to be overridden
	 * @author Matt
	 *
	 */
	public class UserRegistrationMessage implements MessageHandlerBase {
		private String customMessage = null;
		private Message myMessage;
		
		@Override
		public void messageExec(Message myMessage) {
			// TODO Parse the message and send it to the appropriate object
			//myServer.printText(true, message);
			this.myMessage = myMessage;
			System.out.println(this.toString());
		}

		@Override
		public void exactIdMatch() {
			switch(myMessage.idCommandNumber){
			case 1: customMessage = "MOTD "+myMessage.body;
					break;
			default: customMessage = "|Extrack prefix="+myMessage.prefix+"|Extract Channel="+myMessage.channel+ "|Extract Messsage="+myMessage.body+"|";
			}
		}
		
		@Override
		public String toString(){
			exactIdMatch();

			if(customMessage == null)
				return myMessage.body;
			else
				return customMessage;
		}
	}
	
	public class CommandResponseMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			System.out.println(myMessage.body);
		}

		@Override
		public void exactIdMatch() {
			// TODO Auto-generated method stub
			
		}		
		
		@Override
		public String toString(){
			
			return null;
		}
	}
	
	/**
	 * MessageHandlerBase simply contains two abstract methods to be overridden
	 * @author Matt
	 *
	 */
	private void handleDefault(String message) {
		System.out.println("Unhandled: "+message);
	}
	
	
}
