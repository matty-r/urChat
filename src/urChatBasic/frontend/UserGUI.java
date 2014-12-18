package urChatBasic.frontend;

import java.awt.*;
import java.util.logging.Level;
import java.util.prefs.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import urChatBasic.base.Constants;
import urChatBasic.base.IRCServerBase;
import urChatBasic.base.UserGUIBase;

public class UserGUI extends JPanel implements Runnable, UserGUIBase{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2595649865577419300L;
	//Main Panel
	final private int MAIN_WIDTH = 800;
	final private int MAIN_HEIGHT = 600;

	//Tabs
	public JTabbedPane tabbedPane = new JTabbedPane();
	private final int OPTIONS_INDEX = 0;

	//Options Panel
	private JPanel optionsMainPanel = new JPanel();
	private JPanel optionsLeftPanel = new JPanel();
	private DefaultListModel<String> optionsArray = new DefaultListModel<String>();
	private JList<String> optionsList = new JList<String>(optionsArray);

	private JPanel optionsRightPanel = new JPanel();
	private Preferences clientSettings;



	//Client Options Panel
	private JPanel optionsClientPanel = new JPanel();
	private JCheckBox showEventTicker = new JCheckBox("Show Event Ticker");
	private JCheckBox showUsersList = new JCheckBox("Show Users List");
	private JCheckBox showJoinsQuitsEventTicker = new JCheckBox("Show Joins/Quits in the Event Ticker");
	private JCheckBox showJoinsQuitsMainWindow = new JCheckBox("Show Joins/Quits in the Chat Window");
	private JCheckBox logChannelText = new JCheckBox("Save and log all channel text");
	private JCheckBox logServerActivity = new JCheckBox("Save and log all Server activity");
	private JCheckBox logClientText = new JCheckBox("Log client text (Allows up or down history)");
	private JCheckBox limitServerLines = new JCheckBox("Limit the number of lines in Server activity");
	private JCheckBox limitChannelLines = new JCheckBox("Limit the number of lines in channel text");
	private JCheckBox enableTimeStamps = new JCheckBox("Time Stamp chat messages");
	//private JCheckBox enableClickableLinks = new JCheckBox("Make links clickable");

	private JTextField limitServerLinesCount = new JTextField();
	private JTextField limitChannelLinesCount = new JTextField();

	private static final int TICKER_DELAY_MIN = 1;
	private static final int TICKER_DELAY_MAX = 30;
	private static final int TICKER_DELAY_INIT = 20; 
	private static final int DEFAULT_LINES_LIMIT = 1000;
	private JSlider eventTickerDelay = new JSlider(JSlider.HORIZONTAL,TICKER_DELAY_MIN, TICKER_DELAY_MAX, TICKER_DELAY_INIT);

	private JButton saveSettings = new JButton("Save Settings");

	//Server Options Panel
	private JPanel serverOptionsPanel = new JPanel();
	private JTextField usernameTextField = new JTextField("");
	private JTextField servernameTextField = new JTextField("");
	private JTextField firstChannelTextField = new JTextField("");
	private JButton connectButton = new JButton("Connect");

	//Favourites Panel
	private JPanel optionsFavouritesPanel = new JPanel();
	private JCheckBox autoConnectToFavourites = new JCheckBox("Automatically connect to favourites");
	private DefaultListModel<FavouritesItem> favouritesListModel = new DefaultListModel<FavouritesItem>();
	private JList<FavouritesItem> favouritesList = new JList<FavouritesItem>(favouritesListModel);
	private JScrollPane favouritesScroller = new JScrollPane(favouritesList);

	public static Font universalFont = new Font("Consolas", Font.PLAIN, 12);

