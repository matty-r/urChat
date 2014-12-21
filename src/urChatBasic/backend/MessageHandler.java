package urChatBasic.backend;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;

import urChatBasic.base.Constants;
import urChatBasic.base.IRCServerBase;
import urChatBasic.base.MessageHandlerBase;
import urChatBasic.base.UserGUIBase;
import urChatBasic.frontend.DriverGUI;

/** This class will Handle the message it has received and assign an approriate 
 * class that will parse the string and then
 * @author Matt
 *
 */
public class MessageHandler {
	static Set<IDGroup> groupIDs = new HashSet<IDGroup>();
	static Set<IDSingle> singleIDs = new HashSet<IDSingle>();
	
	IRCServerBase myServer;
	UserGUIBase gui = DriverGUI.gui;
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
	public MessageHandler(IRCServerBase server,String receivedText){
		this.myServer = server;
		
		if(groupIDs.isEmpty())
			addRanges();
		if(singleIDs.isEmpty())
			addSingles();
		
		Message receivedMessage = new Message(receivedText);
		
		boolean handled = false;
		
		
		if(!handled)
		for(IDSingle testSingle : singleIDs)
			if(testSingle.type.equals(MessageIdType.NUMBER_ID) && testSingle.isEqual(receivedMessage.idCommandNumber)){
				testSingle.handlerType.messageExec(receivedMessage);
				handled = true;
				break;
			}

		if(!handled)
		for(IDSingle testSingle : singleIDs)
			if(testSingle.type.equals(MessageIdType.STRING_ID) && testSingle.isEqual(receivedMessage.idCommand)){
				testSingle.handlerType.messageExec(receivedMessage);
				handled = true;
				break;
			}
		
		if(!handled)
		for(IDGroup testRange : groupIDs)
			if(testRange.type.equals(MessageIdType.NUMBER_ID) && testRange.inRange(receivedMessage.idCommandNumber)){
				testRange.handlerType.messageExec(receivedMessage);
				handled = true;
				break;
			}
			
		if(!handled)
		handleDefault(receivedMessage);
		
	}
	
