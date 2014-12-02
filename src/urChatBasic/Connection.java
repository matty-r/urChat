package urChatBasic;

import java.awt.Toolkit;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Connection implements Runnable{

	private static BufferedWriter writer;
	private static BufferedReader reader;
	
	public static String server = null;
	public static String myNick = null;
	public static String login = null;
	public static String firstChannel = null;
	private Socket mySocket = null;
	public static UserGUI gui = DriverGUI.gui;
	
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
    	
    	
        
        DriverGUI.gui.printText("Server","Attempting to connect to "+server,"Server");
        mySocket = new Socket(server, 6667);
        receivedFromServer("Server","Connected.");
        
		writer = new BufferedWriter(
                new OutputStreamWriter(mySocket.getOutputStream( )));
        reader = new BufferedReader(
                new InputStreamReader(mySocket.getInputStream( ),StandardCharsets.UTF_8));

        // Log on to the server.
        writer.write("NICK " + myNick + "\r\n");
        writer.write("USER " + login + " 8 * : "+myNick+"\r\n");
        receivedFromServer("Server","Connect with nick "+myNick);
        writer.flush( );
        
        // Read lines from the server until it tells us we have connected.
        String line = null;
        while ((line = reader.readLine()) != null) {
        	if (line.indexOf("004") >= 0) {
                //Logged in successfully
        		receivedFromServer("Server","Logged in successfully.");
                break;
            } else
            	receivedFromServer("Server",line);
        }

        // Join the channel.
        writer.write("JOIN " + firstChannel + "\r\n");
        writer.flush();
        gui.printText("Server", "Connecting to channel "+firstChannel,"Server"); 
        
        // Keep reading lines from the server.
        while ((line = reader.readLine()) != null) {
                // Print the raw line received by the bot.
                receivedFromServer("Server",line);
        }
        
        mySocket.close();
    	gui.printText("Server", "Socket has closed.","Server");
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
				DriverGUI.gui.printText(fromChannel, "<"+myNick+"> "+clientText,myNick);
			}
		writer.flush();
		}
	}

	/**
	 * Depending on the client window depends on where to show the text.
	 * @param channel
	 * @param serverText
	 * @throws IOException
	 */
	public void receivedFromServer(String channel, String serverText) throws IOException {
		String[] tempTextArray = null;
		tempTextArray = serverText.split(" ");
		
		 if (serverText.toLowerCase( ).startsWith("ping ")) {
             // We must respond to PINGs to avoid being disconnected.
             writer.write("PONG " + serverText.substring(5) + "\r\n");
             writer.flush();
         }
         else {
        	 if(tempTextArray.length > 1)
	        	 switch(tempTextArray[1]){
	        	 	//005 = Server change
		        	case "005" :server = tempTextArray[0].substring(1);
		        				DriverGUI.gui.printText("Server",">> Changed your server to "+server,"Server");
		        	 			break;
    	 			//332 = Channel Topic
		        	case "332" :String incomingTopic = "";
			    				for(int x = 4; x < tempTextArray.length; x++)
			    					incomingTopic += " "+tempTextArray[x];
			    				if(incomingTopic.length() > 3)
			    					gui.setChannelTopic(tempTextArray[3], incomingTopic.substring(1));
		        				break;
		        	//353 = User List
		        	case "353" :gui.addToUsersList(tempTextArray[4], tempTextArray);
		        		 		break;
    		 		//470 = Forwarding to another channel
		        	case "470" :if(tempTextArray[3] == channel)
		        					channel = tempTextArray[4];
    							break;
					//473 = Cannot join channel 
		        	case "473" :gui.setCurrentTab(1);
		        				gui.printText(channel,"!! "+myNick+" unable to join channel.",myNick);
		        				Toolkit.getDefaultToolkit().beep();
		        				break;
					//433 = Nickname already in use
		        	case "433" :gui.printText(channel,"!! "+myNick+" is already in use.",myNick);
		        				Toolkit.getDefaultToolkit().beep();
		        				break;
		        	//432 = Nickname is invalid.
		        	case "432" :gui.printText(channel,"!! "+myNick+" is invalid.",myNick);
		        				Toolkit.getDefaultToolkit().beep();
    							break;
		        	//JOIN = When a user joins a channel
		        	case "JOIN":if(extractNick(tempTextArray[0]).equals(myNick)){
		        					gui.addCreatedChannels(tempTextArray[2].replace(":", ""));
		        					gui.printEventTicker(tempTextArray[2].replace(":",""), "You have joined "+tempTextArray[2]);
					        	} else
		        					gui.addToUsersList(tempTextArray[2], tempTextArray[0].substring(0, tempTextArray[0].indexOf("!")));
		        				break;
		        	case "PRIVMSG": String incomingMessage = "";
		        				for(int x = 3; x < tempTextArray.length; x++)
		        					incomingMessage += " "+tempTextArray[x];
		        				if(!tempTextArray[2].contains(myNick)){
		        						gui.printText(tempTextArray[2],"<"+extractNick(tempTextArray[0])+"> "+incomingMessage.substring(2),extractNick(tempTextArray[0]));
		        						
		        					//if my nick was mentioned in a message, make a noise
		        					if(incomingMessage.contains(myNick))
		        						Toolkit.getDefaultToolkit().beep();
		        				} else{
		        					gui.printText("Server","{"+extractNick(tempTextArray[0])+"} "+incomingMessage.substring(2),extractNick(tempTextArray[0]));
		        					Toolkit.getDefaultToolkit().beep();
		        				}
		        				break;
		        	case "PART":if(!(extractNick(tempTextArray[0]).equals(myNick))){
		        					for(String tempChannel : tempTextArray[2].split(","))
		        						gui.removeFromUsersList(tempChannel, tempTextArray[0].substring(0, tempTextArray[0].indexOf("!")));
		        				}
		        				break;
		        	case "QUIT":if(!(tempTextArray[0].contains(myNick)))
    								gui.removeFromUsersList(channel, tempTextArray[0].substring(0, tempTextArray[0].indexOf("!")));
    							break;
		        	case "NICK":if(!(tempTextArray[0].contains(myNick)))
									gui.renameUser(channel, tempTextArray[0].substring(0, tempTextArray[0].indexOf("!")), tempTextArray[tempTextArray.length-1].substring(1));
		    					break;
		        	case "NOTICE":gui.printText(channel, serverText,channel);
		    					break;
		        	case ":Closing": gui.quitChannels();
		        					 gui.serverDisconnect();
		        					 gui.printText(channel,"Quit server.",channel);
		        				break;
		        	default: gui.printText(channel,serverText,channel);
		        	 			break;
	        	 }
        	 else
        		 gui.printText(channel,serverText,channel);
         }
	}

	private String extractNick(String textString){
		return textString.substring(1, textString.indexOf("!"));
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