	//Created Servers/Tabs
	private List<IRCServerBase> createdServers = new ArrayList<IRCServerBase>();

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#getLimitServerLinesCount()
	 */
	@Override
	public int getLimitServerLinesCount(){
		try{
			return Integer.parseInt(limitServerLinesCount.getText());
		} catch(Exception e){
			//Was an error, default to 1000
			return DEFAULT_LINES_LIMIT;
		}
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#getLimitChannelLinesCount()
	 */
	@Override
	public int getLimitChannelLinesCount(){
		try{
			return Integer.parseInt(limitChannelLinesCount.getText());
		} catch(Exception e){
			//Was an error, default to 1000
			return DEFAULT_LINES_LIMIT;
		}
	}
	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#setCurrentTab(int)
	 */
	@Override
	public void setCurrentTab(int indexNum){
		tabbedPane.setSelectedIndex(indexNum);
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#setCurrentTab(java.lang.String)
	 */
	@Override
	public void setCurrentTab(String tabName){
		for(int x = 0; x < tabbedPane.getTabCount(); x++)
			if(tabbedPane.getTitleAt(x).toLowerCase().equals(tabName.toLowerCase()))
				tabbedPane.setSelectedIndex(x);
	}
	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#getTabIndex(java.lang.String)
	 */
	@Override
	public int getTabIndex(String tabName){
		int currentTabCount = tabbedPane.getTabCount();
		
		for(int x = 0; x < currentTabCount; x++){
			if(tabbedPane.getTitleAt(x).toLowerCase().equals(tabName.toLowerCase()))
				return x;
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#saveChannelHistory()
	 */
	@Override
	public Boolean saveChannelHistory(){
		return logChannelText.isSelected();
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#saveServerHistory()
	 */
	@Override
	public Boolean saveServerHistory(){
		return logServerActivity.isSelected();
	}


	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#getCreatedServer(java.lang.String)
	 */
	@Override
	public IRCServerBase getCreatedServer(String serverName){
		//for(int x = 0; x < createdChannels.size(); x++)
		for(IRCServerBase tempServer : createdServers)
			if(tempServer.getName().equals(serverName.toLowerCase()))
				return tempServer;
		return null;
	}


	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#addToCreatedServers(java.lang.String)
	 */
	@Override
	public void addToCreatedServers(String serverName){

		if(getCreatedServer(serverName) == null){
			IRCServer tempServer = new IRCServer(serverName.trim(),usernameTextField.getText().trim(),usernameTextField.getText().trim());
			createdServers.add(tempServer);
			tabbedPane.addTab(serverName, tempServer.icon,tempServer);
			tabbedPane.setSelectedIndex(tabbedPane.indexOfComponent(tempServer));
			tempServer.serverTextBox.requestFocus();
		}
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#isCreatedServersEmpty()
	 */
	@Override
	public Boolean isCreatedServersEmpty(){
		return createdServers.isEmpty();
	}


	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#isShowingEventTicker()
	 */
	@Override
	public Boolean isShowingEventTicker(){
		return showEventTicker.isSelected();
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#isShowingUsersList()
	 */
	@Override
	public Boolean isShowingUsersList(){
		return showUsersList.isSelected();
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#isJoinsQuitsTickerEnabled()
	 */
	@Override
	public Boolean isJoinsQuitsTickerEnabled(){
		return showJoinsQuitsEventTicker.isSelected();
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#isJoinsQuitsMainEnabled()
	 */
	@Override
	public Boolean isJoinsQuitsMainEnabled(){
		return showJoinsQuitsMainWindow.isSelected();
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#isChannelHistoryEnabled()
	 */
	@Override
	public Boolean isChannelHistoryEnabled(){
		return logChannelText.isSelected();
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#isLimitedServerActivity()
	 */
	@Override
	public Boolean isLimitedServerActivity(){
		return limitServerLines.isSelected();
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#isLimitedChannelActivity()
	 */
	@Override
	public Boolean isLimitedChannelActivity(){
		return limitChannelLines.isSelected();
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#isTimeStampsEnabled()
	 */
	@Override
	public Boolean isTimeStampsEnabled(){
		return enableTimeStamps.isSelected();
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#isClientHistoryEnabled()
	 */
	@Override
	public Boolean isClientHistoryEnabled(){
		return logClientText.isSelected();

	}   

	/*public Boolean isLinksClickable(){
	return enableClickableLinks.isSelected();
	}*/

	private void setupServerOptionsPanel(){
		serverOptionsPanel.setLayout(new BoxLayout(serverOptionsPanel, BoxLayout.PAGE_AXIS));
		serverOptionsPanel.add(new JLabel("Nick:"));
		usernameTextField.setMaximumSize(new Dimension(250,20));
		serverOptionsPanel.add(usernameTextField);
		serverOptionsPanel.add(Box.createRigidArea(new Dimension(0,5)));
		serverOptionsPanel.add(new JLabel("Server name:"));
		servernameTextField.setMaximumSize(new Dimension(250,20));
		serverOptionsPanel.add(servernameTextField);
		serverOptionsPanel.add(Box.createRigidArea(new Dimension(0,5)));
		serverOptionsPanel.add(new JLabel("Channel:"));
		firstChannelTextField.setMaximumSize(new Dimension(250,20));
		serverOptionsPanel.add(firstChannelTextField);
		serverOptionsPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		serverOptionsPanel.add(Box.createRigidArea(new Dimension(0,20)));
		serverOptionsPanel.add(connectButton);
		connectButton.addActionListener(new ConnectPressed());

		optionsRightPanel.add(serverOptionsPanel, "Server");

	}

	private void setupClientOptionsPanel(){
		optionsClientPanel.setLayout(new BoxLayout(optionsClientPanel,BoxLayout.PAGE_AXIS));


		//Settings for these are loaded with the settings API
		//found in getClientSettings()
		optionsClientPanel.add(showEventTicker);
		optionsClientPanel.add(showUsersList);
		optionsClientPanel.add(showJoinsQuitsEventTicker);
		optionsClientPanel.add(showJoinsQuitsMainWindow);
		optionsClientPanel.add(logChannelText);
		optionsClientPanel.add(logServerActivity);
		optionsClientPanel.add(logClientText);
		optionsClientPanel.add(limitServerLines);
		optionsClientPanel.add(limitServerLinesCount);
		limitServerLinesCount.setMaximumSize(new Dimension(250,20));
		optionsClientPanel.add(limitChannelLines);
		optionsClientPanel.add(limitChannelLinesCount);
		limitChannelLinesCount.setMaximumSize(new Dimension(250,20));
		optionsClientPanel.add(enableTimeStamps);

		//Turn on labels at major tick marks.
		eventTickerDelay.setMajorTickSpacing(10);
		eventTickerDelay.setMinorTickSpacing(1);
		eventTickerDelay.setPaintTicks(true);
		//optionsClientPanel.add(enableClickableLinks);
		eventTickerDelay.setPaintLabels(true);
		eventTickerDelay.setMaximumSize(new Dimension(400,40));
		optionsClientPanel.add(Box.createRigidArea(new Dimension(0,5)));
		optionsClientPanel.add(new JLabel("Event Ticker movement delay (Lower is faster)"));
		optionsClientPanel.add(Box.createRigidArea(new Dimension(0,5)));
		optionsClientPanel.add(eventTickerDelay);
		optionsClientPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		JPanel fontsPanel =  new FontPanel(this);
		fontsPanel.setMaximumSize(new Dimension(600,60));
		optionsClientPanel.add(fontsPanel);
		optionsClientPanel.add(saveSettings);
		saveSettings.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setClientSettings();
			}
		});

		optionsRightPanel.add(optionsClientPanel, "Client");
	}

	private void setupFavouritesOptionsPanel(){
		optionsFavouritesPanel.setLayout(new BorderLayout());
		optionsFavouritesPanel.add(autoConnectToFavourites, BorderLayout.NORTH);
		favouritesList.addMouseListener(new FavouritesPopClickListener());
		favouritesScroller.setPreferredSize(new Dimension(autoConnectToFavourites.getPreferredSize().width,0));
		optionsFavouritesPanel.add(favouritesScroller, BorderLayout.LINE_START);


		optionsRightPanel.add(optionsFavouritesPanel, "Favourites");
	}


	/**
	 * Create an element in the favourites list. Contains a constructor plus a pop up menu
	 * for the element. 
	 * @author Matt
	 * @param String server
	 * @param String channel
	 */
	class FavouritesItem{
		String server;
		String channel;
		FavouritesPopUp myMenu;

		public FavouritesItem(String server, String channel){
			this.server = server;
			this.channel = channel;
			myMenu = new FavouritesPopUp();
		}

		@Override
		public String toString(){
			return server +":"+channel;
		}

		private class FavouritesPopUp extends JPopupMenu {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3599612559330380653L;
			JMenuItem nameItem;
			JMenuItem removeItem;
			public FavouritesPopUp(){
				nameItem = new JMenuItem(FavouritesItem.this.toString());
				add(nameItem);
				this.addSeparator();
				//nameItem.setEnabled(false);
				removeItem = new JMenuItem("Delete");
				removeItem.addActionListener(new RemoveFavourite());
				add(removeItem);
			}

		}


		private class RemoveFavourite implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(favouritesList.getSelectedIndex() > -1){
					FavouritesItem tempItem = favouritesListModel.elementAt(favouritesList.getSelectedIndex());
					removeFavourite(tempItem.server,tempItem.channel);
					clientSettings.node("Favourites").node(server).remove(tempItem.channel);
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#addFavourite(java.lang.String, java.lang.String)
	 */
	@Override
	public void addFavourite(String server,String channel){
		favouritesListModel.addElement(new FavouritesItem(server, channel));
		clientSettings.node("Favourites").node(server).put(channel, channel);
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#isFavourite(java.lang.String, java.lang.String)
	 */
	@Override
	public Boolean isFavourite(String server,String channel){
		FavouritesItem castItem;

		for(Object tempItem : favouritesListModel.toArray()){
			castItem = (FavouritesItem) tempItem;
			if(castItem.server.equals(server) && castItem.channel.equals(channel)){
				return true;
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#isFavourite(urChatBasic.frontend.IRCChannel)
	 */
	@Override
	public Boolean isFavourite(IRCChannel channel){
		FavouritesItem castItem;

		for(Object tempItem : favouritesListModel.toArray()){
			castItem = (FavouritesItem) tempItem;
			if(castItem.server.equals(channel.getServer()) && castItem.channel.equals(channel.getName())){
				return true;
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see urChatBasic.frontend.UserGUIBase#removeFavourite(java.lang.String, java.lang.String)
	 */
	@Override
	public void removeFavourite(String server,String channel){
		FavouritesItem castItem;

		for(Object tempItem : favouritesListModel.toArray()){
			castItem = (FavouritesItem) tempItem;
			if(castItem.server.equals(server) && castItem.channel.equals(channel)){
				favouritesListModel.removeElement(castItem);
				break;
			}
		}
	}


	class FavouritesPopClickListener extends MouseAdapter {
		public void mousePressed(MouseEvent e){
			if (e.isPopupTrigger()){
				int row = favouritesList.locationToIndex(e.getPoint());
				if(row > -1){
					favouritesList.setSelectedIndex(row);
					doPop(e);
				}
			}
		}

		public void mouseReleased(MouseEvent e){
			if (e.isPopupTrigger()){
				int row = favouritesList.locationToIndex(e.getPoint());
				if(row > -1){
					favouritesList.setSelectedIndex(row);
					doPop(e);
				}
			}
		}

		private void doPop(MouseEvent e){
			favouritesList.getSelectedValue().myMenu.show(e.getComponent(), e.getX(), e.getY());
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

	 private void setupRightOptionsPanel(){
		 ListSelectionModel listSelectionModel = optionsList.getSelectionModel();
		 listSelectionModel.addListSelectionListener(
				 new OptionsListSelectionHandler());

		 optionsRightPanel.setBackground(Color.BLACK);
		 optionsRightPanel.setLayout(new CardLayout());

		 setupServerOptionsPanel();
		 setupClientOptionsPanel();
		 setupFavouritesOptionsPanel();
	 }

	 /**
	  * Used to initiate server connection
	  * @author Matt
	  *
	  */
	 private class ConnectPressed implements ActionListener{
		 @Override
		 public void actionPerformed(ActionEvent arg0) {
			 if(connectButton.getText() == "Connect"){
				 addToCreatedServers(servernameTextField.getText().trim());

				 if(autoConnectToFavourites.isSelected()){
					 FavouritesItem castItem;
					 for(Object tempItem : favouritesListModel.toArray()){
						 castItem = (FavouritesItem) tempItem;
						 addToCreatedServers(castItem.server);
					 }
				 }
			 }
		 }
	 }


	 /* (non-Javadoc)
	  * @see urChatBasic.frontend.UserGUIBase#sendGlobalMessage(java.lang.String, java.lang.String)
	  */
	 @Override
	 public void sendGlobalMessage(String message, String sender){
		 for(IRCServerBase tempServer : createdServers)
			 tempServer.sendClientText(message, sender);
	 }

	 /* (non-Javadoc)
	  * @see urChatBasic.frontend.UserGUIBase#connectFavourites(urChatBasic.base.IRCServerBase)
	  */
	 @Override
	 public void connectFavourites(IRCServerBase server){
		 if(servernameTextField.getText().trim().equals(server.getName()))
			 server.sendClientText("/join "+firstChannelTextField.getText().trim(),servernameTextField.getText().trim());

		 if(autoConnectToFavourites.isSelected()){
			 FavouritesItem castItem;
			 for(Object tempItem : favouritesListModel.toArray()){
				 castItem = (FavouritesItem) tempItem;
				 if(castItem.server.equals(server.getName()))
					 if(server.getCreatedChannel(castItem.channel) == null)
						 server.sendClientText("/join "+castItem.channel,castItem.server);
			 }
		 }
	 }

	 /* (non-Javadoc)
	  * @see urChatBasic.frontend.UserGUIBase#shutdownAll()
	  */
	 @Override
	 public void shutdownAll(){
		 if(!isCreatedServersEmpty()){
			 quitServers();
			 connectButton.setText("Connect");
		 }
	 }

	 /* (non-Javadoc)
	  * @see urChatBasic.frontend.UserGUIBase#quitServers()
	  */
	 @Override
	 public void quitServers(){
		 while(createdServers.iterator().hasNext()){
			 IRCServerBase tempServer = createdServers.iterator().next();
			 tempServer.quitChannels();
			 tempServer.quitPrivateRooms();
			 if(tempServer instanceof IRCServer)
			 {
				 tabbedPane.remove((IRCServer)tempServer);
			 }
			 createdServers.remove(tempServer);
		 }
	 }

	 /* (non-Javadoc)
	  * @see urChatBasic.frontend.UserGUIBase#quitServer(urChatBasic.base.IRCServerBase)
	  */
	 @Override
	 public void quitServer(IRCServerBase server){
		 server.quitChannels();
		 server.quitPrivateRooms();
		 tabbedPane.remove((IRCServer)server);
		 createdServers.remove(server);
	 }



	 /**
	  * Saves the settings into the registry/Settings API
	  */
	 private void setClientSettings(){
		 clientSettings.put("FIRST_CHANNEL", firstChannelTextField.getText());
		 clientSettings.put("SERVER_NAME", servernameTextField.getText());
		 clientSettings.put("NICK_NAME", usernameTextField.getText());
		 clientSettings.putBoolean("TIME_STAMPS", enableTimeStamps.isSelected());
		 clientSettings.putBoolean("EVENT_TICKER_ACTIVE",showEventTicker.isSelected());
		 clientSettings.putBoolean("USERS_LIST_ACTIVE", showUsersList.isSelected());
		 clientSettings.putBoolean("EVENT_TICKER_JOINS_QUITS", showJoinsQuitsEventTicker.isSelected());
		 clientSettings.putBoolean("MAIN_WINDOW_JOINS_QUITS", showJoinsQuitsMainWindow.isSelected());
		 clientSettings.putBoolean("LOG_CHANNEL_HISTORY", logChannelText.isSelected());
		 clientSettings.putBoolean("LOG_SERVER_ACTIVITY", logServerActivity.isSelected());
		 clientSettings.putBoolean("LIMIT_CHANNEL_LINES", limitChannelLines.isSelected());
		 clientSettings.putBoolean("AUTO_CONNECT_FAVOURITES", autoConnectToFavourites.isSelected());
		 clientSettings.put("LIMIT_CHANNEL_LINES_COUNT", limitChannelLinesCount.getText());
		 clientSettings.putBoolean("LIMIT_SERVER_LINES", limitServerLines.isSelected());
		 clientSettings.put("LIMIT_SERVER_LINES_COUNT", limitServerLinesCount.getText());
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
		 showUsersList.setSelected(clientSettings.getBoolean("USERS_LIST_ACTIVE", true));
		 showEventTicker.setSelected(clientSettings.getBoolean("EVENT_TICKER_ACTIVE", true));
		 enableTimeStamps.setSelected(clientSettings.getBoolean("TIME_STAMPS", true));
		 showJoinsQuitsEventTicker.setSelected(clientSettings.getBoolean("EVENT_TICKER_JOINS_QUITS", true));
		 showJoinsQuitsMainWindow.setSelected(clientSettings.getBoolean("MAIN_WINDOW_JOINS_QUITS", false));
		 logChannelText.setSelected(clientSettings.getBoolean("LOG_CHANNEL_HISTORY", false));
		 logServerActivity.setSelected(clientSettings.getBoolean("LOG_SERVER_ACTIVITY", false));
		 limitChannelLines.setSelected(clientSettings.getBoolean("LIMIT_CHANNEL_LINES", true));
		 limitChannelLinesCount.setText(clientSettings.get("LIMIT_CHANNEL_LINES_COUNT","500"));
		 limitServerLines.setSelected(clientSettings.getBoolean("LIMIT_SERVER_LINES", true));
		 limitServerLinesCount.setText(clientSettings.get("LIMIT_SERVER_LINES_COUNT","500"));
		 logClientText.setSelected(clientSettings.getBoolean("LOG_CLIENT_TEXT", false));
		 eventTickerDelay.setValue(clientSettings.getInt("EVENT_TICKER_DELAY", 20));
		 autoConnectToFavourites.setSelected(clientSettings.getBoolean("AUTO_CONNECT_FAVOURITES", false));


		 try {
			 for(String serverNode : clientSettings.node("Favourites").childrenNames())
				 for(String value : clientSettings.node("Favourites").node(serverNode).keys())
					 favouritesListModel.addElement(new FavouritesItem(serverNode,value));
		 } catch (BackingStoreException e) {
			 Constants.LOGGER.log(Level.WARNING, e.getLocalizedMessage());
		 }
	 }

	 /* (non-Javadoc)
	  * @see urChatBasic.frontend.UserGUIBase#removeClientSetting(java.lang.String, java.lang.String)
	  */
	 @Override
	 public void removeClientSetting(String node,String key){
		 clientSettings.node(node).remove(key);
	 }

	 /* (non-Javadoc)
	  * @see urChatBasic.frontend.UserGUIBase#getEventTickerDelay()
	  */
	 @Override
	 public int getEventTickerDelay(){
		 return eventTickerDelay.getValue();
	 }

	 /**
	  * Used to change which panel to show when you choose an option
	  * Client or Server item in the list
	  * @author Matt
	  *
	  */
	 class OptionsListSelectionHandler implements ListSelectionListener {
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
		 optionsArray.addElement("Favourites");

		 setupLeftOptionsPanel();
		 setupRightOptionsPanel();

		 optionsMainPanel.add(optionsLeftPanel, BorderLayout.LINE_START);
		 optionsMainPanel.add(optionsRightPanel, BorderLayout.CENTER);
		 optionsList.setSelectedIndex(OPTIONS_INDEX);
	 }

	 private void setupTabbedPane(){
		 tabbedPane.addChangeListener(new MainTabbedPanel_changeAdapter(this));
		 tabbedPane.addMouseListener(new TabbedMouseListener());
		 setupOptionsPanel();
		 tabbedPane.addTab("Options",optionsMainPanel);

	 }
	 /**
	  * Used to listen to the right click on the tabs to determine
	  * what type we clicked on and pop up a menu or exit.
	  * I will add menus to all types eventually.
	  * @author Matt
	  *
	  */
	 private class TabbedMouseListener extends MouseInputAdapter {
		 public void mouseClicked(MouseEvent e) {
			 final int index = tabbedPane.getUI().tabForCoordinate(tabbedPane, e.getX(), e.getY());
			 if(index > -1){ 
				 String tabName = tabbedPane.getTitleAt(index);
				 if(SwingUtilities.isRightMouseButton(e))			   
					 if(tabbedPane.getComponentAt(index).getClass() == IRCChannel.class){
						 IRCServerBase tempServer = getCreatedServer(((IRCChannel)tabbedPane.getComponentAt(index)).getServer());
						 if(isFavourite(tempServer.getCreatedChannel(tabName)))
							 tempServer.getCreatedChannel(tabName).myMenu.addAsFavouriteItem.setText("Remove as Favourite");
						 else 
							 tempServer.getCreatedChannel(tabName).myMenu.addAsFavouriteItem.setText("Add as Favourite");
						 tempServer.getCreatedChannel(tabName).myMenu.show(tabbedPane, e.getX(), e.getY());
					 }else if(tabbedPane.getComponentAt(index).getClass() == IRCPrivate.class){
						 IRCServerBase tempServer = getCreatedServer(((IRCPrivate)tabbedPane.getComponentAt(index)).getServer());
						 tempServer.quitPrivateRooms(tabName);
					 }else if(getCreatedServer(tabName) != null) {
						 ((IRCServer)getCreatedServer(tabName)).myMenu.show(tabbedPane, e.getX(), e.getY());
					 }
			 }
		 }
	 }


	 //Sets focus to the clientTextBox when tab is changed to a channel
	 private void TabbedPanel_stateChanged(ChangeEvent e) {
		 JTabbedPane tabSource = (JTabbedPane) e.getSource();
		 int index = tabSource.getSelectedIndex();
		 if(index > -1){
			 if(tabbedPane.getComponentAt(index).getClass().equals(IRCChannel.class)){
				 IRCChannel tempTab = (IRCChannel)tabbedPane.getComponentAt(index);
				 tempTab.showEventTicker(isShowingEventTicker());
				 tempTab.clientTextBox.requestFocus();
				 tempTab.showUsersList(isShowingUsersList());
			 } else if(tabbedPane.getComponentAt(index).getClass().equals(IRCPrivate.class)){
				 ((IRCPrivate)tabbedPane.getComponentAt(index)).privateTextBox.requestFocus();
			 } else if(tabbedPane.getComponentAt(index).getClass().equals(IRCServer.class)){
				 ((IRCServer)tabbedPane.getComponentAt(index)).serverTextBox.requestFocus();
			 }
		 }
	 }

	 class MainTabbedPanel_changeAdapter implements javax.swing.event.ChangeListener {
		 UserGUI adaptee;
		 MainTabbedPanel_changeAdapter(UserGUI adaptee) {
			 this.adaptee = adaptee;
		 }
		 public void stateChanged(ChangeEvent e) {
			 adaptee.TabbedPanel_stateChanged(e);
		 }
	 }


	 public UserGUI(){
		 //Create the initial size of the panel
		 setupTabbedPane();
		 this.setLayout(new BorderLayout());
		 this.add(tabbedPane, BorderLayout.CENTER);
		 this.setFont(universalFont);
		 //Set size of the overall panel
		 this.setPreferredSize (new Dimension(MAIN_WIDTH, MAIN_HEIGHT));
		 this.setBackground(Color.gray);
		 clientSettings = Preferences.userNodeForPackage(this.getClass());
		 getClientSettings();
	 }

	 /* (non-Javadoc)
	  * @see urChatBasic.frontend.UserGUIBase#paintComponent(java.awt.Graphics)
	  */
	 @Override
	 public void paintComponent(Graphics g){
		 super.paintComponent(g);

	 }


	 /* (non-Javadoc)
	  * @see urChatBasic.frontend.UserGUIBase#run()
	  */
	 @Override
	 public void run() {
		 // Auto-generated method stub
	 }
}