	public MessageHandler(String receivedText){
		if(groupIDs.isEmpty())
			addRanges();
		if(singleIDs.isEmpty())
			addSingles();
		
		Message receivedMessage = new Message(receivedText);
		
		boolean handled = false;
		
		
		if(!handled)
		for(IDSingle testSingle : singleIDs)
			if(testSingle.type.equals(MessageIdType.NUMBER_ID) && testSingle.isEqual(receivedMessage.idCommandNumber)){
				testSingle.handlerType.messageExec(receivedMessage);
				handled = true;
				break;
			}

		if(!handled)
		for(IDSingle testSingle : singleIDs)
			if(testSingle.type.equals(MessageIdType.STRING_ID) && testSingle.isEqual(receivedMessage.idCommand)){
				testSingle.handlerType.messageExec(receivedMessage);
				handled = true;
				break;
			}
		
		if(!handled)
		for(IDGroup testRange : groupIDs)
			if(testRange.type.equals(MessageIdType.NUMBER_ID) && testRange.inRange(receivedMessage.idCommandNumber)){
				testRange.handlerType.messageExec(receivedMessage);
				handled = true;
				break;
			}
			
		if(!handled)
		handleDefault(receivedMessage);
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

	public static class IDGroup { 
		private final int min, max; 
		private final MessageHandlerBase handlerType;
		private final MessageIdType type = MessageIdType.NUMBER_ID;
		
		public IDGroup(int min, int max,MessageHandlerBase handlerType) { 
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
	
	public static class IDSingle{
		String id;
		int[] idArray;
		MessageHandlerBase handlerType;
		MessageIdType type;
		
		public IDSingle(int id,MessageHandlerBase handlerType) {
			this.idArray = new int[]{id};
			this.handlerType = handlerType;
			type = MessageIdType.NUMBER_ID;
		}
		
		public IDSingle(int[] id,MessageHandlerBase handlerType) {
			this.idArray = id;
			this.handlerType = handlerType;
			type = MessageIdType.NUMBER_ID;
		}
		
		public IDSingle(String id,MessageHandlerBase handlerType) {
			this.id = id;
			this.handlerType = handlerType;
			type = MessageIdType.STRING_ID;
		}
		
		public boolean isEqual(String testId){
			try{
			if(Integer.parseInt(testId) > 0)
				return isEqual(Integer.parseInt(testId));
			} catch(Exception e){
				return id.equals(testId);
			}
			return false;	
		}
		
		public boolean isEqual(int testId){
			for(int x : idArray)
				if(x == testId){
					return true;
				}
			
			return false;
		}
	}
	
	private void addRanges(){
		groupIDs.add(new IDGroup(1,4,new UserRegistrationMessage())); 
		groupIDs.add(new IDGroup(311,322,new CommandResponseMessage()));
		groupIDs.add(new IDGroup(412,415,new BadPrivateMessage()));
		groupIDs.add(new IDGroup(371,376,new GeneralMessage()));
		groupIDs.add(new IDGroup(251,256,new GeneralMessage()));
		groupIDs.add(new IDGroup(471,475,new JoinFailureMessage()));
	}
	
	private void addSingles(){
		singleIDs.add(new IDSingle(5,new NoticeMessage()));
		singleIDs.add(new IDSingle(353,new UsersListMessage()));
		singleIDs.add(new IDSingle((new int[]{311,319,312,318}),new WhoIsMessage()));
		singleIDs.add(new IDSingle(332,new ChannelTopicMessage()));
		singleIDs.add(new IDSingle((new int[]{366,265,266,250,333,328,477}),new GeneralMessage()));
		singleIDs.add(new IDSingle((new int[]{432,433}),new InvalidNickMessage()));
		singleIDs.add(new IDSingle("MODE",new ModeMessage()));
		singleIDs.add(new IDSingle("NOTICE",new NoticeMessage()));
		singleIDs.add(new IDSingle("PRIVMSG",new PrivateMessage()));
		singleIDs.add(new IDSingle("PART",new PartMessage()));
		singleIDs.add(new IDSingle("KICK",new KickMessage()));
		singleIDs.add(new IDSingle("JOIN",new JoinMessage()));
		singleIDs.add(new IDSingle(":Closing",new DisconnectMessage()));
		singleIDs.add(new IDSingle("QUIT",new DisconnectMessage()));
		singleIDs.add(new IDSingle("NICK",new RenameUserMessage()));
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
			setIdCommand();
			setMessageBody();
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
			if(prefix.charAt(0) == ':')
				if(countOfOccurrences(prefix, '.') == 2)
					this.server = prefix.substring(1).trim();
		}
		
		private void setChannel(){
			String withoutPrefix = rawMessage.replace(prefix, "").trim();
			int messageBegin = posnOfOccurrence(withoutPrefix, SPACES_AHEAD_DELIMITER, 1);
			
			int channelBegin = withoutPrefix.indexOf(CHANNEL_DELIMITER);
			if(channelBegin < messageBegin && channelBegin > -1)
				this.channel = withoutPrefix.substring(channelBegin, messageBegin).split(" ")[0].trim();
			else
				this.channel = withoutPrefix.split(" ")[1];
		}
		
		private void setMessageBody(){
			try{
			String withoutPrefixIdChannel = rawMessage.replace(prefix,"").trim();

			if(withoutPrefixIdChannel.indexOf(":") > -1)
				this.body = withoutPrefixIdChannel.substring(withoutPrefixIdChannel.indexOf(":")+1).trim();
			else
				this.body = withoutPrefixIdChannel;
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
		
		@Override
		public void messageExec(Message myMessage) {
			printServerText(myMessage.body);
		}

	}
	
	public class CommandResponseMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			printServerText(myMessage.body);
		}
	}
	
	public class GeneralMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			printServerText(myMessage.body);
		}
	}
	
