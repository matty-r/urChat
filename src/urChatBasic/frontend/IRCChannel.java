package urChatBasic.frontend;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.*;

import urChatBasic.base.Constants;

public class IRCChannel extends JPanel implements IRCActions{
	/**
	 *
	 */
	private static final long serialVersionUID = 1358231872908927052L;

	private UserGUI gui = DriverGUI.gui;

	private DateFormat historyDateFormat = new SimpleDateFormat("ddMMyyyy");
	private Date todayDate = new Date();
	private String channelName;
	private String serverName;
	//Used to identify a message to be printed from the Event ticker
	public static final String EVENT_USER = "****";
	private final int USER_LIST_WIDTH = 100;
	//Deprecated. Was used to save history, but is now done live.
	//private List<String> channelHistory = new ArrayList<String>();
	private List<String> userHistory = new ArrayList<String>();
	private String channelTopic;
	private String historyFileName;
	////////////////
	//GUI ELEMENTS//
	////////////////
	private JPanel fontPanel;

	//Icons
	public ImageIcon icon;

	public ChannelPopUp myMenu;

	//channel Text Area
	private JTextPane channelTextArea = new JTextPane();
	private JScrollPane channelScroll = new JScrollPane(channelTextArea);
	//private Font channelFont = gui.getFont(); JPanel has it's own Font attribute

	//Users list area
	private List<IRCUser> usersArray = new ArrayList<IRCUser>();
	private UsersListModel usersListModel = new UsersListModel(usersArray);
	@SuppressWarnings("unchecked")
	private JList<IRCUser> usersList = new JList<IRCUser>(usersListModel);
	private JScrollPane userScroller = new JScrollPane(usersList);
	private Boolean usersListShown = null;

	//bottomPanel
	private JPanel bottomPanel = new JPanel();
	public JTextField clientTextBox = new JTextField();
	private int BOTTOM_HEIGHT = 35;

	//Main Panel
	private JPanel mainPanel = new JPanel();
	final int MAIN_WIDTH = 500;
	final int MAIN_HEIGHT = 400;

	//AutoCompleteWord
	private String startingCharacters = null;
	private String lastUserToComplete = null;
	private List<String> autoCompleteNames = new ArrayList<String>();

	//Event Ticker stuff
	private List<JLabel> eventLabels = new ArrayList<JLabel>();
	private final int EVENT_VELOCITY = 1;
	private Timer eventTickerTimer = new Timer(0, new TickerAction());
	private JPanel tickerPanel = new JPanel();
	private TickerListener eventTickerListener = new TickerListener();
	private final int EVENT_BUFFER = 20;
	private Boolean eventTickerShown = null;

	//IRCServer information (Owner of channel)
	private IRCServer myServer;


	/**
	 * Constructor
	 * @param serverName
	 * @param channelName
	 */
	public IRCChannel(IRCServer serverName,String channelName){
		//Create the initial size of the panel
		//Set size of the overall panel
		setPreferredSize (new Dimension(MAIN_WIDTH, MAIN_HEIGHT));
		setBackground(Color.gray);
		setupMainPanel();
		setName(channelName);
		this.setLayout(new BorderLayout());
		this.add(mainPanel,BorderLayout.CENTER);
		historyFileName = historyDateFormat.format(todayDate)+" "+this.channelName+".log";
		this.myMenu = new ChannelPopUp();
		this.setFont(gui.getFont());
		fontPanel = new FontPanel(this);
		mainPanel.add(fontPanel, BorderLayout.NORTH);
		fontPanel.setVisible(false);
		Image tempIcon = null;
		try {
			tempIcon = ImageIO.read(new File(Constants.RESOURCES_DIR+"Room.png"));
		} catch (IOException e) {
			Constants.LOGGER.log(Level.SEVERE, "FAILED TO LOAD Room.png: " + e.getLocalizedMessage());
		}
		icon = new ImageIcon(tempIcon);

		myServer = serverName;
		this.serverName = serverName.getName();
	}


