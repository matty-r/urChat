package urChatBasic.backend;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import urChatBasic.base.ConnectionBase;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCServerBase;
import urChatBasic.base.UserGUIBase;


public class Connection implements ConnectionBase{

	//This was static.. it was causing silly issues with the reader/writer
	private BufferedWriter writer;
	private BufferedReader reader;
	
	private IRCServerBase server;
	private String myNick;
	private String login;
	private String portNumber;
	private Socket mySocket;
	public UserGUIBase gui;
	
	//Used for Logging messages received by the server
	//Debug Mode
	//Currently deprecated
	//private DateFormat debugDateFormat = new SimpleDateFormat("ddMMyyyy");
	//private DateFormat debugTimeFormat = new SimpleDateFormat("HHmm");
	//private Date todayDate = new Date();
	//private String debugFile;

    public Connection(IRCServerBase server,String nick,String login,String portNumber, UserGUIBase ugb){
    	this.gui = ugb;
    	this.server =  server;
    	this.myNick = nick;
    	if(portNumber.trim().equals(""))
    		this.portNumber = Constants.DEFAULT_FIRST_PORT;
    	this.portNumber = portNumber;
    	this.login = login;
	}
    
    /* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#isConnected()
	 */
    @Override
	public Boolean isConnected(){
    	return (mySocket != null) && (server != null) && (mySocket.isConnected());
    }
    
    /* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#getServer()
	 */
    @Override
	public IRCServerBase getServer(){
    	return this.server;
    }
    
    /* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#getServer()
	 */
    @Override
	public String getPortNumber(){
    	return this.portNumber;
    }
    
    /* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#getServer()
	 */
    @Override
	public String getLogin(){
    	return this.login;
    }

    private void startUp() throws IOException{
    	
		localMessage("Attempting to connect to "+server);
		//TODO must be able to type in the port number instead of having this hard-coded.
        mySocket = new Socket(server.getName(), Integer.parseInt(getPortNumber()));

		writer = new BufferedWriter(
                new OutputStreamWriter(mySocket.getOutputStream( )));
        reader = new BufferedReader(
                new InputStreamReader(mySocket.getInputStream( ),StandardCharsets.UTF_8));
        
        localMessage("Connected to "+getServer());
        
        
        String line = null;
        
        // Log on to the server.
        writer.write("NICK " + getNick() + "\r\n");
        writer.write("USER " + login + " 8 * : "+getLogin()+"\r\n");
        localMessage("Connect with nick "+getNick());
        writer.flush( );
        
        // Read lines from the server until it tells us we have connected.
        
        while ((line = reader.readLine()) != null) {
        	if (line.indexOf("004") >= 0) {
                //Logged in successfully
        		localMessage("Logged in successfully.");
        		gui.connectFavourites(server);
                break;
            } else
            	serverMessage(line);
        }
        
        // Keep reading lines from the server.
        while ((line = reader.readLine()) != null) {
        	serverMessage(line);
        }
        
		writer.close();
		reader.close();
		mySocket.close();
    }
	
	/* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#setNick(java.lang.String)
	 */
	@Override
	public void setNick(String newNick){
		myNick = newNick;
	}
	
	/* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#getNick()
	 */
	@Override
	public String getNick(){
		return myNick;
	}
	
	/* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#sendClientText(java.lang.String, java.lang.String)
	 */
	@Override
	public void sendClientText(String clientText,String fromChannel) throws IOException{
		if(isConnected()){
		
			String[] tempTextArray = clientText.split(" ");
			
			if(clientText != ""){
				if(clientText.startsWith("/join")){
					writer.write("JOIN " + clientText.replace("/join ","") +"\r\n");
				} else if(clientText.startsWith("/nick")){
						writer.write("NICK " + clientText.replace("/nick ","") +"\r\n");
						myNick = clientText.replace("/nick ","");
				} else if(clientText.startsWith("/msg")){
					tempTextArray = clientText.split(" ");
					if(tempTextArray.length > -1){
						writer.write("PRIVMSG " + tempTextArray[1] + " :"+clientText.replace("/msg "+tempTextArray[1]+" ", "") +"\r\n");
						server.printPrivateText(tempTextArray[1], clientText.replace("/msg "+tempTextArray[1]+" ", ""), myNick);
						gui.setCurrentTab(tempTextArray[1]);
					}
				} else if(clientText.startsWith("/whois")){
					if(tempTextArray.length > -1)
						writer.write("WHOIS " + tempTextArray[1] +"\r\n");
				} else if(clientText.startsWith("/quit")){
						writer.write("QUIT :" + clientText.replace("/quit ","") +"\r\n");
				} else if(clientText.startsWith("/part")){
					writer.write("PART " + fromChannel + " :" + clientText.replace("/part  ","") +"\r\n");
				} else if(clientText.startsWith("/me")){
					writer.write("PRIVMSG " + fromChannel + " :  ACTION " + clientText.replace("/me ","") +"  \r\n");
				} else {
					writer.write("PRIVMSG " + fromChannel + " :"+clientText+"\r\n");
					server.printChannelText(fromChannel, clientText, myNick);
				}
			writer.flush();
			
			if(clientText.toLowerCase().startsWith("/msg nickserv"))
				clientText = "*** HIDDEN NICKSERV IDENTIFY ***";
			Constants.LOGGER.log(Level.FINE, "Client Text:- "+fromChannel+" "+clientText);
			}
		}
	}