	public class JoinMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			if(myMessage.nick.equals(myServer.getNick())){
        		myServer.addToCreatedChannels(myMessage.channel);
				myServer.printEventTicker(myMessage.channel, "You have joined "+myMessage.channel);
        	} else 
				myServer.addToUsersList(myMessage.channel, myMessage.nick);
		}

	}
	
	public class ChannelTopicMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			myServer.setChannelTopic(myMessage.channel, myMessage.body);
		}
	}
		
	public class UsersListMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			myServer.addToUsersList(myMessage.channel, myMessage.body.split(" "));
		}
	}
	
	public class RenameUserMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			if(!myMessage.nick.equals(myServer.getNick()))
				myServer.renameUser(myMessage.nick, myMessage.body);
		}
	}
	
	public class JoinFailureMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			printServerText(myMessage.body);
		}

	}
	
	public class ModeMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			printServerText(myMessage.body);
		}
	
	}

	public class WhoIsMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			printPrivateText(myMessage.rawMessage.split(" ")[3],myMessage.body,myMessage.rawMessage.split(" ")[3]);
		}
	
	}	
	
	public class ServerChangeMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			//TODO
		}

	}
	
	public class BadPrivateMessage implements MessageHandlerBase{

		@Override
		public void messageExec(Message myMessage) {
			// TODO Auto-generated method stub
		}

		
	}
	
	public class InvalidNickMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			printServerText(myMessage.body);
		}
	}	
	
	public class NoticeMessage implements MessageHandlerBase{

		@Override
		public void messageExec(Message myMessage) {
			if(myMessage.nick != null && myMessage.nick.toLowerCase().equals("NickServ".toLowerCase())){
				printPrivateText(myMessage.nick, myMessage.body, myMessage.nick);
				gui.connectFavourites(myServer);
			} else
				printServerText(myMessage.body);
		}


		
	}
	
	public class PrivateMessage implements MessageHandlerBase{

		@Override
		public void messageExec(Message myMessage) {
			 if(!myMessage.channel.equals(myServer.getNick())){
					if(isBetween(myMessage.body,CTCP_DELIMITER,"ACTION",CTCP_DELIMITER))
						myMessage.body = myMessage.body.replace(CTCP_DELIMITER+"ACTION", ">");
					myServer.printChannelText(myMessage.channel,myMessage.body,myMessage.nick);
				} else{
					myServer.printChannelText(myMessage.channel,myMessage.body,myMessage.nick);
				}
		}


	}
	
	public class PartMessage implements MessageHandlerBase{

		@Override
		public void messageExec(Message myMessage) {
			if(!(myMessage.nick.equals(myServer.getNick()))){
				for(String tempChannel : myMessage.channel.split(","))
					myServer.removeFromUsersList(tempChannel, myMessage.nick);
			} else {
				for(String tempChannel : myMessage.channel.split(",")){
					myServer.removeFromUsersList(tempChannel, myServer.getNick());
					printServerText( "You quit "+tempChannel);
				}
			}
		}

		
	}
	
	public class KickMessage implements MessageHandlerBase{

		@Override
		public void messageExec(Message myMessage) {
			myServer.removeFromUsersList(myMessage.channel, myMessage.body);
		}

		
	}
	
	public class DisconnectMessage implements MessageHandlerBase{

		@Override
		public void messageExec(Message myMessage) {
			if(myServer.getNick().equals(myMessage.nick)){
			gui.quitServer(myServer);
			for(Handler tempHandler:Constants.LOGGER.getHandlers())
				tempHandler.close();
			} else
				myServer.removeFromUsersList(myServer.getName(),myMessage.nick);
		}
	}

	private void handleDefault(Message myMessage) {
		printServerText(myMessage.rawMessage);
		Constants.LOGGER.log(Level.WARNING, "NOT HANDLED: "+myMessage.rawMessage);
	}
	
	private void printPrivateText(String userName,String line,String fromUser){
		myServer.printPrivateText(userName, line, fromUser);
	}
	
	private void printServerText(String message){
		myServer.printServerText(message);
	}
}
