package urChatBasic;

import java.awt.*;
import java.util.prefs.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;

public class UserGUI extends JPanel implements Runnable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2595649865577419300L;
	//Main Panel
	final private int MAIN_WIDTH = 800;
	final private int MAIN_HEIGHT = 600;
	
	//Tabs
	private JTabbedPane tabbedPane = new JTabbedPane();
	private final int OPTIONS_INDEX = 0;
	
	//Options Panel
	private JPanel optionsMainPanel = new JPanel();
	private JPanel optionsLeftPanel = new JPanel();
	private JPanel optionsRightPanel = new JPanel();
	private DefaultListModel<String> optionsArray = new DefaultListModel<String>();
	private JList<String> optionsList = new JList<String>(optionsArray);
	private JCheckBox showJoinsQuitsEventTicker = new JCheckBox("Show Joins/Quits in the Event Ticker");
	private JCheckBox logChannelText = new JCheckBox("Save and log all channel text");
	private JCheckBox logClientText = new JCheckBox("Log client text (Allows up or down history)");
	private JCheckBox enableTimeStamps = new JCheckBox("Time Stamp chat messages");
	private JButton connectButton = new JButton("Connect");
	private static final int TICKER_DELAY_MIN = 1;
	private static final int TICKER_DELAY_MAX = 30;
	private static final int TICKER_DELAY_INIT = 20; 
	private JSlider eventTickerDelay = new JSlider(JSlider.HORIZONTAL,TICKER_DELAY_MIN, TICKER_DELAY_MAX, TICKER_DELAY_INIT);
	private JTextField usernameTextField = new JTextField("");
	private JTextField servernameTextField = new JTextField("");
	private JTextField firstChannelTextField = new JTextField("");
	private JButton saveSettings = new JButton("Save Settings");
	private Preferences clientSettings;
	public static Connection myConnection = DriverGUI.chatSession;
	
	//Server Panel
	//private JPanel serverPanel = new JPanel();
	//Server panel is now created per server
	
	
	//Created channels/tabs
	private ArrayList<IRCChannel> createdChannels = new ArrayList<IRCChannel>();
	
	//Created Servers/Tabs
	private ArrayList<IRCServer> createdServers = new ArrayList<IRCServer>();
	
	//Created Private Rooms/Tabs
	private ArrayList<IRCPrivate> createdPrivateRooms = new ArrayList<IRCPrivate>();
 
	/**
	 * Sets the tab to the index number
	 * @param indexNum
	 */
	public void setCurrentTab(int indexNum){
		tabbedPane.setSelectedIndex(indexNum);
	}
	
	/**
	 * Sets the tab to the name
	 * @param indexNum
	 */
	public void setCurrentTab(String tabName){
		for(int x = 0; x < tabbedPane.getTabCount()-1; x++)
			if(tabbedPane.getTabComponentAt(x).getName().matches(tabName))
				tabbedPane.setSelectedIndex(x);
	}
	/**
	 * Returns a tabs Index by name
	 * @param tabName
	 * @return int
	 */
	public int getTabIndex(String tabName){
		for(int x = 0; x < tabbedPane.getTabCount(); x++){
			if(!tabbedPane.getTitleAt(x).equals(null))
				if(tabbedPane.getTitleAt(x).equals(tabName))
					return x;
		}
		return -1;
	}
	
	public Boolean saveChannelHistory(){
		if(logChannelText.isSelected())
			return true;
		
		return false;
	}
	
	/**
	 * Closes and removes all channels that have been created.
	 */
	public void quitChannels(){
		while(createdChannels.iterator().hasNext()){
			IRCChannel tempChannel = createdChannels.iterator().next();
			createdChannels.remove(tempChannel);
			tabbedPane.remove(tempChannel);
		}
	}
	
	/**
	 * Closes and removes all channels that have been created.
	 */
	public void quitChannels(String channelName){
		if(getCreatedChannels(channelName) != null){
			createdChannels.remove(getCreatedChannels(channelName));
			tabbedPane.remove(getTabIndex(channelName));
		}
	}
	
	/**
	 * Closes and removes all private rooms that have been created.
	 */
	public void quitPrivateRooms(){
		while(createdPrivateRooms.iterator().hasNext()){
			IRCPrivate tempPrivateRoom = createdPrivateRooms.iterator().next();
			tabbedPane.remove(tempPrivateRoom);
			createdPrivateRooms.remove(tempPrivateRoom);
		}
	}
	
	/**
	 * Closes and removes a selected private room that have been created.
	 */
	public void quitPrivateRooms(String roomName){
		if(getCreatedPrivateRooms(roomName) != null){
			createdPrivateRooms.remove(getCreatedPrivateRooms(roomName));
			tabbedPane.remove(getTabIndex(roomName));
		}
	}
	
   /**
    * Return the appropriate created channel
    * @param channelName
    * @return IRCChannel
    */
   public IRCChannel getCreatedChannels(String channelName){
	   //for(int x = 0; x < createdChannels.size(); x++)
	   for(IRCChannel tempChannel : createdChannels)
		   if(tempChannel.getName().equals(channelName))
			   return tempChannel;
	   return null;
   }
   
   /**
    * Return the appropriate created server
    * @param serverName
    * @return IRCServer
    */
   public IRCServer getCreatedServers(String serverName){
	   //for(int x = 0; x < createdChannels.size(); x++)
	   for(IRCServer tempServer : createdServers)
		   if(tempServer.getName().equals(serverName))
			   return tempServer;
	   return null;
   }
   
   /**
    * Return the appropriate created server
    * @param serverName
    * @return IRCServer
    */
   public IRCPrivate getCreatedPrivateRooms(String privateRoom){
	   //for(int x = 0; x < createdChannels.size(); x++)
	   for(IRCPrivate tempPrivate : createdPrivateRooms)
		   if(tempPrivate.getName().equals(privateRoom))
			   return tempPrivate;
	   return null;
   }
   
   /**
    * Check to see if there are any channels at all.
    * @param channelName
    * @return IRCChannel
    */
   public Boolean isCreatedChannelsEmpty(){
	   if(createdChannels.isEmpty())
		   return true;
	   
	   return false;
   }
   
   /**
    * Check to see if there are any Servers at all.
    * @param channelName
    * @return IRCChannel
    */
   public Boolean isCreatedServersEmpty(){
	   if(createdServers.isEmpty())
		   return true;
	   
	   return false;
   }
   
   public IRCUser getIRCUser(String userName){
	   for(IRCChannel tempChannel : createdChannels)
		   return tempChannel.getCreatedUsers(userName);
		   
	   return null;
   }
   
   /**
    * Creates a new channel based on name
    * @param channelName
    */
   public void addCreatedChannels(String channelName){
	   
	   if(getCreatedChannels(channelName) == null){
		IRCChannel tempChannel = new IRCChannel(channelName);
	   	createdChannels.add(tempChannel);
	   	tabbedPane.addTab(channelName, tempChannel.icon ,tempChannel);
	   	tabbedPane.setSelectedIndex(tabbedPane.indexOfComponent(tempChannel));
	   	tempChannel.clientTextBox.requestFocus();
	   }
   }
   
   /**
    * Creates a new server based on name
    * @param serverName
    */
   public void addCreatedServers(String serverName){
	   
	   if(getCreatedServers(serverName) == null){
		IRCServer tempServer = new IRCServer(serverName);
	   	createdServers.add(tempServer);
	   	tabbedPane.addTab(serverName, tempServer.icon,tempServer);
	   	tabbedPane.setSelectedIndex(tabbedPane.indexOfComponent(tempServer));
	   	tempServer.serverTextBox.requestFocus();
	   }
   }
   
   /**
    * Creates a new server based on name
    * @param serverName
    */
   public void addPrivateRooms(String privateRoom){
	   if(getCreatedPrivateRooms(privateRoom) == null){
			IRCPrivate tempPrivateRoom = new IRCPrivate(getIRCUser(privateRoom));
		   	createdPrivateRooms.add(tempPrivateRoom);
		   	tabbedPane.addTab(tempPrivateRoom.getName(), tempPrivateRoom.icon,tempPrivateRoom);
		   	tabbedPane.setSelectedIndex(tabbedPane.indexOfComponent(tempPrivateRoom));
		   	tempPrivateRoom.privateTextBox.requestFocus();
	   }
   }
   
   /**
    * Creates a new server based on IRCUser
    * @param serverName
    */
   public void addPrivateRooms(IRCUser privateRoom){
	   if(getCreatedPrivateRooms(privateRoom.getName()) == null){
			IRCPrivate tempPrivateRoom = new IRCPrivate(privateRoom);
		   	createdPrivateRooms.add(tempPrivateRoom);
		   	tabbedPane.addTab(tempPrivateRoom.getName(), tempPrivateRoom.icon,tempPrivateRoom);
		   	tabbedPane.setSelectedIndex(tabbedPane.indexOfComponent(tempPrivateRoom));
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
			addPrivateRooms(channelName);
			getCreatedPrivateRooms(channelName).printText(addTimeStamp(), line);
		} else
			getCreatedChannels(channelName).printText(addTimeStamp(),line,fromUser);
	}
	
	/**
	 * Prints the text to the appropriate channels main text window.
	 * @param channelName
	 * @param line
	 */
	public void printPrivateText(String userName, String line){
			addPrivateRooms(new IRCUser(userName));
			getCreatedPrivateRooms(userName).printText(addTimeStamp(),line);
	}
	
	public void printServerText(String serverName, String line){
		try{
		getCreatedServers(serverName).printText(addTimeStamp(),line);
		} catch(Exception e){
			//TODO something here
		}
	}
	
	public void printEventTicker(String channelName, String eventText){
		getCreatedChannels(channelName).tickerPanelAddEventLabel(eventText);
	}
	

	
	private void setupRightOptionsPanel(){
		ListSelectionModel listSelectionModel = optionsList.getSelectionModel();
	    listSelectionModel.addListSelectionListener(
	                            new SharedListSelectionHandler());
	    
		optionsRightPanel.setBackground(Color.BLACK);
		optionsRightPanel.setLayout(new CardLayout());
		JPanel serverPanel = new JPanel();
		serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.PAGE_AXIS));
		serverPanel.add(new JLabel("Nick:"));
		usernameTextField.setMaximumSize(new Dimension(250,20));
		serverPanel.add(usernameTextField);
		serverPanel.add(Box.createRigidArea(new Dimension(0,5)));
		serverPanel.add(new JLabel("Server name:"));
		servernameTextField.setMaximumSize(new Dimension(250,20));
		serverPanel.add(servernameTextField);
		serverPanel.add(Box.createRigidArea(new Dimension(0,5)));
		serverPanel.add(new JLabel("Channel:"));
		firstChannelTextField.setMaximumSize(new Dimension(250,20));
		serverPanel.add(firstChannelTextField);
		serverPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		serverPanel.add(Box.createRigidArea(new Dimension(0,20)));
		serverPanel.add(connectButton);
		connectButton.addActionListener(new connectPressed());
						
        JPanel clientPanel = new JPanel();
        clientPanel.setLayout(new BoxLayout(clientPanel,BoxLayout.PAGE_AXIS));
        clientPanel.add(showJoinsQuitsEventTicker);
        showJoinsQuitsEventTicker.setSelected(true);
        clientPanel.add(logChannelText);
        logChannelText.setSelected(true);
        clientPanel.add(logClientText);
        logClientText.setSelected(true);
        clientPanel.add(enableTimeStamps);
        enableTimeStamps.setSelected(true);
        //Turn on labels at major tick marks.
        eventTickerDelay.setMajorTickSpacing(10);
        eventTickerDelay.setMinorTickSpacing(1);
        eventTickerDelay.setPaintTicks(true);
        eventTickerDelay.setPaintLabels(true);
        eventTickerDelay.setMaximumSize(new Dimension(400,40));
        clientPanel.add(Box.createRigidArea(new Dimension(0,5)));
        clientPanel.add(new JLabel("Event Ticker movement delay (Lower is faster)"));
        clientPanel.add(Box.createRigidArea(new Dimension(0,5)));
        clientPanel.add(eventTickerDelay);
        clientPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        clientPanel.add(saveSettings);
        saveSettings.addActionListener( new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent arg0) {
												setClientSettings();
											}
								        });
        optionsRightPanel.add(serverPanel, "Server");
        optionsRightPanel.add(clientPanel, "Client");
	}
	
	/**
	 * Used to initiate server connection
	 * @author Matt
	 *
	 */
	private class connectPressed implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(connectButton.getText() == "Connect"){
				serverConnect();
				connectButton.setText("Disconnect");
				} else {
					try {
						Connection.sendClientText("/quit Goodbye cruel world", "Server");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		}
	}
	
	/**
	 * Saves all the information from the text boxes to the connection
	 * 
	 */
	public void serverConnect(){
		Connection.server = servernameTextField.getText();
		Connection.myNick =  usernameTextField.getText();
		Connection.login =  usernameTextField.getText();
		Connection.firstChannel = firstChannelTextField.getText();
		
		//addCreatedServers();
		
		new Thread(new Runnable() {
			   public void run() {
				  // DriverGUI.chatSession =  new Connection();
				   
				   DriverGUI.startConnection();
			   }
			}).start();
	}
	
	/**
	 * Remove and disconnect all private rooms, channels and servers
	 */
	public void shutdownAll(){
		if(!isCreatedServersEmpty()){
			quitChannels();
			quitPrivateRooms();
			quitServers();
			connectButton.setText("Connect");
		}
	}
	
	/**
	 * Loops through all servers and disconnects
	 * and deletes the tab
	 */
	public void quitServers(){
		while(createdServers.iterator().hasNext()){
			IRCServer tempServer = createdServers.iterator().next();
			tabbedPane.remove(tempServer);
			createdServers.remove(tempServer);
		}
	}
	/**
	 * Houses the options list
	 */
	private void setupLeftOptionsPanel(){
		optionsLeftPanel.setBackground(Color.RED);
		optionsLeftPanel.setPreferredSize(new Dimension(100,0));
		optionsLeftPanel.setLayout(new BorderLayout());
		optionsLeftPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		optionsLeftPanel.add(optionsList);
	}
	
	/**
	 * Saves the settings into the registry/Settings API
	 */
	private void setClientSettings(){
		clientSettings.put("FIRST_CHANNEL", firstChannelTextField.getText());
		clientSettings.put("SERVER_NAME", servernameTextField.getText());
		clientSettings.put("NICK_NAME", usernameTextField.getText());
		clientSettings.putBoolean("TIME_STAMPS", enableTimeStamps.isSelected());
		clientSettings.putBoolean("EVENT_TICKER_JOINS_QUITS", showJoinsQuitsEventTicker.isSelected());
		clientSettings.putBoolean("LOG_CHANNEL_HISTORY", logChannelText.isSelected());
		clientSettings.putBoolean("LOG_CLIENT_TEXT", logClientText.isSelected());
		clientSettings.putInt("EVENT_TICKER_DELAY", eventTickerDelay.getValue());
	}
	
	/**
	 * Loads the settings from the registry/Settings API
	 */
	private void getClientSettings(){
		firstChannelTextField.setText(clientSettings.get("FIRST_CHANNEL",""));
		servernameTextField.setText(clientSettings.get("SERVER_NAME", ""));
		usernameTextField.setText(clientSettings.get("NICK_NAME", ""));
		enableTimeStamps.setSelected(clientSettings.getBoolean("TIME_STAMPS", false));
		showJoinsQuitsEventTicker.setSelected(clientSettings.getBoolean("EVENT_TICKER_JOINS_QUITS", false));
		logChannelText.setSelected(clientSettings.getBoolean("LOG_CHANNEL_HISTORY", false));
		logClientText.setSelected(clientSettings.getBoolean("LOG_CLIENT_TEXT", false));
		eventTickerDelay.setValue(clientSettings.getInt("EVENT_TICKER_DELAY", 0));
	}
	
	
	public int getEventTickerDelay(){
		return eventTickerDelay.getValue();
	}
	
	/**
	 * Useds to change which panel to show when you choose an option
	 * Client or Server item in the list
	 * @author Matt
	 *
	 */
    class SharedListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            
            if (!(lsm.isSelectionEmpty())){
                // Find out which indexes are selected.
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                    	CardLayout cl = (CardLayout)(optionsRightPanel.getLayout());
                        cl.show(optionsRightPanel, (String)optionsArray.getElementAt(i));
                    }
                }
            }
        }
    }
    
	private void setupOptionsPanel(){
		optionsMainPanel.setLayout(new BorderLayout());
		
		optionsArray.addElement("Server");
		optionsArray.addElement("Client");
		
		setupLeftOptionsPanel();
		setupRightOptionsPanel();

		optionsMainPanel.add(optionsLeftPanel, BorderLayout.LINE_START);
		optionsMainPanel.add(optionsRightPanel, BorderLayout.CENTER);
		optionsList.setSelectedIndex(OPTIONS_INDEX);
	}
	
	private void setupTabbedPane(){
		tabbedPane.addChangeListener(new mainTabbedPanel_changeAdapter(this));
		tabbedPane.addMouseListener(new TabbedMouseListener());
		setupOptionsPanel();
		tabbedPane.addTab("Options",optionsMainPanel);
		
	}
	/**
	 * Used to listen to the right click on the tabs so and determine
	 * what type we clicked on and exit it.
	 * @author Matt
	 *
	 */
   private class TabbedMouseListener extends MouseInputAdapter {
	   public void mouseClicked(MouseEvent e) {
		   String tabName = tabbedPane.getSelectedComponent().getName();
		   //BUTTON3 is right-click
	       if(e.getButton() == MouseEvent.BUTTON3){
	    	   if(getCreatedChannels(tabName) != null)
					try {
						Connection.sendClientText("/part i'm outta here", tabName);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    	   else
	    		   if(getCreatedPrivateRooms(tabName) != null)
						quitPrivateRooms(tabName);
	    		   else
	    			   if(getCreatedServers(tabName) != null){
	    				   try {
							Connection.sendClientText("/quit", tabName);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	    			   }
	       }
	    }
	   

   }
	
	//Sets focus to the clientTextBox when tab is changed to a channel
	private void mainTabbedPanel_stateChanged(ChangeEvent e) {
	    JTabbedPane tabSource = (JTabbedPane) e.getSource();
	    if(tabSource.getSelectedIndex() > -1){
		    String tab = tabSource.getTitleAt(tabSource.getSelectedIndex());
		    if(getCreatedChannels(tab) != null)
		    	getCreatedChannels(tab).clientTextBox.requestFocus();
		    else
		    if(getCreatedServers(tab) != null)
		    	getCreatedServers(tab).serverTextBox.requestFocus();
		    else
	    	if(getCreatedPrivateRooms(tab) != null)
	    		getCreatedPrivateRooms(tab).privateTextBox.requestFocus();
	    }
	  }
	
	class mainTabbedPanel_changeAdapter implements javax.swing.event.ChangeListener {
		  UserGUI adaptee;
		  mainTabbedPanel_changeAdapter(UserGUI adaptee) {
		    this.adaptee = adaptee;
		  }
		  public void stateChanged(ChangeEvent e) {
		    adaptee.mainTabbedPanel_stateChanged(e);
		  }
		}
	
	
	public UserGUI(){
		//Create the initial size of the panel
		setupTabbedPane();
		this.setLayout(new BorderLayout());
		this.add(tabbedPane, BorderLayout.CENTER);
	    //Set size of the overall panel
	    this.setPreferredSize (new Dimension(MAIN_WIDTH, MAIN_HEIGHT));
	    this.setBackground(Color.gray);
	    clientSettings = Preferences.userNodeForPackage(this.getClass());
	    getClientSettings();
	 }

	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
	}

	//Adds users to the list in the users array[]
	public void addToUsersList(String channelName,String[] users){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				if(!channelName.matches("Server")){
					IRCChannel tempChannel = getCreatedChannels(channelName);
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
				
				IRCChannel tempChannel = getCreatedChannels(channelName);
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
						IRCChannel tempChannel = getCreatedChannels(channelName);
						if(tempChannel != null)
							if(thisUser.equals(Connection.myNick))
								quitChannels(channelName);
							else
								tempChannel.removeFromUsersList(channelName, thisUser);
					}
				}
		});
	}

	public Boolean getJoinsQuitsTicker(){
		if(showJoinsQuitsEventTicker.isSelected())
			return true;
		
		return false;
	}
	
	public Boolean getChannelHistory(){
		if(logChannelText.isSelected())
			return true;
		
		return false;
	}
	
	public Boolean addTimeStamp(){
		if(enableTimeStamps.isSelected())
			return true;
		
		return false;
	}
	
	public Boolean getClientHistory(){
		if(logClientText.isSelected())
			return true;
		
		return false;
	}
	
	
	public String getChannelTopic(String channelName) {
		return getCreatedChannels(channelName).getChannelTopic();
	}

	public void setChannelTopic(String channelName,String channelTopic) {
		getCreatedChannels(channelName).setChannelTopic(channelTopic);
	}
	
	/**
	 * This is a forwarding method used to direct the call to the IRCChannel,
	 * filters through 
	 * @param channelName
	 * @param user
	 * @param newUser
	 */
	public void renameUser(String channelName,String oldUserName,String newUserName){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				String thisoldUser = oldUserName;
				String thisnewUser = newUserName;
						if(oldUserName.startsWith(":"))
							thisoldUser = oldUserName.substring(1);
						
						if(channelName == "Server"){
							for(IRCChannel tempChannel : createdChannels){
								tempChannel.renameUserUsersList(tempChannel.getName(), thisoldUser, thisnewUser);
							}
						} else {
							IRCChannel tempChannel = getCreatedChannels(channelName);
							if(tempChannel != null)
								tempChannel.renameUserUsersList(tempChannel.getName(), thisoldUser, thisnewUser);
						}
					}
		});
	}
	
	/**Clear the users list*/
	public void clearUsersList(String channelName){
		IRCChannel tempChannel = getCreatedChannels(channelName);
		if(tempChannel != null)
			tempChannel.clearUsersList(tempChannel.getName());
	}
	
	@Override
	public void run() {
		// Auto-generated method stub
	}
}