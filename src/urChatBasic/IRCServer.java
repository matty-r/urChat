package urChatBasic;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;

public class IRCServer extends JPanel implements IRCActions {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4685985875752613136L;
	  ////////////////
	 //GUI ELEMENTS//
	////////////////
	//Icons
	public ImageIcon icon;

	private UserGUI gui = DriverGUI.gui;
	
	//Server Properties
	private Connection serverConnection; 
	
	//Server Text Area
	private JTextPane serverTextArea = new JTextPane();
	private JScrollPane serverTextScroll = new JScrollPane(serverTextArea);
	public JTextField serverTextBox = new JTextField();
	private String name; 
	
	//Created Private Rooms/Tabs
	private List<IRCPrivate> createdPrivateRooms = new ArrayList<IRCPrivate>();
	//Created channels/tabs
	private List<IRCChannel> createdChannels = new ArrayList<IRCChannel>();
	
	public IRCServer(String serverName,String nick,String login){
		this.setLayout(new BorderLayout());
		this.add(serverTextScroll, BorderLayout.CENTER);
		this.add(serverTextBox, BorderLayout.PAGE_END);
		serverTextBox.addActionListener(new SendServerText());
		serverTextArea.setFont(gui.getFont());
		this.name = serverName;
		
		Image tempIcon = null;
		try {
			tempIcon = ImageIO.read(new File("Resources\\Server.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		icon = new ImageIcon(tempIcon);	
		
		serverConnect(nick,login);
	}
	
	public String getNick(){
		return serverConnection.myNick;
	}
	
	/**
	 * Saves all the information from the text boxes to the connection
	 * 
	 */
	public void serverConnect(String nick,String login){
		try {
			serverConnection = new Connection(IRCServer.this,nick,login);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(serverConnection).start();
	}
	
	@Override
	public String toString(){
		return this.name;
	}

	
	public void setName(String serverName){
		this.name = serverName;
	}
	
	@Override
	public String getName(){
		return this.name;
	}

	/**
	* Check to see if there are any channels at all.
	* @param channelName
	* @return IRCChannel
	*/
	public Boolean isCreatedChannelsEmpty(){
		return createdChannels.isEmpty();
	}

   public IRCUser getIRCUser(String userName){
	   for(IRCChannel tempChannel : createdChannels)
		   if(tempChannel.getCreatedUsers(userName) != null)
		    return tempChannel.getCreatedUsers(userName);
		   
	   return null;
   }
   

   /**
    * Return the appropriate created server
    * @param serverName
    * @return IRCServer
    */
   public IRCPrivate getCreatedPrivateRoom(String privateRoom){
	   //for(int x = 0; x < createdChannels.size(); x++)
	   for(IRCPrivate tempPrivate : createdPrivateRooms)
		   if(tempPrivate.getName().toLowerCase().equals(privateRoom.toLowerCase()))
			   return tempPrivate;
	   return null;
   }
   
    /**
	 * Closes and removes all channels that have been created.
	 */
	public void quitChannels(){
		while(createdChannels.iterator().hasNext()){
			IRCChannel tempChannel = createdChannels.iterator().next();
			createdChannels.remove(tempChannel);
			gui.tabbedPane.remove(tempChannel);
		}
	}
	
	/**
	 * Closes and removes all channels that have been created.
	 */
	public void quitChannel(String channelName){
		if(getCreatedChannel(channelName) != null){
			createdChannels.remove(getCreatedChannel(channelName));
			gui.tabbedPane.remove(gui.getTabIndex(channelName));
		}
	}
	
	/**
	 * Closes and removes all private rooms that have been created.
	 */
	public void quitPrivateRooms(){
		while(createdPrivateRooms.iterator().hasNext()){
			IRCPrivate tempPrivateRoom = createdPrivateRooms.iterator().next();
			gui.tabbedPane.remove(tempPrivateRoom);
			createdPrivateRooms.remove(tempPrivateRoom);
		}
	}
	
	/**
	 * Closes and removes a selected private room that have been created.
	 */
	public void quitPrivateRooms(String roomName){
		if(getCreatedPrivateRoom(roomName) != null){
			createdPrivateRooms.remove(getCreatedPrivateRoom(roomName));
			gui.tabbedPane.remove(gui.getTabIndex(roomName));
		}
	}
	
  /**
   * Return the appropriate created channel
   * @param channelName
   * @return IRCChannel
   */
  public IRCChannel getCreatedChannel(String channelName){
	   for(IRCChannel tempChannel : createdChannels)
		   if(tempChannel.getName().equals(channelName))
			   return tempChannel;
	   return null;
  }
  
   
   /**
    * Creates a new channel based on name
    * @param channelName
    */	
   public void addToCreatedChannels(String channelName){
	   if(getCreatedChannel(channelName) == null){
		IRCChannel tempChannel = new IRCChannel(this, channelName);
	   	createdChannels.add(tempChannel);
	   	gui.tabbedPane.addTab(channelName, tempChannel.icon ,tempChannel);
	   	gui.tabbedPane.setSelectedIndex(gui.tabbedPane.indexOfComponent(tempChannel));
	   	tempChannel.clientTextBox.requestFocus();
	   }
   }
	   
   /**
    * Creates a new Private Room based on name
    * @param serverName
    */
   public void addToPrivateRooms(String privateRoom){
	   if(getCreatedPrivateRoom(privateRoom) == null){
			IRCPrivate tempPrivateRoom = new IRCPrivate(this,getIRCUser(privateRoom));
		   	createdPrivateRooms.add(tempPrivateRoom);
		   	gui.tabbedPane.addTab(tempPrivateRoom.getName(), tempPrivateRoom.icon,tempPrivateRoom);
		   	gui.tabbedPane.setSelectedIndex(gui.tabbedPane.indexOfComponent(tempPrivateRoom));
		   	tempPrivateRoom.privateTextBox.requestFocus();
	   }
   }
	   
   /**
    * Creates a new Private Room based on IRCUser
    * @param serverName
    */
   public void addToPrivateRooms(IRCUser privateRoom){
	   if(getCreatedPrivateRoom(privateRoom.getName()) == null){
			IRCPrivate tempPrivateRoom = new IRCPrivate(this,privateRoom);
		   	createdPrivateRooms.add(tempPrivateRoom);
		   	gui.tabbedPane.addTab(tempPrivateRoom.getName(), tempPrivateRoom.icon,tempPrivateRoom);
		   	gui.tabbedPane.setSelectedIndex(gui.tabbedPane.indexOfComponent(tempPrivateRoom));
		   	tempPrivateRoom.privateTextBox.requestFocus();
	   }
   }
   
	/**
	 * Prints the text to the appropriate channels main text window.
	 * @param channelName
	 * @param line
	 */
	public void printChannelText(String channelName, String line, String fromUser){
		if(channelName.equals(fromUser)){
			printPrivateText(channelName,line);
			//addToPrivateRooms(channelName);
			//getCreatedPrivateRoom(channelName).printText(isTimeStampsEnabled(), line);
		} else
			getCreatedChannel(channelName).printText(gui.isTimeStampsEnabled(),line,fromUser);
	}
	
	/**
	 * Prints the text to the appropriate channels main text window. Checks the user
	 * exists first and if they are muted else if they don't exist then just create it
	 * and print the private text.
	 * @param channelName
	 * @param line
	 */
	public void printPrivateText(String userName, String line){
		if(getIRCUser(userName) != null && !getIRCUser(userName).isMuted()){
			if(getCreatedPrivateRoom(userName) == null)
				addToPrivateRooms(getIRCUser(userName));
			getCreatedPrivateRoom(userName).printText(gui.isTimeStampsEnabled(),line);
			if(gui.getTabIndex(userName) != gui.tabbedPane.getSelectedIndex())
				Toolkit.getDefaultToolkit().beep();
		} else if (getIRCUser(userName) == null){
			if(getCreatedPrivateRoom(userName) == null)
				addToPrivateRooms(new IRCUser(this,userName));
			getCreatedPrivateRoom(userName).printText(gui.isTimeStampsEnabled(),line);
			if(gui.getTabIndex(userName) != gui.tabbedPane.getSelectedIndex())
				Toolkit.getDefaultToolkit().beep();
		}
	}
	
	public void printServerText(String line){
		try{
		this.printText(gui.isTimeStampsEnabled(),line);
		} catch(Exception e){
			//TODO something here
		}
	}
	
	public void printEventTicker(String channelName, String eventText){
		getCreatedChannel(channelName).createEvent(eventText);
	}
	

	//Adds users to the list in the users array[]
	public void addToUsersList(String channelName,String[] users){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				if(!channelName.matches("Server")){
					IRCChannel tempChannel = getCreatedChannel(channelName);
					if(tempChannel != null)
						tempChannel.addToUsersList(tempChannel.getName(), users);
				}
			}
		});
	}
	
	//Adds a single user, good for when a user joins the channel
	public void addToUsersList(String channelName,String user){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				String thisUser = user;
				if(user.startsWith(":"))
					thisUser = user.substring(1);
				
				IRCChannel tempChannel = getCreatedChannel(channelName);
				if(tempChannel != null)
					tempChannel.addToUsersList(tempChannel.getName(), thisUser);
			}
		});
	}
		

	/**
	 * Removes a single user from the specified channel. If the call is from "Server"
	 * as the channelName it will loop through all createdChannels and remove the user.
	 * But only if they were in there to begin with.
	 * @param channelName
	 * @param user
	 */
	public void removeFromUsersList(String channelName,String user){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				String thisUser = user;
					if(user.startsWith(":"))
						thisUser = user.substring(1);
						
					if(channelName == "Server"){
						for(IRCChannel tempChannel : createdChannels){
								tempChannel.removeFromUsersList(tempChannel.getName(), thisUser);
						}
					} else {
						IRCChannel tempChannel = getCreatedChannel(channelName);
						if(tempChannel != null)
							if(thisUser.equals(serverConnection.getNick()))
								quitChannel(channelName);
							else
								tempChannel.removeFromUsersList(channelName, thisUser);
					}
				}
		});
	}

	
	private class SendServerText implements ActionListener
	   {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if(!serverTextBox.getText().trim().isEmpty())
						serverConnection.sendClientText(serverTextBox.getText(),getName());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				serverTextBox.setText("");
			}
	   }
	
	public void sendClientText(String line,String source){
		try {
			if(serverConnection != null)
				serverConnection.sendClientText(line, source);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void doLimitLines(){
		if(gui.isLimitedServerActivity()){
			String[] tempText = serverTextArea.getText().split("\n");
			int linesCount = tempText.length;
			
			if(linesCount >= gui.getLimitServerLinesCount()){
				String newText =  serverTextArea.getText().replace(tempText[0]+"\n", "");
				serverTextArea.setText(newText);
			}
		}
	}
	
	public void printText(Boolean dateTime, String line){
		doLimitLines();
		
		StyledDocument doc = (StyledDocument) serverTextArea.getDocument();
		Style style = doc.addStyle("StyleName", null);
	
	   // StyleConstants.setItalic(style, true);
		DateFormat chatDateFormat = new SimpleDateFormat("HHmm");
		Date chatDate = new Date();
		
		if(dateTime)
			line = "["+chatDateFormat.format(chatDate)+"] " + line;
	    try {
			doc.insertString(doc.getLength(), line+"\n", style);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		serverTextArea.setCaretPosition(serverTextArea.getDocument().getLength());
	}
	
	
	public String getChannelTopic(String channelName) {
		return getCreatedChannel(channelName).getChannelTopic();
	}

	public void setChannelTopic(String channelName,String channelTopic) {
		getCreatedChannel(channelName).setChannelTopic(channelTopic);
	}

	@Override
	/**
	 * This is a forwarding method used to direct the call to the IRCChannel,
	 * filters through 
	 * @param channelName
	 * @param user
	 * @param newUser
	 */
	public void renameUser(String oldUserName,String newUserName){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
						//if(oldUserName.startsWith(":"))
							//oldUserName = oldUserName.substring(1);
						
						for(IRCChannel tempChannel : createdChannels){
							tempChannel.renameUser(oldUserName.replace(":", ""), newUserName);
						}
					}
		});
	}

	@Override
	public String getServer() {
		return this.getName();
	}

}