	//Repaints the window, delayed by EVENT_DELAY
	private class TickerAction implements ActionListener
	{
		public void actionPerformed (ActionEvent event)
		{
			if(IRCChannel.this.tickerPanel.isVisible()){
				for(JLabel tempLabel : eventLabels){
					tempLabel.setLocation(tempLabel.getX()-EVENT_VELOCITY, 0);
					if(tempLabel.getX()+tempLabel.getWidth() < 0){
						tickerPanel.remove(tempLabel);
						eventLabels.remove(tempLabel);
						break;
					}
				}

				if(eventLabels.isEmpty())
					eventTickerTimer.stop();

				repaint();
			} else {
				while(eventLabels.iterator().hasNext()){
					JLabel tempLabel = eventLabels.iterator().next();
					tickerPanel.remove(tempLabel);
					eventLabels.remove(tempLabel);
				}

				eventTickerTimer.stop();
			}
		}
	}

	public String getServer(){
		return this.serverName;
	}

	public String getName(){
		return this.channelName;
	}

	public void setName(String newName){
		channelName = newName;
	}

	private class SendTextListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(!clientTextBox.getText().trim().isEmpty()){
				myServer.sendClientText(clientTextBox.getText(),getName());
				if(gui.isClientHistoryEnabled())
					userHistory.add(clientTextBox.getText());
			}
			clientTextBox.setText("");
		}
	}

	/**
	 * Tab Auto Complete between where the Caret is positioned and the index of the last space.
	 * Therefore it must be the last word of the sentence.
	 * @author Matt
	 *
	 */
	private class ChannelKeyListener implements KeyListener {
		public void keyPressed( KeyEvent e ) {
			//When the user presses tab
			if ( e.getKeyCode() == KeyEvent.VK_TAB ) {
				//If we haven't already started pressing tab startingCharacters will be null
				if(startingCharacters == null){
					//If it's not the first word then get from where the last space is
					if(clientTextBox.getText().lastIndexOf(" ") >= 0)
					{
						// get text between last space and caretPosition
						for(String word : clientTextBox.getText().split(" "))
						{
							if(startingCharacters == null || (clientTextBox.getText().indexOf(word) < clientTextBox.getCaretPosition() && clientTextBox.getText().indexOf(word) > clientTextBox.getText().indexOf(startingCharacters)))
							{
								startingCharacters = word;
							}
						}

						// startingCharacters = clientTextBox.getText().toLowerCase().substring(clientTextBox.getText().lastIndexOf(" ")+1, clientTextBox.getCaretPosition());
					}
					else
					{
						startingCharacters = clientTextBox.getText().toLowerCase().substring(0, clientTextBox.getCaretPosition());
					}
				}

				//If usersArray and clientText isn't empty.
				if(usersArray.size() > 0 && clientTextBox.getText().length() > 0){
					usersArray.stream()
						.filter(user -> user.getName().toLowerCase().replace("@", "").startsWith(startingCharacters.toLowerCase()))
						.forEach(user -> autoCompleteNames.add(user.getName()));

					//If the matches arean't already in autoCompleteNames
					if(!autoCompleteNames.isEmpty()){

						String nextUser;
						int currentCaretPosition = clientTextBox.getCaretPosition();
						//If we haven't already chosen a previous match, starting from the beginning
						if(lastUserToComplete == null){
							lastUserToComplete = autoCompleteNames.get(0);
							nextUser = autoCompleteNames.get(0);
						} else {
							currentCaretPosition = clientTextBox.getText().indexOf(lastUserToComplete);
							//Otherwise choose the next one along, or go back to the start if its the last match
							if((autoCompleteNames.indexOf(lastUserToComplete) + 1) == autoCompleteNames.size())
								nextUser = autoCompleteNames.get(0);
							else
								nextUser = autoCompleteNames.get(autoCompleteNames.indexOf(lastUserToComplete) + 1);
						}

						//If the lastUser is already in the clientTextBox then just replace it
						//otherwise put it where the cursor and the last space is
						int completionLength = nextUser.length();
						if(clientTextBox.getText().contains(lastUserToComplete))
						{
							// TODO: this should only replace the text closest to the caret position.
							clientTextBox.setText(clientTextBox.getText().replace(lastUserToComplete+": ", nextUser+": "));
							completionLength += 2;
						} else
						{
							String textAfterCaret = clientTextBox.getText().substring(clientTextBox.getCaretPosition(), clientTextBox.getText().length());
							clientTextBox.setText(clientTextBox.getText().substring(0, (clientTextBox.getCaretPosition()-startingCharacters.length()))+(nextUser+": ") + textAfterCaret);
						}
						clientTextBox.setCaretPosition((currentCaretPosition+completionLength));
						lastUserToComplete = nextUser;
					}
				}
			} else {
				int nextTextInt = 0;
				switch(e.getKeyCode()){
				case KeyEvent.VK_UP:if(!userHistory.isEmpty()){
					nextTextInt = userHistory.indexOf(clientTextBox.getText())-1;
					if(nextTextInt < 0)
						nextTextInt = userHistory.size()-1;

					clientTextBox.setText(userHistory.get(nextTextInt));
				}
				break;
				case KeyEvent.VK_DOWN:if(!userHistory.isEmpty()){
					nextTextInt = userHistory.indexOf(clientTextBox.getText())+1;
					if(nextTextInt > userHistory.size()-1)
						nextTextInt = 0;

					clientTextBox.setText(userHistory.get(nextTextInt));
				}
				break;
				case KeyEvent.VK_ESCAPE: clientTextBox.setText("");
				break;
				default: if(lastUserToComplete != null)
					lastUserToComplete = null;
				if(startingCharacters != null)
					startingCharacters = null;
				if(!autoCompleteNames.isEmpty())
					autoCompleteNames.clear();
				break;
				}
			}
		}
		public void keyTyped(KeyEvent ke){}
		public void keyReleased(KeyEvent ke){}
	}

	private void setupUsersList(){
		usersList.setFont(getFont());
		usersList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		usersList.setLayoutOrientation(JList.VERTICAL);
		usersList.setVisibleRowCount(-1);
		usersList.addMouseListener(new UsersMouseListener());
		userScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		userScroller.setPreferredSize(new Dimension(USER_LIST_WIDTH, MAIN_HEIGHT-BOTTOM_HEIGHT));
	}


	/**
	 * Used for when you right click on a user - displays the menu.
	 * @author Matt
	 *
	 */
	class UsersMouseListener extends MouseInputAdapter{
		public void mouseClicked(MouseEvent e) {
			final int index = usersList.locationToIndex(e.getPoint());
			if(index > -1){
				usersList.setSelectedIndex(index);
				IRCUser userName = usersList.getSelectedValue();
				if(SwingUtilities.isRightMouseButton(e))
					userName.myMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	private void setupTickerPanel(){
		tickerPanel.setFont(getFont());
		tickerPanel.setPreferredSize(clientTextBox.getPreferredSize());
		tickerPanel.setBackground(Color.LIGHT_GRAY);
		tickerPanel.setLayout(null);
		tickerPanel.addMouseListener(eventTickerListener);
	}

	private class TickerListener extends MouseInputAdapter {
		public void mouseEntered(MouseEvent e) {
			eventTickerTimer.setDelay(gui.getEventTickerDelay()*10);
		}

		public void mouseExited(MouseEvent e){
			eventTickerTimer.setDelay(gui.getEventTickerDelay());
		}
	}

	public class IRCAlert extends JLabel{
		/**
		 *
		 */
		 private static final long serialVersionUID = 1L;
		 AlertType type;

		 IRCAlert(AlertType type){
			 this.type = type;
		 }

	}

	public void createEvent(String eventText){
		eventTickerTimer.setDelay(gui.getEventTickerDelay());
		if(gui.isJoinsQuitsTickerEnabled()){
			JLabel tempLabel = new JLabel(eventText);
			int tempX = 0;
			if(!(eventLabels.isEmpty())){
				if(eventLabels.get(eventLabels.size()-1).getPreferredSize().width + eventLabels.get(eventLabels.size()-1).getX()+EVENT_BUFFER < super.getWidth()){
					tempX = super.getWidth()+EVENT_BUFFER;
				} else {
					tempX = eventLabels.get(eventLabels.size()-1).getPreferredSize().width + eventLabels.get(eventLabels.size()-1).getX()+EVENT_BUFFER;
				}
			} else {
				tempX = super.getWidth();
			}
			//Get the preferred size as this will be long enough to contain the entire string
			int tempLabelWidth = (int) tempLabel.getPreferredSize().getWidth();
			int tempLabelHeight = (int) tempLabel.getPreferredSize().getHeight();
			//Ensures we don't get overlaps of labels. tempX is the width of the last label
			//which is then used as the x position plus the width of the frame
			tempLabel.setBounds(tempX, 0, tempLabelWidth, tempLabelHeight);
			tempLabel.setBackground(Color.BLACK);
			//Add the label to the list of labels
			eventLabels.add(tempLabel);
			//Add it to the actual panel
			tickerPanel.add(tempLabel);
			//if the timer hasn't already started, then start it.
			if(!(eventTickerTimer.isRunning()))
				eventTickerTimer.start();
		}

		if(gui.isJoinsQuitsMainEnabled())
			printText(gui.isTimeStampsEnabled(), eventText, EVENT_USER);
	}

	@SuppressWarnings("unchecked")
	private void setupBottomPanel(){
		setupTickerPanel();
		bottomPanel.setLayout(new BorderLayout());
		//Set initial sizes and colours
		bottomPanel.setPreferredSize(new Dimension(MAIN_WIDTH,BOTTOM_HEIGHT));
		bottomPanel.setBackground(Color.BLACK);
		bottomPanel.setLocation(0,MAIN_HEIGHT-BOTTOM_HEIGHT);
		bottomPanel.add(clientTextBox,BorderLayout.NORTH);
		bottomPanel.add(tickerPanel);
		clientTextBox.setFont(getFont());
		clientTextBox.addActionListener(new SendTextListener());
		clientTextBox.addKeyListener(new ChannelKeyListener());
		clientTextBox.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,Collections.EMPTY_SET);
	}


	class ChannelPopUp extends JPopupMenu{
		/**
		 *
		 */
		private static final long serialVersionUID = 640768684923757684L;
		JMenuItem nameItem;
		JMenuItem quitItem;
		JMenuItem hideUsersItem;
		JMenuItem hideTickerItem;
		JMenuItem addAsFavouriteItem;
		JMenuItem chooseFont;
		public ChannelPopUp(){
			nameItem = new JMenuItem(IRCChannel.this.getName());
			add(nameItem);
			addSeparator();
			//
			quitItem = new JMenuItem("Quit");
			add(quitItem);
			quitItem.addActionListener(new QuitItem());
			//
			hideUsersItem = new JMenuItem("Toggle Users List");
			add(hideUsersItem);
			hideUsersItem.addActionListener(new ToggleHideUsersListItem());
			//
			hideTickerItem = new JMenuItem("Toggle Event Ticker");
			add(hideTickerItem);
			hideTickerItem.addActionListener(new ToggleHideTickerListItem());
			//
			addAsFavouriteItem = new JMenuItem("Add as Favourite");
			add(addAsFavouriteItem);
			addAsFavouriteItem.addActionListener(new AddAsFavourite());
			//
			chooseFont = new JMenuItem("Toggle Font chooser");
			add(chooseFont);
			chooseFont.addActionListener(new ChooseFont());
		}
	}

	private class QuitItem implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			myServer.sendClientText("/part i'm outta here", IRCChannel.this.getName());
		}
	}
	/**
	 * Used by the PopUpMenu to Toggle the Ticker
	 * @author Matt
	 *
	 */
	private class ToggleHideUsersListItem implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			usersListShown = !IRCChannel.this.userScroller.isVisible();
			showUsersList(!IRCChannel.this.userScroller.isVisible());
		}
	}

	private class AddAsFavourite implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0){
			if(!gui.isFavourite(IRCChannel.this)){
				gui.addFavourite(myServer.getName(),IRCChannel.this.getName());
			} else {
				gui.removeFavourite(myServer.getName(),IRCChannel.this.getName());
			}
		}
	}

	private class ChooseFont implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0){
			fontPanel.setVisible(!fontPanel.isVisible());
		}
	}

	/**
	 * First checks to make sure the user hasn't set it manually for this channel.
	 * usersListShown is only set by the pop up menu, so unless you've changed it,
	 * it won't care about the global setting
	 * @param showIt
	 */
	public void showUsersList(Boolean showIt){
		if(usersListShown == showIt || usersListShown == null)
			IRCChannel.this.userScroller.setVisible(showIt);
	}

	/**
	 * Used by the PopUpMenu to Toggle the Ticker
	 * @author Matt
	 *
	 */
	private class ToggleHideTickerListItem implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			eventTickerShown = !IRCChannel.this.tickerPanel.isVisible();
			showEventTicker(!IRCChannel.this.tickerPanel.isVisible());
		}
	}
	/**
	 * First checks to make sure the user hasn't set it manually for this channel.
	 * eventTickerShown is only set by the pop up menu, so unless you've changed it,
	 * it won't care about the global setting
	 * @param showIt
	 */
	public void showEventTicker(Boolean showIt){
		if(eventTickerShown == showIt || eventTickerShown == null){
			IRCChannel.this.tickerPanel.setVisible(showIt);
			if(IRCChannel.this.tickerPanel.isVisible())
				IRCChannel.this.bottomPanel.setPreferredSize(new Dimension(IRCChannel.this.getWidth(),BOTTOM_HEIGHT));
			else
				IRCChannel.this.bottomPanel.setPreferredSize(IRCChannel.this.clientTextBox.getPreferredSize());
		}
	}

	/**
	 * Return the appropriate created IRC User
	 * @param channelName
	 * @return IRCChannel
	 */
	public IRCUser getCreatedUsers(String userName){
		for(IRCUser tempUser : usersArray){
			if(tempUser.getName().toLowerCase().equals(userName.toLowerCase()))
				return tempUser;
		};

		return null;
	}

	public void doLimitLines(){
		if(gui.isLimitedChannelActivity()){
			String[] tempText = channelTextArea.getText().split("\n");
			int linesCount = tempText.length;

			if(linesCount >= gui.getLimitChannelLinesCount()){
				String newText =  channelTextArea.getText().replace(tempText[0]+"\n", "");
				channelTextArea.setText(newText);
			}
		}
	}

	public void printText(Boolean dateTime, String line, String fromUser){

		DateFormat chatDateFormat = new SimpleDateFormat("HHmm");
		Date chatDate = new Date();
		String timeLine = "";

		if(dateTime)
			timeLine = "["+chatDateFormat.format(chatDate)+"]";
		if(gui.isChannelHistoryEnabled()){
			try {
				writeHistoryFile(line);
			} catch (IOException e) {
				Constants.LOGGER.log(Level.WARNING, e.getLocalizedMessage());
			}
		}
		StyledDocument doc = (StyledDocument) channelTextArea.getDocument();
		//If we received a message from a user that isn't in the channel
		//then add them to the users list.
		//But don't add them if it's from the Event Ticker
		if(getCreatedUsers(fromUser) == null){
			if(!fromUser.equals(EVENT_USER))
				addToUsersList(this.getName(), fromUser);
		}


		if((getCreatedUsers(fromUser) != null && !getCreatedUsers(fromUser).isMuted()) || fromUser.equals(EVENT_USER)){
			new LineFormatter(this.getFont(),myServer.getNick()).formattedDocument(doc, timeLine, fromUser, line);

			channelTextArea.setCaretPosition(channelTextArea.getDocument().getLength());
		}
	}

	private void setupMainTextArea(){
		channelScroll.setPreferredSize(new Dimension(MAIN_WIDTH-USER_LIST_WIDTH,MAIN_HEIGHT-BOTTOM_HEIGHT));
		channelScroll.setLocation(0, 0);
		channelScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		channelTextArea.setEditable(false);
		channelTextArea.setFont(gui.getFont());
		channelTextArea.setEditorKit(new WrapEditorKit());
	}



	class WrapEditorKit extends StyledEditorKit {
		/**
		 *
		 */
		private static final long serialVersionUID = 980393121518733188L;
		ViewFactory defaultFactory=new WrapColumnFactory();
		public ViewFactory getViewFactory() {
			return defaultFactory;
		}

	}

	class WrapColumnFactory implements ViewFactory {
		public View create(Element elem) {
			String kind = elem.getName();
			if (kind != null) {
				if (kind.equals(AbstractDocument.ContentElementName)) {
					return new WrapLabelView(elem);
				} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
					return new ParagraphView(elem);
				} else if (kind.equals(AbstractDocument.SectionElementName)) {
					return new BoxView(elem, View.Y_AXIS);
				} else if (kind.equals(StyleConstants.ComponentElementName)) {
					return new ComponentView(elem);
				} else if (kind.equals(StyleConstants.IconElementName)) {
					return new IconView(elem);
				}
			}

			// default to text display
			return new LabelView(elem);
		}
	}

	class WrapLabelView extends LabelView {
		public WrapLabelView(Element elem) {
			super(elem);
		}

		public float getMinimumSpan(int axis) {
			switch (axis) {
			case View.X_AXIS:
				return 0;
			case View.Y_AXIS:
				return super.getMinimumSpan(axis);
			default:
				throw new IllegalArgumentException("Invalid axis: " + axis);
			}
		}
	}

	private void setupMainPanel(){
		mainPanel.setLayout( new BorderLayout());
		setupMainTextArea();
		mainPanel.add(channelScroll,BorderLayout.CENTER);
		setupUsersList();
		mainPanel.add(userScroller,BorderLayout.LINE_END);
		mainPanel.setBackground(Color.black);
		setupBottomPanel();
		mainPanel.add(bottomPanel,BorderLayout.SOUTH);
	}


	//Adds users to the list in the users array[]
	public void addToUsersList(final String channel,final String[] users){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				if(users.length >= 0){
					for(int x = 0; x < users.length; x++){
						String tempUser = users[x];
						if(users[x].startsWith(":"))
							tempUser = tempUser.substring(1);

						if(!usersArray.contains(tempUser))
							usersArray.add(new IRCUser(myServer,tempUser));
					}
				}
				usersListModel.sort();
			}
		});
	}

	//Adds a single user, good for when a user joins the channel
	public void addToUsersList(final String channel,final String user){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				String thisUser = user;
				if(user.startsWith(":"))
					thisUser = user.substring(1);

				if(!usersArray.contains(thisUser)){
					usersArray.add(new IRCUser(myServer,thisUser));
					usersList.setSelectedIndex(0);
					createEvent("++ "+thisUser+" has entered "+channel);
					usersListModel.sort();
				}
			}
		});
	}

	public String getChannelTopic(String channelName) {
		return getChannelTopic();
	}

	/**
	 * Removes a single user, good for when a user leaves the channel
	 * @param channel
	 * @param user
	 **/
	public void removeFromUsersList(final String channel,final String user){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				String thisUser = user;
				if(user.startsWith(":"))
					thisUser = user.substring(1);

				for(int x =0; x < usersArray.size();x++)
				{
					if(usersArray.get(x).getName().matches(thisUser)){
						usersArray.remove(x);


						createEvent("-- "+thisUser+" has quit "+channel);
					}
				}
				usersListModel.sort();
			}
		});
	}

	/**Clear the users list*/
	public void clearUsersList(String channel){
		usersArray.clear();
	}


	public String getChannelTopic() {
		return channelTopic;
	}

	public void setChannelTopic(String channelTopic) {
		this.channelTopic = channelTopic;
		this.createEvent(channelTopic);
	}

	/** Write all competitors to the competitors.txt file */
	public void writeHistoryFile(String line) throws IOException{
		if(gui.saveChannelHistory()){
			FileWriter fw = new FileWriter (Constants.DIRECTORY_LOGS+historyFileName, true);
			BufferedWriter bw = new BufferedWriter (fw);
			PrintWriter outFile = new PrintWriter (bw);
			outFile.println(line);
			outFile.close();
		}
	}

	/**Rename user by removing old name and inserting new name.*/
	public void renameUser(final String oldUserName,final String newUserName) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				IRCUser tempUser = getCreatedUsers(oldUserName);
				if(tempUser != null){
					createEvent("!! "+oldUserName+" changed name to "+newUserName);
					tempUser.setName(newUserName);
				}
			}
		});
	}
}
