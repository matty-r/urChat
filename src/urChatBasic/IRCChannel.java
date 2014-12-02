package urChatBasic;

import java.awt.event.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class IRCChannel extends JPanel implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1358231872908927052L;

	private UserGUI gui = DriverGUI.gui;
	
	private DateFormat historyDateFormat = new SimpleDateFormat("HHmm ddMMyyyy");
	private Date todayDate = new Date();
	private String channelName;
	private final int USER_LIST_WIDTH = 100;
	private ArrayList<String> channelHistory = new ArrayList<String>();
	private ArrayList<String> userHistory = new ArrayList<String>();
	private String channelTopic;
	private String historyFileName;
	  ////////////////
	 //GUI ELEMENTS//
	////////////////
	//channel Text Area
	private JTextPane channelTextArea = new JTextPane();
	private JScrollPane channelScroll = new JScrollPane(channelTextArea);
	private Font channelFont = new Font("Consolas", Font.PLAIN, 12);

	//Users list area
	private ArrayList<IRCUser> usersArray = new ArrayList<IRCUser>();
	private UsersListModel usersListModel = new UsersListModel(usersArray);
	@SuppressWarnings("unchecked")
	private JList<IRCUser> usersList = new JList<IRCUser>(usersListModel);
	private JScrollPane userScroller = new JScrollPane(usersList);
	
	//bottomPanel
	private JPanel bottomPanel = new JPanel();
	public JTextField clientTextBox = new JTextField();
	private int BOTTOM_HEIGHT = 40;
	
	//Main Panel
	private JPanel mainPanel = new JPanel();
	final int MAIN_WIDTH = 500;
	final int MAIN_HEIGHT = 400;
	
	//AutoCompleteWord
	private String startingCharacters = null;
	private String lastUserToComplete = null;
	private ArrayList<String> autoCompleteNames = new ArrayList<String>();
	
	//Event Ticker stuff
	private ArrayList<JLabel> eventLabels = new ArrayList<JLabel>();
	private final int EVENT_VELOCITY = 1;
	private Timer eventTickerTimer = new Timer(0, new eventTickerAction());
	private JPanel tickerPanel = new JPanel();
	private TickerListener eventTickerListener = new TickerListener();
	private final int EVENT_BUFFER = 20;
	
	//Repaints the window, delayed by EVENT_DELAY
	private class eventTickerAction implements ActionListener
	{
		public void actionPerformed (ActionEvent event)
		{
	     for(JLabel tempLabel : eventLabels){
	    	tempLabel.setLocation(tempLabel.getX()-EVENT_VELOCITY, 0);
	    	if(tempLabel.getX()+tempLabel.getWidth() < 0){
	    		eventLabels.remove(tempLabel);
	    		break;
	    	}
	     }
	     
	     if(eventLabels.isEmpty())
	    	 eventTickerTimer.stop();
	     repaint();
		}
	}
	
	
	public String getName(){
		return this.channelName;
	}
	
	public void setName(String newName){
		channelName = newName;
	}
	
	private class userChatText implements ActionListener
	   {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				try {
					Connection.sendClientText(clientTextBox.getText(),getName());
					if(gui.getClientHistory())
						userHistory.add(clientTextBox.getText());
				} catch (IOException e) {
					e.printStackTrace();
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
   private class myKeyListener implements KeyListener {
	      public void keyPressed( KeyEvent e ) {
	    	  //When the user presses tab
	         if ( e.getKeyCode() == KeyEvent.VK_TAB ) {
	        	 //If we haven't already started pressing tab startingCharacters will be null
	        	 if(startingCharacters == null){
	        		 //If it's not the first word then get from where the last space is
	        		 if(clientTextBox.getText().lastIndexOf(" ") >= 0)
	        			 startingCharacters = clientTextBox.getText().toLowerCase().substring(clientTextBox.getText().lastIndexOf(" ")+1, clientTextBox.getCaretPosition());
	        		 else
	        			 startingCharacters = clientTextBox.getText().toLowerCase().substring(0, clientTextBox.getCaretPosition());
	        	 }
	        	 
	        	 //If usersArray and clientText isn't empty.
	        	 ArrayList<String> matches = new ArrayList<String>();
	        	 if(usersArray.size() > 0 && clientTextBox.getText().length() > 0){
	        		 for(int x=0; x < usersArray.size()-1; x++){
	        			 //For each matching word put it in the matches array
	        			 if(usersArray.get(x).getName().toLowerCase().replace("@","").startsWith(startingCharacters))
	        				 matches.add(usersArray.get(x).getName());
	        		 }
	        		 //If the matches arean't already in autoCompleteNames
	        		 if(!matches.isEmpty()){
			        	 if(!autoCompleteNames.containsAll(matches))
							 autoCompleteNames = matches;
		        	 
		        	 
			        	 String nextUser;
			        	 //If we haven't already chosen a previous match, starting from the beginning
						 if(lastUserToComplete == null){
					 		lastUserToComplete = autoCompleteNames.get(0);
					 		nextUser = autoCompleteNames.get(0);
						 } else {
							 //Otherwise choose the next one along, or go back to the start if its the last match
							if((autoCompleteNames.indexOf(lastUserToComplete) + 1) == autoCompleteNames.size())
								nextUser = autoCompleteNames.get(0);
							else
								nextUser = autoCompleteNames.get(autoCompleteNames.indexOf(lastUserToComplete) + 1);
						 }
					 
						 //If the lastUser is already in the clientTextBox then just replace it
						 //otherwise put it where the cursor and the last space is
						 if(clientTextBox.getText().contains(lastUserToComplete))
		    					clientTextBox.setText(clientTextBox.getText().replace(lastUserToComplete+": ", nextUser+": "));
		    				else
		    					clientTextBox.setText(clientTextBox.getText().substring(0, (clientTextBox.getCaretPosition()-startingCharacters.length()))+(nextUser+": "));	        				
			    				
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
	   usersList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	   usersList.setLayoutOrientation(JList.VERTICAL);
	   usersList.setVisibleRowCount(-1);
	   userScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	   userScroller.setPreferredSize(new Dimension(USER_LIST_WIDTH, MAIN_HEIGHT-BOTTOM_HEIGHT)); 
   }
   
   private void setupTickerPanel(){
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
   
   public void tickerPanelAddEventLabel(String eventText){
	   eventTickerTimer.setDelay(gui.getEventTickerDelay());
	  if(gui.getJoinsQuitsTicker()){
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
   }
   
	@SuppressWarnings("unchecked")
	private void setupBottomPanel(){
		setupTickerPanel();
		bottomPanel.setLayout(new GridLayout(2,1));
	    //Set initial sizes and colours
		bottomPanel.setPreferredSize(new Dimension(MAIN_WIDTH,BOTTOM_HEIGHT));
		bottomPanel.setBackground(Color.yellow);
		bottomPanel.setLocation(0,MAIN_HEIGHT-BOTTOM_HEIGHT);
		bottomPanel.add(clientTextBox);
		bottomPanel.add(tickerPanel);
		clientTextBox.addActionListener(new userChatText());
		clientTextBox.addKeyListener(new myKeyListener());
		clientTextBox.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,Collections.EMPTY_SET);
	}
	
   /**
    * Return the appropriate created IRC User
    * @param channelName
    * @return IRCChannel
    */
   public IRCUser getCreatedUsers(String userName){
	   //for(int x = 0; x < createdChannels.size(); x++)
	   for(IRCUser tempUser : usersArray)
		   if(tempUser.getName().matches(userName))
			   return tempUser;
	   return null;
   }
	
	public void printText(String line, String fromUser){
		DateFormat chatDateFormat = new SimpleDateFormat("HHmm");
		Date chatDate = new Date();
		
		if(gui.getTimeStamp())
			line = "["+chatDateFormat.format(chatDate)+"] " + line;
		if(gui.getChannelHistory())
			channelHistory.add(line);

		if(getCreatedUsers(fromUser).getName().matches(Connection.myNick)){
		    StyledDocument doc = (StyledDocument) channelTextArea.getDocument();
	    	Style style = doc.addStyle("StyleName", null);

	        StyleConstants.setItalic(style, true);

	        try {
				doc.insertString(doc.getLength(), line+"\n", style);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			StyledDocument doc = (StyledDocument) channelTextArea.getDocument();
			Style style = doc.addStyle("StyleName", null);

			try {
				doc.insertString(doc.getLength(), line+"\n", style);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		channelTextArea.setCaretPosition(channelTextArea.getDocument().getLength());
	}
	
	private void setupMainTextArea(){
		channelScroll.setPreferredSize(new Dimension(MAIN_WIDTH-USER_LIST_WIDTH,MAIN_HEIGHT-BOTTOM_HEIGHT));
		channelScroll.setLocation(0, 0);
		channelTextArea.setEditable(false);
		channelTextArea.setFont(channelFont);
	}
	
	private void setupMainPanel(){
		mainPanel.setLayout( new BorderLayout());
		
		//mainPanel.setPreferredSize (new Dimension(MAIN_WIDTH, MAIN_HEIGHT));
		//mainPanel.setLocation(0,0);
		setupMainTextArea();
	    mainPanel.add(channelScroll,BorderLayout.CENTER);
		setupUsersList();
		mainPanel.add(userScroller,BorderLayout.LINE_END);
	    mainPanel.setBackground(Color.black);
	    setupBottomPanel();
	    mainPanel.add(bottomPanel,BorderLayout.PAGE_END);
	}

	public IRCChannel(String channelName){
		//Create the initial size of the panel
	    //Set size of the overall panel
		setPreferredSize (new Dimension(MAIN_WIDTH, MAIN_HEIGHT));
		setBackground(Color.gray);
		setupMainPanel();
		setName(channelName);
		this.setLayout(new BorderLayout());
		this.add(mainPanel,BorderLayout.CENTER);
		historyFileName = historyDateFormat.format(todayDate)+" "+this.channelName+".log";
	 }


	//Adds users to the list in the users array[]
	public void addToUsersList(String channel,String[] users){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				if(users.length >= 5){
					for(int x = 5; x < users.length; x++){
						if(users[x].startsWith(":")){
							usersArray.add(new IRCUser(users[x].substring(1)));
						} else
							usersArray.add(new IRCUser(users[x]));
						usersList.setSelectedIndex(0);
					}
				}
				usersListModel.sort();
			}
		});
	}
	
	//Adds a single user, good for when a user joins the channel
	public void addToUsersList(String channel,String user){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				String thisUser = user;
						if(user.startsWith(":"))
							thisUser = user.substring(1);
						
						//usersArray.addElement(thisUser);
						usersArray.add(new IRCUser(thisUser));
						usersList.setSelectedIndex(0);
						tickerPanelAddEventLabel("++ "+thisUser+" has entered "+channel);
						usersListModel.sort();
					}
		});
	}
	

	/**private int getLargerElement(int y, int x,String firstElement, String secondElement){
		int maximumAccuracy = firstElement.length();

		if(firstElement.length() <= secondElement.length())
			maximumAccuracy = firstElement.length();
		else 
			maximumAccuracy = secondElement.length();
		
		for(int z = 1; z < maximumAccuracy; z ++)
			if(firstElement.toLowerCase().charAt(z) > secondElement.toLowerCase().charAt(z))
				return y;
			else if(firstElement.toLowerCase().charAt(z) < secondElement.toLowerCase().charAt(z))
				return x;	
					
		
		return 0;
	}*/
	
	/**
	 * Removes a single user, good for when a user joins the channel
	 * @param channel
	 * @param user
	 **/
	public void removeFromUsersList(String channel,String user){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				String thisUser = user;
						if(user.startsWith(":"))
							thisUser = user.substring(1);

						for(int x =0; x < usersArray.size();x++)
						{
							if(usersArray.get(x).getName().matches(thisUser)){
								usersArray.remove(x);
							usersList.setSelectedIndex(0);
							tickerPanelAddEventLabel("-- "+thisUser+" has quit "+channel);
							}
						}
						usersListModel.sort();
					}
		});
	}

	/**Rename user by removing old name and inserting new name.*/
	public void renameUserUsersList(String channel,String oldUserName,String newUserName){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				String thisoldUser = oldUserName;
				String thisnewUser = newUserName;
						if(oldUserName.startsWith(":"))
							thisoldUser = oldUserName.substring(1);

								for(IRCUser tempUser : usersArray)
						    		if(tempUser.getName().equals(oldUserName)){
						    			tempUser.setName(newUserName);
						    			break;
						    		}
								tickerPanelAddEventLabel("!! "+thisoldUser+" changed name to "+thisnewUser);
					}
		});
	}
	
	/**Clear the users list*/
	public void clearUsersList(String channel){
		usersArray.clear();
	}

	@Override
	public void run() {
		// Do I need anything in here?
	}

	public String getChannelTopic() {
		return channelTopic;
	}

	public void setChannelTopic(String channelTopic) {
		this.channelTopic = channelTopic;
		this.tickerPanelAddEventLabel(channelTopic);
	}
	
	/** Write all competitors to the competitors.txt file */
	public void writeHistoryFile() throws IOException{
		if(gui.saveChannelHistory()){
			FileWriter fw = new FileWriter (historyFileName);
			BufferedWriter bw = new BufferedWriter (fw);
			PrintWriter outFile = new PrintWriter (bw);
			for(String eachLine : channelHistory)
				outFile.println(eachLine);
			outFile.close();
		}
	}
}