	private void localMessage(String message){
		server.printServerText(message);
		Constants.LOGGER.log(Level.FINE, "Local Text:-"+message);
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
	
	private Boolean isBetween(String line,String start,String middle,String end) {
		int startIndex = line.indexOf(start);
		int middleIndex = line.indexOf(middle);
		int endIndex = line.indexOf(end);
		
		if(startIndex >= 0 && middleIndex >= 0 && endIndex >= 0)
			if(middleIndex > startIndex && middleIndex < endIndex)
				return true;

	    return false;
	}
	 
	private void serverMessage(String receivedText) throws IOException{
		Constants.LOGGER.log(Level.FINE, receivedText);
		String[] receivedOptions;
		String message = "";

		if(isBetween(receivedText,":","!","@")){
			receivedOptions = receivedText.split(" ");
			receivedText = receivedText.replace(receivedOptions[0], ":");
		} else			
			receivedOptions = receivedText.substring(posnOfOccurrence(receivedText, ':', 1)+1, posnOfOccurrence(receivedText, ':', 2)).split(" ");

		
		try{
		if(countOfOccurrences(receivedText, ':') > 1)
			message = receivedText.substring(posnOfOccurrence(receivedText, ':', 2)+1);
		} catch(IndexOutOfBoundsException e) {
			Constants.LOGGER.log(Level.SEVERE, "Whats going on here? " + e.getLocalizedMessage());
		}
		
		 if (receivedText.toLowerCase( ).startsWith("ping")) {
             // We must respond to PINGs to avoid being disconnected.
             writer.write("PONG " + message + "\r\n");
             writer.flush();
         }
         else {
        	 if(receivedOptions.length > 1)
	        	 switch(receivedOptions[1]){
	        	 	//001 = Server MOTD
	        	 	case "001": printServerText(message);
	        	 				break;
	        	 	//002 = TODO
	        	 	case "002": printServerText(message);
	        	 				break;
	        	 	//003 = TODO
	        	 	case "003": printServerText(message);
	        	 				break;
	 				//251 = TODO
	        	 	case "251": printServerText(message);
	        	 				break;
	        	 	//005 = Server change
		        	case "005" :String tempServer = receivedOptions[0].substring(1);
		        				printServerText(">> Changed your server to "+tempServer);
		        	 			break;
    	 			//332 = Channel Topic
		        	case "332" :
		        				server.setChannelTopic(receivedOptions[3], message);
		        				break;
    	 			//372 = Server MOTD
		        	case "372" :printServerText(message);
		        				break;
		        	//353 = User List
					//:barjavel.freenode.net 353 matty_r @ #testingonly :@matty_r
		        	case "353" :server.addToUsersList(receivedOptions[4], message.split(" "));
		        		 		break;
    		 		//470 = Forwarding to another channel
		        	//case "470" :if(tempTextArray[3] == channel)
		        					//channel = tempTextArray[4];
    							//break;
    		 		//477 = :tepper.freenode.net 477 matty_r ##java :Cannot join channel (+r) - you need to be identified with services
					//473 = Cannot join channel 
		        	case "473" :gui.setCurrentTab(1);
		        				printServerText("!! "+myNick+" unable to join channel.");
		        				Toolkit.getDefaultToolkit().beep();
		        				break;
					//433 = Nickname already in use
		        	case "433" :printServerText("!! "+myNick+" is already in use.");
		        				Toolkit.getDefaultToolkit().beep();
		        				break;
		        	//432 = Nickname is invalid.
		        	case "432" :printServerText("!! "+myNick+" is invalid.");
		        				Toolkit.getDefaultToolkit().beep();
    							break;
		        	//JOIN = When a user joins a channel
		        	case "JOIN":String joinChannel = receivedText.substring(receivedText.indexOf('#'));
		        				if(extractNick(receivedOptions[0]).equals(myNick)){
					        		server.addToCreatedChannels(joinChannel);
			    					server.printEventTicker(joinChannel, "You have joined "+joinChannel);
					        	} else 
		        					server.addToUsersList(joinChannel, extractNick(receivedOptions[0]));
		        				break;
		        	case "PRIVMSG": if(!receivedOptions[2].equals(myNick)){
		        						server.printChannelText(receivedOptions[2],message,extractNick(receivedOptions[0]));
			        				} else{
			        					server.printPrivateText(extractNick(receivedOptions[0]),message,extractNick(receivedOptions[0]));
			        				}
		        				break;
    				//311 = Whois response
		        	case "311":	printPrivateText(receivedOptions[3],message,receivedOptions[3]);
        						break;
					//319 = Whois response
		        	case "319":	printPrivateText(receivedOptions[3],message,receivedOptions[3]);
								break;
					//312 = Whois response
		        	case "312":	printPrivateText(receivedOptions[3],message,receivedOptions[3]);
								break;
					//330 = Whois response
		        	case "330":	printPrivateText(receivedOptions[3],message,receivedOptions[3]);
								break;
					//318 = Whois response
		        	case "318":	printPrivateText(receivedOptions[3],message,receivedOptions[3]);
								break;
		        	case "NICK":if(!(extractNick(receivedOptions[0]).equals(myNick)))
									server.renameUser(extractNick(receivedOptions[0]), message);
		    					break;
		        	case "PART":if(!(extractNick(receivedOptions[0]).equals(myNick))){
		        					for(String tempChannel : receivedOptions[2].split(","))
		        						server.removeFromUsersList(tempChannel, extractNick(receivedOptions[0]));
		        				} else {
		        					for(String tempChannel : receivedOptions[2].split(",")){
		        						server.removeFromUsersList(tempChannel, myNick);
		        						printServerText( "You quit "+tempChannel);
	        						}
		        				}
		        				break;
		        	//1415> :dreamreal!~dreamreal@unaffiliated/dreamreal KICK ##java codecutter :codecutter
		        	case "KICK":server.removeFromUsersList(receivedOptions[2], message);
					        	break;
    				//:macabre_!~ibtjw@23-114-59-64.lightspeed.austtx.sbcglobal.net QUIT :Ping timeout: 252 seconds
		        	case "QUIT":if(!(extractNick(receivedOptions[0]).equals(myNick)))
			    						server.removeFromUsersList("Server", extractNick(receivedOptions[0]));
			    				break;
    				//:NickServ!NickServ@services. NOTICE matty_r :You are now identified for matty_r.
		        	case "NOTICE":if(extractNick(receivedOptions[0]) != null && extractNick(receivedOptions[0]).equals("NickServ")){
		        					printPrivateText(extractNick(receivedOptions[0]), message, extractNick(receivedOptions[0]));
		        					gui.connectFavourites(server);
		        				} else
		        					printServerText( message);
		    					break;
		        	case "Link"://gui.shutdownAll();
		        				gui.quitServer(server);
		        				break;
		        	default:printServerText(message);
		        			Constants.LOGGER.log(Level.WARNING, "NOT HANDLED: " + message);
	        	 			break;
	        	 } else {
	        		printServerText(message);
	        		Constants.LOGGER.log(Level.WARNING, "NOT HANDLED: " + message);
	        	 }
         }
	}
	
	private void printPrivateText(String userName,String line,String fromUser){
		server.printPrivateText(userName, line, fromUser);
	}
	
	private void printServerText(String message){
		server.printServerText(message);
	}
	
	/* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#writeDebugFile(java.lang.String)
	 */
	
	private String extractNick(String textString){
		if(textString.indexOf("!") > -1)
			if(textString.startsWith(":"))
				return textString.substring(1, textString.indexOf("!"));
			else
				return textString.substring(0, textString.indexOf("!"));
		
		return null;
	}

	/* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#run()
	 */
	@Override
	public void run() {
		try {
			if(getPortNumber() != null && getServer() != null && getNick() != null)
				startUp();
		} catch (IOException e) {
			Constants.LOGGER.log(Level.SEVERE, "startUp() failed! " + e.getLocalizedMessage());
		}
	}


}