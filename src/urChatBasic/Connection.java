package urChatBasic;

import java.awt.Toolkit;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Connection implements Runnable{

	private static BufferedWriter writer;
	private static BufferedReader reader;
	
	public static String server = null;
	public static String myNick = null;
	public static String login = null;
	public static String firstChannel = null;
	private Socket mySocket = null;
	public static UserGUI gui = DriverGUI.gui;
	
	//Used for Logging messages received by the server
	//Debug Mode
	private DateFormat debugDateFormat = new SimpleDateFormat("ddMMyyyy");
	private DateFormat debugTimeFormat = new SimpleDateFormat("HHmm");
	private Date todayDate = new Date();
	private String debugFile;
	
	
	
    public Connection(){
	    	try {
				startUp();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
    
    public Boolean getConnected(){
    	if(mySocket == null)
    		return false;
    	if(server == null)
    		return false;
    	if(mySocket.isConnected())
    		return true;
    	
    	return false;
    }
    
    private void startUp() throws IOException{
    	gui.addCreatedServers(server);
        localMessage("Attempting to connect to "+server);
        mySocket = new Socket(server, 6667);
        
        
        localMessage("Connected to "+mySocket.getInetAddress().getHostName());
        
		writer = new BufferedWriter(
                new OutputStreamWriter(mySocket.getOutputStream( )));
        reader = new BufferedReader(
                new InputStreamReader(mySocket.getInputStream( ),StandardCharsets.UTF_8));
        
        String line = null;

        // Log on to the server.
        writer.write("NICK " + myNick + "\r\n");
        writer.write("USER " + login + " 8 * : "+myNick+"\r\n");
        localMessage("Connect with nick "+myNick);
        writer.flush( );
        
        // Read lines from the server until it tells us we have connected.
        
        while ((line = reader.readLine()) != null) {
        	if (line.indexOf("004") >= 0) {
                //Logged in successfully
        		localMessage("Logged in successfully.");
                break;
            } else
            	serverMessage(line);
        }

        // Join the channel.
        writer.write("JOIN " + firstChannel + "\r\n");
        writer.flush();
        localMessage("Connecting to channel "+firstChannel); 
        
        // Keep reading lines from the server.
        while ((line = reader.readLine()) != null) {
                // Print the raw line received by the bot.
        	serverMessage(line);
        }
        
        mySocket.close();
    	//gui.printServerText("Server", "Socket has closed.","Server");
    }
	
	public static void setNick(String newNick){
		myNick = newNick;
	}
	
	public static String getNick(){
		return myNick;
	}
	
	public static void sendClientText(String clientText,String fromChannel) throws IOException{
		String[] tempTextArray = null;
		String tempText = "";
		
		if(clientText != ""){
			if(clientText.startsWith("/join")){
				writer.write("JOIN " + clientText.replace("/join ","") +"\r\n");
			} else if(clientText.startsWith("/nick")){
					writer.write("NICK " + clientText.replace("/nick ","") +"\r\n");
					myNick = clientText.replace("/nick ","");
			} else if(clientText.startsWith("/msg")){
				tempTextArray = clientText.split(" ");
				for(int x = 2; x < tempTextArray.length; x++){
					tempText += tempTextArray[x] + " ";
				}
				writer.write("PRIVMSG " + tempTextArray[1] + " :"+tempText.substring(0, tempText.length()-1) +"\r\n");
			} else if(clientText.startsWith("/quit")){
					writer.write("QUIT :" + clientText.replace("/quit ","") +"\r\n");
			} else if(clientText.startsWith("/me")){
				writer.write("PRIVMSG " + fromChannel + " :  ACTION " + clientText.replace("/me ","") +"  \r\n");
			} else {
				writer.write("PRIVMSG " + fromChannel + " :"+clientText+"\r\n");
				gui.printChannelText(fromChannel, "<"+myNick+"> "+clientText,myNick);
			}
		writer.flush();
		}
	}

	private void localMessage(String message){
		gui.printServerText(server,message);
	}
	
	
	private int nthOccurrence(String str, char c, int n) {
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
	
	private Boolean isBetween(String line,String start,String middle,String end) {
		int startIndex = line.indexOf(start);
		int middleIndex = line.indexOf(middle);
		int endIndex = line.indexOf(end);
		
		if(startIndex >= 0 && middleIndex >= 0 && endIndex >= 0)
			if(middleIndex > startIndex && middleIndex < endIndex)
				return true;

	    return false;
	}
	 
	//TODO Create private room on privmsg
	private void serverMessage(String receivedText) throws IOException{
		writeDebugFile(receivedText);
		String[] receivedOptions;
		String message = "";

		if(isBetween(receivedText,":","!~","@"))
			receivedOptions = receivedText.split(" ");
		else			
			receivedOptions = receivedText.substring(nthOccurrence(receivedText, ':', 1)+1, nthOccurrence(receivedText, ':', 2)).split(" ");

		
		try{
		message = receivedText.substring(nthOccurrence(receivedText, ':', 2)+1);
		} catch(IndexOutOfBoundsException e) {
			//TODO This probably means it's not a server wide message?
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
	        	 	case "001": gui.printServerText(server,message);
	        	 				break;
	        	 	//002 = TODO
	        	 	case "002": gui.printServerText(server,message);
	        	 				break;
	        	 	//003 = TODO
	        	 	case "003": gui.printServerText(server,message);
	        	 				break;
	 				//251 = TODO
	        	 	case "251": gui.printServerText(server,message);
	        	 				break;
	        	 	//005 = Server change
		        	case "005" : String tempServer = receivedOptions[0].substring(1);
		        				gui.printServerText(server,">> Changed your server to "+tempServer);
		        	 			break;
    	 			//332 = Channel Topic
		        	case "332" :gui.setChannelTopic(receivedOptions[3], message);
		        				break;
    	 			//372 = Server MOTD
		        	case "372" :gui.printServerText(server,message);
		        				break;
		        	//353 = User List
					//:barjavel.freenode.net 353 matty_r @ #testingonly :@matty_r
		        	case "353" :gui.addToUsersList(receivedOptions[4], message.split(" "));
		        		 		break;
    		 		//470 = Forwarding to another channel
		        	//case "470" :if(tempTextArray[3] == channel)
		        					//channel = tempTextArray[4];
    							//break;
					//473 = Cannot join channel 
		        	case "473" :gui.setCurrentTab(1);
		        				gui.printServerText(server,"!! "+myNick+" unable to join channel.");
		        				Toolkit.getDefaultToolkit().beep();
		        				break;
					//433 = Nickname already in use
		        	case "433" :gui.printServerText(server,"!! "+myNick+" is already in use.");
		        				Toolkit.getDefaultToolkit().beep();
		        				break;
		        	//432 = Nickname is invalid.
		        	case "432" :gui.printServerText(server,"!! "+myNick+" is invalid.");
		        				Toolkit.getDefaultToolkit().beep();
    							break;
		        	//JOIN = When a user joins a channel
		        	case "JOIN":if(extractNick(receivedOptions[0]).equals(myNick)){
					        		gui.addCreatedChannels(receivedOptions[2]);
			    					gui.printEventTicker(receivedOptions[2], "You have joined "+receivedOptions[2]);
					        	} else 
		        					gui.addToUsersList(receivedOptions[2], receivedOptions[0].substring(0, receivedOptions[0].indexOf("!")));
		        				break;
		        	case "PRIVMSG": if(!receivedOptions[2].equals(myNick)){
		        						gui.printChannelText(receivedOptions[2],"<"+extractNick(receivedOptions[0])+"> "+message,extractNick(receivedOptions[0]));
		        					//if my nick was mentioned in a message, make a noise
		        					if(message.contains(myNick))
		        						Toolkit.getDefaultToolkit().beep();
			        				} else{
			        					//TODO Created a IRCPrivate chat room
			        					gui.printChannelText(extractNick(receivedOptions[0]),"{"+extractNick(receivedOptions[0])+"} "+message,extractNick(receivedOptions[0]));
			        					Toolkit.getDefaultToolkit().beep();
			        				}
		        				break;
		        	case "NICK":if(!(extractNick(receivedOptions[0]).equals(myNick)))
									gui.renameUser("Server", extractNick(receivedOptions[0]), message);
		    					break;
		        	case "PART":if(!(extractNick(receivedOptions[0]).equals(myNick))){
		        					for(String tempChannel : receivedOptions[2].split(","))
		        						gui.removeFromUsersList(tempChannel, extractNick(receivedOptions[0]));
		        				}
		        				break;
    				//:macabre_!~ibtjw@23-114-59-64.lightspeed.austtx.sbcglobal.net QUIT :Ping timeout: 252 seconds
		        	case "QUIT":if(!(extractNick(receivedOptions[0]).equals(myNick)))
			    						gui.removeFromUsersList("Server", extractNick(receivedOptions[0]));
			    				break;
		        	case "NOTICE":gui.printServerText(server, message);
		    					break;
		        	case "Link":gui.quitChannels();
		        				gui.quitPrivateRooms();
		        				gui.serverDisconnect();
		        				break;
		        	default:gui.printServerText(server,message);
		        			writeDebugFile("!!!!!!!!!!!!Not Handled!!!!!!!!!!!!");
	        	 			break;
	        	 } else {
	        		gui.printServerText(server,message);
        		 	writeDebugFile("!!!!!!!!!!!!Not Handled!!!!!!!!!!!!");
	        	 }
         }
	}
	
	
	/** Write all messages to the debug.txt file */
	public void writeDebugFile(String message) throws IOException{
		debugFile = debugDateFormat.format(todayDate)+" "+this.server+".log";
		FileWriter fw = new FileWriter (debugFile, true);
		BufferedWriter bw = new BufferedWriter (fw);
		PrintWriter outFile = new PrintWriter (bw);
		outFile.println(debugTimeFormat.format(todayDate)+"> "+message);
		outFile.close();
	}
	
	private String extractNick(String textString){
		if(textString.startsWith(":"))
			return textString.substring(1, textString.indexOf("!"));
		
		return textString.substring(0, textString.indexOf("!"));
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
