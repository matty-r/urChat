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

	//Tabs
	public DnDTabbedPane tabbedPane = new DnDTabbedPane();
	private final int OPTIONS_INDEX = 0;

	//Options Panel
	private JPanel optionsMainPanel = new JPanel();
	private JPanel optionsLeftPanel = new JPanel();
	private DefaultListModel<String> optionsArray = new DefaultListModel<String>();
	private JList<String> optionsList = new JList<String>(optionsArray);
	private JPanel optionsRightPanel = new JPanel();
	private Preferences clientSettings;


	//Client Options Panel
	private static final JPanel optionsClientPanel = new JPanel();
	private static final JScrollPane clientScroller = new JScrollPane(optionsClientPanel);
	private static final JCheckBox showEventTicker = new JCheckBox("Show Event Ticker");
	private static final JCheckBox showUsersList = new JCheckBox("Show Users List");
	private static final JCheckBox showJoinsQuitsEventTicker = new JCheckBox("Show Joins/Quits in the Event Ticker");
	private static final JCheckBox showJoinsQuitsMainWindow = new JCheckBox("Show Joins/Quits in the Chat Window");
	private static final JCheckBox logChannelText = new JCheckBox("Save and log all channel text");
	private static final JCheckBox logServerActivity = new JCheckBox("Save and log all Server activity");
	private static final JCheckBox logClientText = new JCheckBox("Log client text (Allows up or down history)");
	private static final JCheckBox limitServerLines = new JCheckBox("Limit the number of lines in Server activity");
	private static final JCheckBox limitChannelLines = new JCheckBox("Limit the number of lines in channel text");
	private static final JCheckBox enableTimeStamps = new JCheckBox("Time Stamp chat messages");
	private JPanel clientFontPanel = new FontPanel(this);
	//private JCheckBox enableClickableLinks = new JCheckBox("Make links clickable");

	private JTextField limitServerLinesCount = new JTextField();
	private JTextField limitChannelLinesCount = new JTextField();

	private static final int TICKER_DELAY_MIN = 0;
	private static final int TICKER_DELAY_MAX = 30;
	private static final int TICKER_DELAY_INIT = 20; 
	private static final int DEFAULT_LINES_LIMIT = 500;
	private final JSlider eventTickerDelay = new JSlider(JSlider.HORIZONTAL,TICKER_DELAY_MIN, TICKER_DELAY_MAX, TICKER_DELAY_INIT);

	private final JButton saveSettings = new JButton("Save");

	//Server Options Panel
	private static final JPanel serverOptionsPanel = new JPanel();
	private static final JScrollPane serverScroller = new JScrollPane(serverOptionsPanel);
	private static final JLabel userNameLabel = new JLabel("Nick:");
	private static final JTextField userNameTextField = new JTextField("");
	private static final JLabel realNameLabel = new JLabel("Real name:");
	private static final JTextField realNameTextField = new JTextField("");
	private static final JLabel serverNameLabel = new JLabel("Server:");
	private static final JTextField servernameTextField = new JTextField("");
	private static final JLabel serverPortLabel = new JLabel("Port:");
	private static final JTextField serverPortTextField = new JTextField("");
	private static final JLabel firstChannelLabel = new JLabel("Channel:");
	private static final JTextField firstChannelTextField = new JTextField("");
	private static final JButton connectButton = new JButton("Connect");

	//Favourites Panel
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
			//Was an error, set to default
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
			IRCServer tempServer = new IRCServer(serverName.trim(),userNameTextField.getText().trim(),realNameTextField.getText().trim(),serverPortTextField.getText().trim());
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

	 private void setupOptionsPanel(){
		 optionsMainPanel.setLayout(new BorderLayout());
		 
		 optionsArray.addElement("Server");
		 optionsArray.addElement("Client");

		 setupLeftOptionsPanel();
		 setupRightOptionsPanel();

		 optionsMainPanel.add(optionsLeftPanel, BorderLayout.LINE_START);
		 optionsMainPanel.add(optionsRightPanel, BorderLayout.CENTER);
		 optionsList.setSelectedIndex(OPTIONS_INDEX);

		 optionsClientPanel.setPreferredSize(new Dimension(500,350));
		 serverOptionsPanel.setPreferredSize(new Dimension(200,350));
		 
		 optionsRightPanel.add(serverScroller, "Server");
		 optionsRightPanel.add(clientScroller,"Client");
	 }
	 
	/**
	 * Houses the options list
	 */
	 private void setupLeftOptionsPanel(){
		optionsLeftPanel.setBackground(Color.RED);
		optionsLeftPanel.setPreferredSize(new Dimension(100,0));
		optionsLeftPanel.setLayout(new BorderLayout());
		optionsLeftPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		optionsLeftPanel.add(saveSettings, BorderLayout.SOUTH);
		optionsLeftPanel.add(optionsList, BorderLayout.CENTER);
	 }

	 private void setupRightOptionsPanel(){
		 ListSelectionModel listSelectionModel = optionsList.getSelectionModel();
		 listSelectionModel.addListSelectionListener(
				 new OptionsListSelectionHandler());

		 optionsRightPanel.setBackground(Color.BLACK);
		 optionsRightPanel.setLayout(new CardLayout());

		 setupServerOptionsPanelComponents();
		 setupClientOptionsPanelComponents();
		 setupFavouritesOptionsPanel();
	 }

	
	 /**
	  * Add the components to the Server Options Panel.
	  */
	private void setupServerOptionsPanelComponents(){
		//serverOptionsPanel.setLayout(new BoxLayout(serverOptionsPanel, BoxLayout.PAGE_AXIS));
		setupServerOptionsLayout();
		
		
		serverOptionsPanel.add(userNameLabel);
		serverOptionsPanel.add(userNameTextField);
		userNameTextField.setPreferredSize(new Dimension(100,20));
		
		serverOptionsPanel.add(realNameLabel);
		serverOptionsPanel.add(realNameTextField);
		realNameTextField.setPreferredSize(new Dimension(100,20));
		
		serverOptionsPanel.add(serverNameLabel);
		serverOptionsPanel.add(servernameTextField);
		servernameTextField.setPreferredSize(new Dimension(100,20));
		
		serverOptionsPanel.add(serverPortLabel);
		serverOptionsPanel.add(serverPortTextField);
		serverPortTextField.setPreferredSize(new Dimension(50,20));
		
		serverOptionsPanel.add(firstChannelLabel);
		serverOptionsPanel.add(firstChannelTextField);
		firstChannelTextField.setPreferredSize(new Dimension(100,20));
		serverOptionsPanel.add(connectButton);
		connectButton.addActionListener(new ConnectPressed());
		
		serverOptionsPanel.add(autoConnectToFavourites);
		
		favouritesScroller.setPreferredSize(new Dimension(autoConnectToFavourites.getPreferredSize().width,0));
		favouritesList.addMouseListener(new FavouritesPopClickListener());
		serverOptionsPanel.add(favouritesScroller);

		
	}
	
	/**
	 * Aligns components on the Server Options Panel
	 */
	private void setupServerOptionsLayout(){
		SpringLayout serverLayout = new SpringLayout();
		serverOptionsPanel.setLayout(serverLayout);
		
		//Used to make it more obvious what is going on -
		//and perhaps more readable.
		//0 means THAT edge will be flush with the opposing components edge
		//Yes, negative numbers will make it overlap
		final int TOP_SPACING = 5;
		final int TOP_ALIGNED = 0;
		final int LEFT_ALIGNED = 0;
		final int LEFT_SPACING = 0;
		
		// Components are aligned off the top label
		serverLayout.putConstraint(SpringLayout.WEST, userNameLabel, 5, SpringLayout.WEST, serverOptionsPanel);
		serverLayout.putConstraint(SpringLayout.NORTH, userNameLabel, TOP_ALIGNED, SpringLayout.NORTH, serverOptionsPanel);
		
		serverLayout.putConstraint(SpringLayout.NORTH, userNameTextField, TOP_ALIGNED, SpringLayout.SOUTH, userNameLabel);
		serverLayout.putConstraint(SpringLayout.WEST, userNameTextField, LEFT_ALIGNED, SpringLayout.WEST, userNameLabel);

		serverLayout.putConstraint(SpringLayout.NORTH, realNameLabel, TOP_SPACING, SpringLayout.SOUTH, userNameTextField);
		serverLayout.putConstraint(SpringLayout.WEST, realNameLabel, LEFT_ALIGNED, SpringLayout.WEST, userNameTextField);
		
		serverLayout.putConstraint(SpringLayout.NORTH, realNameTextField, TOP_ALIGNED, SpringLayout.SOUTH, realNameLabel);
		serverLayout.putConstraint(SpringLayout.WEST, realNameTextField, LEFT_ALIGNED, SpringLayout.WEST, realNameLabel);
		
		serverLayout.putConstraint(SpringLayout.NORTH, serverNameLabel, TOP_SPACING, SpringLayout.SOUTH, realNameTextField);
		serverLayout.putConstraint(SpringLayout.WEST, serverNameLabel, LEFT_ALIGNED, SpringLayout.WEST, realNameTextField);
		
		serverLayout.putConstraint(SpringLayout.NORTH, servernameTextField, TOP_ALIGNED, SpringLayout.SOUTH, serverNameLabel);
		serverLayout.putConstraint(SpringLayout.WEST, servernameTextField, LEFT_ALIGNED, SpringLayout.WEST, serverNameLabel);
		
		serverLayout.putConstraint(SpringLayout.NORTH, serverPortLabel, TOP_ALIGNED, SpringLayout.NORTH, serverNameLabel);
		serverLayout.putConstraint(SpringLayout.WEST, serverPortLabel, LEFT_SPACING, SpringLayout.EAST, servernameTextField);
		
		serverLayout.putConstraint(SpringLayout.NORTH, serverPortTextField, TOP_ALIGNED, SpringLayout.SOUTH, serverPortLabel);
		serverLayout.putConstraint(SpringLayout.WEST, serverPortTextField, LEFT_ALIGNED, SpringLayout.WEST, serverPortLabel);
		
		serverLayout.putConstraint(SpringLayout.NORTH, firstChannelLabel, TOP_SPACING, SpringLayout.SOUTH, servernameTextField);
		serverLayout.putConstraint(SpringLayout.WEST, firstChannelLabel, LEFT_ALIGNED, SpringLayout.WEST, servernameTextField);

		serverLayout.putConstraint(SpringLayout.NORTH, firstChannelTextField, TOP_ALIGNED, SpringLayout.SOUTH, firstChannelLabel);
		serverLayout.putConstraint(SpringLayout.WEST, firstChannelTextField, LEFT_ALIGNED, SpringLayout.WEST, firstChannelLabel);
		
		serverLayout.putConstraint(SpringLayout.NORTH, connectButton, TOP_SPACING*TOP_SPACING, SpringLayout.SOUTH, firstChannelTextField);
		serverLayout.putConstraint(SpringLayout.WEST, connectButton, LEFT_SPACING, SpringLayout.WEST, firstChannelTextField);
		
		serverLayout.putConstraint(SpringLayout.NORTH, autoConnectToFavourites, TOP_SPACING, SpringLayout.SOUTH, connectButton);
		serverLayout.putConstraint(SpringLayout.WEST, autoConnectToFavourites, LEFT_SPACING, SpringLayout.WEST, connectButton);
		
		serverLayout.putConstraint(SpringLayout.NORTH, favouritesScroller, TOP_SPACING, SpringLayout.SOUTH, autoConnectToFavourites);
		serverLayout.putConstraint(SpringLayout.SOUTH, favouritesScroller, TOP_ALIGNED, SpringLayout.SOUTH, serverOptionsPanel);
		serverLayout.putConstraint(SpringLayout.WEST, favouritesScroller, LEFT_SPACING, SpringLayout.WEST, autoConnectToFavourites);
	}

	private void setupClientOptionsPanelComponents(){
		
		//clientScroller.setPreferredSize(new Dimension(this.getSize())); 
		//clientScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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

		limitServerLinesCount.setPreferredSize(new Dimension(50,20));
		optionsClientPanel.add(limitChannelLines);

		optionsClientPanel.add(limitChannelLinesCount);

		limitChannelLinesCount.setPreferredSize(new Dimension(50,20));
		optionsClientPanel.add(enableTimeStamps);

		clientFontPanel.setPreferredSize(new Dimension(500,40));
		optionsClientPanel.add(clientFontPanel);
		
		//Turn on labels at major tick mark.
		eventTickerDelay.setMajorTickSpacing(10);
		eventTickerDelay.setMinorTickSpacing(1);
		eventTickerDelay.setPaintTicks(true);
		//Ensure this is added to the COMPONENT ALIGNMENT once enabled.
		//optionsClientPanel.add(enableClickableLinks);
		eventTickerDelay.setPaintLabels(true);
		eventTickerDelay.setMaximumSize(new Dimension(400,40));
		
		eventTickerDelay.setToolTipText("Event Ticker movement delay (Lower is faster)");
		optionsClientPanel.add(eventTickerDelay);

		saveSettings.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setClientSettings();
			}
		});
		
		setupClientOptionsLayout();
		//optionsRightPanel.add(optionsClientPanel, "Client");
	}

	/**
	 * Aligns components on the Client Options Panel
	 */
	private void setupClientOptionsLayout(){
		SpringLayout clientLayout = new SpringLayout();
		optionsClientPanel.setLayout(clientLayout);
		
		//Used to make it more obvious what is going on -
		//and perhaps more readable.
		//0 means THAT edge will be flush with the opposing components edge
		//Yes, negative numbers will make it overlap
		final int TOP_SPACING = 0;
		final int TOP_ALIGNED = 0;
		final int LEFT_ALIGNED = 0;
		final int LEFT_SPACING = 0;
		
		// Components are aligned off the top label
		clientLayout.putConstraint(SpringLayout.WEST, showEventTicker, LEFT_ALIGNED, SpringLayout.WEST, optionsClientPanel);
		clientLayout.putConstraint(SpringLayout.NORTH, showEventTicker, TOP_ALIGNED, SpringLayout.NORTH, optionsClientPanel);
		
		clientLayout.putConstraint(SpringLayout.NORTH, showUsersList, TOP_SPACING, SpringLayout.SOUTH, showEventTicker);
		clientLayout.putConstraint(SpringLayout.WEST, showUsersList, LEFT_ALIGNED, SpringLayout.WEST, showEventTicker);
		
		/* --Place holder for clickable links alignment settings--
		 * Uncomment when clickable links is added
		 * clientLayout.putConstraint(SpringLayout.NORTH, enableClickableLinks, TOP_SPACING, SpringLayout.SOUTH, showUsersList);
		 * clientLayout.putConstraint(SpringLayout.WEST, enableClickableLinks, LEFT_ALIGNED, SpringLayout.WEST, showUsersList);
		 * clientLayout.putConstraint(SpringLayout.WEST, showJoinsQuitsEventTicker, LEFT_ALIGNED, SpringLayout.WEST, enableClickableLinks);
		 * clientLayout.putConstraint(SpringLayout.WEST, showJoinsQuitsEventTicker, LEFT_ALIGNED, SpringLayout.WEST, enableClickableLinks);
		 */
		
		//Remove this when clickable links is added
		clientLayout.putConstraint(SpringLayout.NORTH, showJoinsQuitsEventTicker, TOP_SPACING, SpringLayout.SOUTH, showUsersList);
		//Remove this when clickable links is added
		clientLayout.putConstraint(SpringLayout.WEST, showJoinsQuitsEventTicker, LEFT_ALIGNED, SpringLayout.WEST, showUsersList);
		
		clientLayout.putConstraint(SpringLayout.NORTH, showJoinsQuitsMainWindow, TOP_SPACING, SpringLayout.SOUTH, showJoinsQuitsEventTicker);
		clientLayout.putConstraint(SpringLayout.WEST, showJoinsQuitsMainWindow, LEFT_ALIGNED, SpringLayout.WEST, showJoinsQuitsEventTicker);
		
		clientLayout.putConstraint(SpringLayout.NORTH, logChannelText, TOP_SPACING, SpringLayout.SOUTH, showJoinsQuitsMainWindow);
		clientLayout.putConstraint(SpringLayout.WEST, logChannelText, LEFT_ALIGNED, SpringLayout.WEST, showJoinsQuitsMainWindow);
		
		clientLayout.putConstraint(SpringLayout.NORTH, logServerActivity, TOP_SPACING, SpringLayout.SOUTH, logChannelText);
		clientLayout.putConstraint(SpringLayout.WEST, logServerActivity, LEFT_ALIGNED, SpringLayout.WEST, logChannelText);
		
		clientLayout.putConstraint(SpringLayout.NORTH, logClientText, TOP_SPACING, SpringLayout.SOUTH, logServerActivity);
		clientLayout.putConstraint(SpringLayout.WEST, logClientText, LEFT_ALIGNED, SpringLayout.WEST, logServerActivity);
		
		clientLayout.putConstraint(SpringLayout.NORTH, limitServerLines, TOP_SPACING, SpringLayout.SOUTH, logClientText);
		clientLayout.putConstraint(SpringLayout.WEST, limitServerLines, LEFT_ALIGNED, SpringLayout.WEST, logClientText);
		
		clientLayout.putConstraint(SpringLayout.NORTH, limitServerLinesCount, TOP_ALIGNED, SpringLayout.NORTH, limitServerLines);
		clientLayout.putConstraint(SpringLayout.WEST, limitServerLinesCount, TOP_SPACING, SpringLayout.EAST, limitServerLines);

		clientLayout.putConstraint(SpringLayout.NORTH, limitChannelLines, TOP_SPACING, SpringLayout.SOUTH, limitServerLines);
		clientLayout.putConstraint(SpringLayout.WEST, limitChannelLines, LEFT_ALIGNED, SpringLayout.WEST, limitServerLines);
		
		clientLayout.putConstraint(SpringLayout.NORTH, limitChannelLinesCount, TOP_ALIGNED, SpringLayout.NORTH, limitChannelLines);
		clientLayout.putConstraint(SpringLayout.WEST, limitChannelLinesCount, LEFT_SPACING, SpringLayout.EAST, limitChannelLines);
		
		clientLayout.putConstraint(SpringLayout.NORTH, enableTimeStamps, TOP_SPACING, SpringLayout.SOUTH, limitChannelLines);
		clientLayout.putConstraint(SpringLayout.WEST, enableTimeStamps, LEFT_ALIGNED, SpringLayout.WEST, limitChannelLines);
		
		clientLayout.putConstraint(SpringLayout.NORTH, clientFontPanel, TOP_SPACING, SpringLayout.SOUTH, enableTimeStamps);
		clientLayout.putConstraint(SpringLayout.WEST, clientFontPanel, LEFT_SPACING, SpringLayout.WEST, enableTimeStamps);

		
		clientLayout.putConstraint(SpringLayout.NORTH, eventTickerDelay, TOP_SPACING, SpringLayout.SOUTH, clientFontPanel);
		clientLayout.putConstraint(SpringLayout.WEST, eventTickerDelay, LEFT_ALIGNED, SpringLayout.WEST, clientFontPanel);
	}
	
	private void setupFavouritesOptionsPanel(){
		//optionsFavouritesPanel.setLayout(new BorderLayout());
		//optionsFavouritesPanel.add(autoConnectToFavourites, BorderLayout.NORTH);

		//optionsFavouritesPanel.add(favouritesScroller, BorderLayout.LINE_START);


		//optionsRightPanel.add(optionsFavouritesPanel, "Favourites");
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
		clientSettings.node(Constants.KEY_FAVOURITES_NODE).node(server).put(channel, channel);
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
			 if(tempServer instanceof IRCServerBase)
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
	 public void setClientSettings(){
		 clientSettings.put(Constants.KEY_FIRST_CHANNEL, firstChannelTextField.getText());
		 clientSettings.put(Constants.KEY_FIRST_SERVER, servernameTextField.getText());
		 clientSettings.put(Constants.KEY_FIRST_PORT, serverPortTextField.getText());
		 clientSettings.put(Constants.KEY_NICK_NAME, userNameTextField.getText());
		 clientSettings.put(Constants.KEY_REAL_NAME, realNameTextField.getText());
		 clientSettings.putBoolean(Constants.KEY_TIME_STAMPS, enableTimeStamps.isSelected());
		 clientSettings.putBoolean(Constants.KEY_EVENT_TICKER_ACTIVE,showEventTicker.isSelected());
		 clientSettings.putBoolean(Constants.KEY_USERS_LIST_ACTIVE, showUsersList.isSelected());
		 clientSettings.putBoolean(Constants.KEY_EVENT_TICKER_JOINS_QUITS, showJoinsQuitsEventTicker.isSelected());
		 clientSettings.putBoolean(Constants.KEY_MAIN_WINDOW_JOINS_QUITS, showJoinsQuitsMainWindow.isSelected());
		 clientSettings.putBoolean(Constants.KEY_LOG_CHANNEL_HISTORY, logChannelText.isSelected());
		 clientSettings.putBoolean(Constants.KEY_LOG_SERVER_ACTIVITY, logServerActivity.isSelected());
		 clientSettings.putBoolean(Constants.KEY_LIMIT_CHANNEL_LINES, limitChannelLines.isSelected());
		 clientSettings.putBoolean(Constants.KEY_AUTO_CONNECT_FAVOURITES, autoConnectToFavourites.isSelected());
		 clientSettings.put(Constants.KEY_LIMIT_CHANNEL_LINES_COUNT, limitChannelLinesCount.getText());
		 clientSettings.putBoolean(Constants.KEY_LIMIT_SERVER_LINES, limitServerLines.isSelected());
		 clientSettings.put(Constants.KEY_LIMIT_SERVER_LINES_COUNT, limitServerLinesCount.getText());
		 clientSettings.putBoolean(Constants.KEY_LOG_CLIENT_TEXT, logClientText.isSelected());
		 clientSettings.putInt(Constants.KEY_EVENT_TICKER_DELAY, eventTickerDelay.getValue());
		 clientSettings.putInt(Constants.KEY_WINDOW_X,(int) DriverGUI.frame.getBounds().getX());
		 clientSettings.putInt(Constants.KEY_WINDOW_Y,(int) DriverGUI.frame.getBounds().getY());
		 clientSettings.putInt(Constants.KEY_WINDOW_WIDTH,(int) DriverGUI.frame.getBounds().getWidth());
		 clientSettings.putInt(Constants.KEY_WINDOW_HEIGHT,(int) DriverGUI.frame.getBounds().getHeight());
	 }

	 /**
	  * Loads the settings from the registry/Settings API
	  */
	 public void getClientSettings(){
		 firstChannelTextField.setText(clientSettings.get(Constants.KEY_FIRST_CHANNEL,Constants.DEFAULT_FIRST_CHANNEL));
		 servernameTextField.setText(clientSettings.get(Constants.KEY_FIRST_SERVER, Constants.DEFAULT_FIRST_SERVER));
		 serverPortTextField.setText(clientSettings.get(Constants.KEY_FIRST_PORT, Constants.DEFAULT_FIRST_PORT));
		 userNameTextField.setText(clientSettings.get(Constants.KEY_NICK_NAME, Constants.DEFAULT_NICK_NAME));
		 realNameTextField.setText(clientSettings.get(Constants.KEY_REAL_NAME, Constants.DEFAULT_REAL_NAME));
		 showUsersList.setSelected(clientSettings.getBoolean(Constants.KEY_USERS_LIST_ACTIVE, Constants.DEFAULT_USERS_LIST_ACTIVE));
		 showEventTicker.setSelected(clientSettings.getBoolean(Constants.KEY_EVENT_TICKER_ACTIVE, Constants.DEFAULT_EVENT_TICKER_ACTIVE));
		 enableTimeStamps.setSelected(clientSettings.getBoolean(Constants.KEY_TIME_STAMPS, Constants.DEFAULT_TIME_STAMPS));
		 showJoinsQuitsEventTicker.setSelected(clientSettings.getBoolean(Constants.KEY_EVENT_TICKER_JOINS_QUITS, Constants.DEFAULT_EVENT_TICKER_JOINS_QUITS));
		 showJoinsQuitsMainWindow.setSelected(clientSettings.getBoolean(Constants.KEY_MAIN_WINDOW_JOINS_QUITS, Constants.DEFAULT_MAIN_WINDOW_JOINS_QUITS));
		 logChannelText.setSelected(clientSettings.getBoolean(Constants.KEY_LOG_CHANNEL_HISTORY, Constants.DEFAULT_LOG_CHANNEL_HISTORY));
		 logServerActivity.setSelected(clientSettings.getBoolean(Constants.KEY_LOG_SERVER_ACTIVITY, Constants.DEFAULT_LOG_SERVER_ACTIVITY));
		 limitChannelLines.setSelected(clientSettings.getBoolean(Constants.KEY_LIMIT_CHANNEL_LINES, Constants.DEFAULT_LIMIT_CHANNEL_LINES));
		 limitChannelLinesCount.setText(clientSettings.get(Constants.KEY_LIMIT_CHANNEL_LINES_COUNT,Constants.DEFAULT_LIMIT_CHANNEL_LINES_COUNT));
		 limitServerLines.setSelected(clientSettings.getBoolean(Constants.KEY_LIMIT_SERVER_LINES, Constants.DEFAULT_LIMIT_SERVER_LINES));
		 limitServerLinesCount.setText(clientSettings.get(Constants.KEY_LIMIT_SERVER_LINES_COUNT,Constants.DEFAULT_LIMIT_SERVER_LINES_COUNT));
		 logClientText.setSelected(clientSettings.getBoolean(Constants.KEY_LOG_CLIENT_TEXT, Constants.DEFAULT_LOG_CLIENT_TEXT));
		 eventTickerDelay.setValue(clientSettings.getInt(Constants.KEY_EVENT_TICKER_DELAY, Constants.DEFAULT_EVENT_TICKER_DELAY));
		 autoConnectToFavourites.setSelected(clientSettings.getBoolean(Constants.KEY_AUTO_CONNECT_FAVOURITES, Constants.DEFAULT_AUTO_CONNECT_FAVOURITES));
		 DriverGUI.frame.setBounds(clientSettings.getInt(Constants.KEY_WINDOW_X, Constants.DEFAULT_WINDOW_X), 
				 clientSettings.getInt(Constants.KEY_WINDOW_Y, Constants.DEFAULT_WINDOW_Y), 
				 clientSettings.getInt(Constants.KEY_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_WIDTH), 
				 clientSettings.getInt(Constants.KEY_WINDOW_HEIGHT, Constants.DEFAULT_WINDOW_HEIGHT));
		 this.setPreferredSize(new Dimension(clientSettings.getInt(Constants.KEY_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_WIDTH),clientSettings.getInt(Constants.KEY_WINDOW_HEIGHT, Constants.DEFAULT_WINDOW_HEIGHT)));

		 //TODO Add Port number to favourites.
		 try {
			 for(String serverNode : clientSettings.node(Constants.KEY_FAVOURITES_NODE).childrenNames())
				 for(String value : clientSettings.node(Constants.KEY_FAVOURITES_NODE).node(serverNode).keys())
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
	  * Used to change which panel to show when you choose an option under the
	  * Options Tab.
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
					 if(tabbedPane.getComponentAt(index) instanceof IRCChannel){
						 IRCServerBase tempServer = getCreatedServer(((IRCChannel)tabbedPane.getComponentAt(index)).getServer());
						 if(isFavourite(tempServer.getCreatedChannel(tabName)))
							 tempServer.getCreatedChannel(tabName).myMenu.addAsFavouriteItem.setText("Remove as Favourite");
						 else 
							 tempServer.getCreatedChannel(tabName).myMenu.addAsFavouriteItem.setText("Add as Favourite");
						 tempServer.getCreatedChannel(tabName).myMenu.show(tabbedPane, e.getX(), e.getY());
					 }else if(tabbedPane.getComponentAt(index) instanceof IRCPrivate){
						 IRCServerBase tempServer = getCreatedServer(((IRCPrivate)tabbedPane.getComponentAt(index)).getServer());
						 tempServer.quitPrivateRooms(tabName);
					 }else if(tabbedPane.getComponentAt(index) instanceof IRCServer) {
						 ((IRCServer)getCreatedServer(tabName)).myMenu.show(tabbedPane, e.getX(), e.getY());
					 }
			 }
		 }
	 }


	 /**
	  * Sets focus to the appropriate textBox when tab is changed
	  * @param e
	  */
	 private void TabbedPanel_stateChanged(ChangeEvent e) {
		 JTabbedPane tabSource = (JTabbedPane) e.getSource();
		 int index = tabSource.getSelectedIndex();
		 if(index > -1){
			 if(tabbedPane.getComponentAt(index) instanceof IRCChannel){
				 IRCChannel tempTab = (IRCChannel)tabbedPane.getComponentAt(index);
				 tempTab.showEventTicker(isShowingEventTicker());
				 tempTab.clientTextBox.requestFocus();
				 tempTab.showUsersList(isShowingUsersList());
			 } else if(tabbedPane.getComponentAt(index) instanceof IRCPrivate){
				 ((IRCPrivate)tabbedPane.getComponentAt(index)).privateTextBox.requestFocus();
			 } else if(tabbedPane.getComponentAt(index) instanceof IRCServer){
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
		 
		// this.setPreferredSize (new Dimension(MAIN_WIDTH_INIT, MAIN_HEIGHT_INIT));
		 this.setFont(universalFont);
		 //Create the initial size of the panel
		 setupTabbedPane();
		 this.setLayout(new BorderLayout());
		 this.add(tabbedPane, BorderLayout.CENTER);
		 //Set size of the overall panel
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