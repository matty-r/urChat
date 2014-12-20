package urChatBasic.backend;

import java.util.Map;

import urChatBasic.base.MessageHandlerBase;
import urChatBasic.frontend.IRCServer;

/** This class will Handle the message it has received and assign an approriate 
 * class that will parse the string and then
 * @author Matt
 *
 */
public class MessageHandler {
	Map<String, MessageHandlerBase> myMap;
	IRCServer myServer;
	//Maybe the MIN and MAX can be put into an enum?
	static final int USER_REGISTRATION_MIN = 1;
	static final int USER_REGISTRATION_MAX = 4;
	static final int COMMAND_RESPONSE_MIN = 312;
	static final int COMMAND_RESPONSE_MAX = 322;
	
	/**
	 * Assign the correct 
	 * @param server
	 * @param messageID
	 */
	public MessageHandler(IRCServer server,String messageID){
		int messageNumber = Integer.parseInt(messageID);
		
		if(withinRange(USER_REGISTRATION_MIN,messageNumber,USER_REGISTRATION_MAX))
			myMap.put("User Registration Message", new UserRegistrationMessage());
		if(withinRange(COMMAND_RESPONSE_MIN,messageNumber,COMMAND_RESPONSE_MAX))
			myMap.put("Command Response Message", new CommandResponseMessage());
	}
	
	/**
	 * Is the middle number within the min and max Range?
	 * @param min
	 * @param middle
	 * @param max
	 * @return Boolean
	 */
	private Boolean withinRange(int min,int middle,int max){
		if(middle >= min && middle <= max)
			return true;
		else return false;
	}
	
	/**
	 * MessageHandlerBase simply contains two abstract methods to be overridden
	 * @author Matt
	 *
	 */
	public class UserRegistrationMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(String message) {
			// TODO Parse the message and send it to the appropriate object
			myServer.printText(true, message);
		}

		@Override
		public void messageParse(String message) {
			// TODO Parse the String as per UserRegistrationmessage
			
		}
		
	}
	
	public class CommandResponseMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(String message) {
			// TODO Parse the message and send it to the appropriate object
			myServer.printText(true, message);
		}

		@Override
		public void messageParse(String message) {
			// TODO Parse the String as per CommandResponseMessage
			
		}		
	}
	
	
}
