package urChatBasic.base;

import urChatBasic.base.IRCRoomBase;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.IRCActions;
import urChatBasic.frontend.IRCPrivate;
import urChatBasic.frontend.IRCUser;
import urChatBasic.frontend.LineFormatter;
import urChatBasic.frontend.LineFormatter.ClickableText;
import urChatBasic.frontend.components.FontPanel;
import urChatBasic.frontend.dialogs.FontDialog;
import urChatBasic.frontend.utils.URColour;
import urChatBasic.frontend.UserGUI;
import urChatBasic.frontend.UsersListModel;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.*;

public class IRCRoomBase extends JPanel
{

    //
    // TODO: private boolean hasClientTextBox = true;

    // Room information
    // TODO: Rename to roomName
    private String roomName;

    // Preferences
    private Preferences roomPrefs;

    // IRCServer information (Owner of channel)
    protected IRCServerBase server;
    protected IRCActions myActions;
    protected UserGUI gui = DriverGUI.gui;

    protected FontDialog fontDialog;

    // Useful


    // Icons
    public ImageIcon icon;

    public JPopupMenu myMenu;

    // Main Panel
    protected JPanel mainPanel = new JPanel();
    protected JSplitPane mainResizer = new JSplitPane();

    // Bottom panel to hold the user text box
    protected JTextField clientTextBox = new JTextField();
    private JPanel bottomPanel = new JPanel();
    private int BOTTOM_HEIGHT = 35;

    // Event Ticker stuff
    private List<JLabel> eventLabels = new ArrayList<JLabel>();
    private final int EVENT_VELOCITY = 1;
    private Timer eventTickerTimer = new Timer(0, new TickerAction());
    private JPanel tickerPanel = new JPanel();
    private TickerListener eventTickerListener = new TickerListener();
    private final int EVENT_BUFFER = 20;
    private Boolean eventTickerShown = null;
    private final Dimension labelSize = new JLabel("defaultLabel").getPreferredSize();

    // TODO: Rename to sentHistory
    private List<String> userHistory = new ArrayList<String>();

    // TODO: This is logging stuff, should not be handled here
    private DateFormat historyDateFormat = new SimpleDateFormat("ddMMyyyy");
    private String historyFileName;
    private Date todayDate = new Date();

    private String channelTopic;

    // Tab complete stuff
    // TODO: Rename to tab complete
    private String startingCharacters = null;
    private String lastUserToComplete = null;
    private List<String> autoCompleteNames = new ArrayList<String>();

    // Room Text Area
    private JTextPane channelTextArea = new JTextPane();
    private JScrollPane channelScroll = new JScrollPane(channelTextArea);
    private LineFormatter lineFormatter;


    // Users list area
    // TODO: Users should be created per Server, and instead have a property to hold what channels they're in
    private List<IRCUser> usersArray = new ArrayList<IRCUser>();
    private UsersListModel usersListModel = new UsersListModel(usersArray);
    @SuppressWarnings("unchecked")
    private JList<IRCUser> usersList = new JList<IRCUser>(usersListModel);
    private JScrollPane userScroller = new JScrollPane(usersList);
    private Boolean usersListShown = null;
    private int usersListWidth = 100;

    // getters & setters

    public JTextField getUserTextBox()
    {
        return clientTextBox;
    }

    public IRCServerBase getServer()
    {
        return this.server;
    }

    @Override
    public String getName()
    {
        return this.roomName;
    }

    @Override
    public void setName(String newName)
    {
        roomName = newName;
    }

    public void hideEventTicker()
    {
        eventTickerShown = false;
        tickerPanel.setVisible(false);
        bottomPanel.setPreferredSize(clientTextBox.getPreferredSize());
    }

    public void hideUsersList()
    {
        usersListShown = false;
        // userScroller.setVisible(usersListShown);
        toggleUsersList();
    }

    public void showUsersList()
    {
        usersListShown = true;
        // userScroller.setVisible(usersListShown);
        toggleUsersList();
    }

    public IRCRoomBase(String roomName)
    {
        this.roomName = roomName;
        initRoom();
    }

    public IRCRoomBase(IRCServerBase server, String roomName)
    {
        this.roomName = roomName;
        setServer(server);
        initRoom();
    }

    public void setServer(IRCServerBase server)
    {
        this.server = server;
    }

    private void initRoom()
    {
        if(null != getServer())
        {
            roomPrefs = Constants.FAVOURITES_PREFS.node(getServer().getName()).node(roomName);
            fontDialog = new FontDialog(roomName, gui.getFont(), roomPrefs);

            lineFormatter = new LineFormatter(getFontPanel().getFont(), getServer().getNick());
        } else {
            roomPrefs = Constants.FAVOURITES_PREFS.node(roomName);
            fontDialog = new FontDialog(roomName, gui.getFont(), roomPrefs);

            lineFormatter = new LineFormatter(getFontPanel().getFont(), null);
        }

        setFont(getFontPanel().getFont());

        // Create the initial size of the panel
        // Set size of the overall panel
        setPreferredSize(new Dimension(Constants.MAIN_WIDTH, Constants.MAIN_HEIGHT));
        setBackground(Color.gray);
        setupMainPanel();
        setName(roomName);
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);

        this.myMenu = new ChannelPopUp();
        fontDialog.setVisible(false);
        fontDialog.addSaveListener(new SaveFontListener());

        myActions = new IRCActions(this);
    }

    private void setupMainPanel()
    {
        mainPanel.setLayout(new BorderLayout());
        setupMainTextArea();
        // mainPanel.add(channelScroll, BorderLayout.CENTER);
        setupUsersList();
        // mainPanel.add(userScroller, BorderLayout.LINE_END);

        //Create a split pane with the two scroll panes in it.
        mainResizer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                        channelScroll, userScroller);
        mainResizer.setOneTouchExpandable(true);

        // This should be set to where the minimum size of the userScroller would end up
        mainResizer.setDividerLocation(gui.getWidth() - (userScroller.getPreferredSize().width + mainResizer.getDividerSize()));

        // Left most panel (channelScroll pane), gets the extra space when resizing the window
        mainResizer.setResizeWeight(1);
        channelScroll.setMinimumSize(channelScroll.getPreferredSize());
        userScroller.setMinimumSize(userScroller.getPreferredSize());

        mainPanel.setBackground(Color.black);
        mainPanel.add(mainResizer, BorderLayout.CENTER);
        setupBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    public FontPanel getFontPanel()
    {
        return fontDialog.getFontPanel();
    }

    private void setupMainTextArea()
    {
        channelScroll.setPreferredSize(new Dimension(Constants.MAIN_WIDTH - usersListWidth, Constants.MAIN_HEIGHT - BOTTOM_HEIGHT));
        channelScroll.setLocation(0, 0);
        channelScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        channelTextArea.addMouseListener(new ChannelClickListener());
        channelTextArea.addMouseMotionListener(new ChannelMovementListener());
        channelTextArea.setEditable(false);
        channelTextArea.setFont(getFontPanel().getFont());
        channelTextArea.setEditorKit(new StyledEditorKit());
    }

    private void setupUsersList()
    {
        usersList.setFont(getFontPanel().getFont());
        usersList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        usersList.setLayoutOrientation(JList.VERTICAL);
        usersList.setVisibleRowCount(-1);
        usersList.addMouseListener(new UsersMouseListener());
        userScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        userScroller.setPreferredSize(new Dimension(usersListWidth, Constants.MAIN_HEIGHT - BOTTOM_HEIGHT));
    }


    private void setupBottomPanel()
    {
        setupTickerPanel();

        bottomPanel.setLayout(new BorderLayout());
        // Set initial sizes and colours
        // bottomPanel.setPreferredSize(new Dimension(Constants.MAIN_WIDTH, BOTTOM_HEIGHT * 2));
        // bottomPanel.setBackground(Color.BLACK);

        bottomPanel.add(clientTextBox, BorderLayout.NORTH);
        bottomPanel.add(tickerPanel);

        clientTextBox.setFont(getFontPanel().getFont());
        clientTextBox.addActionListener(new SendTextListener());
        clientTextBox.addKeyListener(new ChannelKeyListener());
        clientTextBox.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
    }

    private void setupTickerPanel()
    {
        tickerPanel.setFont(getFontPanel().getFont());
        tickerPanel.setPreferredSize(labelSize);
        tickerPanel.setBackground(Color.LIGHT_GRAY);
        tickerPanel.setLayout(null);
        tickerPanel.addMouseListener(eventTickerListener);
    }

    public class IRCAlert extends JLabel
    {
        /**
         *
         */
        @Serial
        private static final long serialVersionUID = 1L;
        AlertType type;

        IRCAlert(AlertType type)
        {
            this.type = type;
        }

    }

    class ChannelClickListener extends MouseInputAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            StyledDocument doc = (StyledDocument) channelTextArea.getDocument();
            Element ele = doc.getCharacterElement(channelTextArea.viewToModel2D((e.getPoint())));
            AttributeSet as = ele.getAttributes();
            ClickableText isClickableText = (ClickableText) as.getAttribute("clickableText");
            if(isClickableText != null)
            {
                if (SwingUtilities.isRightMouseButton(e) && isClickableText.rightClickMenu() != null)
                {
                    isClickableText.rightClickMenu().show(e.getComponent(), e.getX(), e.getY());
                } else if (SwingUtilities.isLeftMouseButton(e))
                {
                    isClickableText.execute();
                }
            }
        }
    }

    class ChannelMovementListener extends MouseAdapter
    {
        public void mouseMoved(MouseEvent e) {
            StyledDocument doc = (StyledDocument) channelTextArea.getDocument();
            Element wordElement = doc.getCharacterElement(channelTextArea.viewToModel2D((e.getPoint())));
            AttributeSet wordAttributeSet = wordElement.getAttributes();
            ClickableText isClickableText = (ClickableText) wordAttributeSet.getAttribute("clickableText");
            if (isClickableText != null && gui.isClickableLinksEnabled())
            {
                channelTextArea.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                channelTextArea.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    public void createEvent(String eventText)
    {
        eventTickerTimer.setDelay(gui.getEventTickerDelay());
        if (gui.isJoinsQuitsTickerEnabled())
        {
            JLabel tempLabel = new JLabel(eventText);
            int tempX;
            if (!(eventLabels.isEmpty()))
            {
                if (eventLabels.get(eventLabels.size() - 1).getPreferredSize().width
                        + eventLabels.get(eventLabels.size() - 1).getX() + EVENT_BUFFER < super.getWidth())
                {
                    tempX = super.getWidth() + EVENT_BUFFER;
                } else
                {
                    tempX = eventLabels.get(eventLabels.size() - 1).getPreferredSize().width
                            + eventLabels.get(eventLabels.size() - 1).getX() + EVENT_BUFFER;
                }
            } else
            {
                tempX = super.getWidth();
            }
            // Get the preferred size as this will be long enough to contain the entire string
            int tempLabelWidth = (int) tempLabel.getPreferredSize().getWidth();
            int tempLabelHeight = (int) tempLabel.getPreferredSize().getHeight();
            // Ensures we don't get overlaps of labels. tempX is the width of the last label
            // which is then used as the x position plus the width of the frame
            tempLabel.setBounds(tempX, 0, tempLabelWidth, tempLabelHeight);
            // tempLabel.setBackground(Color.BLACK);
            tempLabel.setForeground(URColour.getInvertedColour(tickerPanel.getBackground()));
            // Add the label to the list of labels
            eventLabels.add(tempLabel);
            // Add it to the actual panel
            tickerPanel.add(tempLabel);
            // if the timer hasn't already started, then start it.
            if (!(eventTickerTimer.isRunning()))
                eventTickerTimer.start();
        }

        if (gui.isJoinsQuitsMainEnabled())
            printText(eventText, Constants.EVENT_USER);
    }

    public void callForAttention()
    {
        myActions.callForAttention();
    }

    // TODO: Change this to accept IRCUser instead
    public void printText(String line, String fromUser)
    {
        if(null == channelTextArea)
        {
            System.out.println("Cant print, shutting down");
            return;
        }

        DateFormat chatDateFormat = new SimpleDateFormat("HHmm");
        Date chatDate = new Date();
        String timeLine = "";

        if (gui.isTimeStampsEnabled())
            timeLine = "[" + chatDateFormat.format(chatDate) + "]";

        if (gui.isChannelHistoryEnabled())
        {
            try
            {
                writeHistoryFile(line);
            } catch (IOException e)
            {
                Constants.LOGGER.log(Level.WARNING, e.getLocalizedMessage());
            }
        }

        StyledDocument doc = (StyledDocument) channelTextArea.getDocument();
        IRCUser fromIRCUser = getCreatedUsers(fromUser);

        // If we received a message from a user that isn't in the channel
        // then add them to the users list.
        // But don't add them if it's from the Event Ticker
        if (fromIRCUser == null)
        {
            if (!fromUser.equals(Constants.EVENT_USER))
            {
                addToUsersList(getName(), fromUser);
                fromIRCUser = getCreatedUsers(fromUser);
            }
        }


        if (fromUser.equals(Constants.EVENT_USER) || !fromIRCUser.isMuted())
        {
            lineFormatter.formattedDocument(doc, timeLine, fromIRCUser, fromUser, line);

            if (lineFormatter.nameStyle.getAttribute("name") == lineFormatter.highStyle().getAttribute("name"))
            {
                callForAttention();
            }

            // Always alert on IRCPrivate messages
            if(this instanceof IRCPrivate)
            {
                callForAttention();
            }

            // TODO: Scrolls to the bottom of the channelTextArea on message received, this should be disabled
            // when the user has scrolled up
            channelTextArea.setCaretPosition(channelTextArea.getDocument().getLength());
        }
    }


    /**
     * First checks to make sure the user hasn't set it manually for this channel. usersListShown is
     * only set by the pop up menu, so unless you've changed it, it won't care about the global setting
     *
     * @param showIt
     */
    public void toggleUsersList()
    {
        if (usersListShown == null || !usersListShown)
        {
            // userScroller.setVisible(showIt);
            mainResizer.setDividerLocation(gui.getWidth());
        } else {
            mainResizer.setDividerLocation(gui.getWidth() - (userScroller.getPreferredSize().width + mainResizer.getDividerSize()));
        }
    }

    /**
     * First checks to make sure the user hasn't set it manually for this channel. eventTickerShown is
     * only set by the pop up menu, so unless you've changed it, it won't care about the global setting
     *
     * @param showIt
     */
    public void showEventTicker(Boolean showIt)
    {
        if (eventTickerShown == showIt || eventTickerShown == null)
        {
            tickerPanel.setVisible(showIt);
            if (tickerPanel.isVisible())
                tickerPanel.setPreferredSize(labelSize);
            else
                tickerPanel.setPreferredSize(new Dimension(0, 0));
        }
    }

    /**
     * Return the appropriate created IRC User
     *
     * @param roomName
     * @return IRCChannel
     */
    public IRCUser getCreatedUsers(String userName)
    {
        for (IRCUser tempUser : usersArray)
        {
            if (tempUser.getName().equalsIgnoreCase(userName))
                return tempUser;
        } ;

        return null;
    }

    public void doLimitLines()
    {
        if (gui.isLimitedChannelActivity())
        {
            String[] tempText = channelTextArea.getText().split("\n");
            int linesCount = tempText.length;

            if (linesCount >= gui.getLimitChannelLinesCount())
            {
                String newText = channelTextArea.getText().replace(tempText[0] + "\n", "");
                channelTextArea.setText(newText);
            }
        }
    }

    public void disableFocus()
    {
        channelTextArea.setFocusable(false);
    }

    public void enableFocus()
    {
        channelTextArea.setFocusable(true);
    }

    // Adds users to the list in the users array[]
    public void addToUsersList(final String channel, final String[] users)
    {
        // Removed as Runnable(), not sure it was necessary
        // TODO: maybe readd Runnable
        if (users.length >= 0 && null != getServer())
        {
            for (int x = 0; x < users.length; x++)
            {
                String tempUserName = users[x];
                if (users[x].startsWith(":"))
                    tempUserName = tempUserName.substring(1);

                IRCUser newUser = getServer().getIRCUser(tempUserName);

                if (!usersArray.contains(newUser))
                    usersArray.add(newUser);
            }
        }
        usersListModel.sort();
    }

    // Adds a single user, good for when a user joins the channel
    public void addToUsersList(final String channel, final String user)
    {
        // Removed as Runnable(), not sure it was necessary
        String thisUser = user;
        if (user.startsWith(":"))
            thisUser = user.substring(1);

        IRCUser newUser = getServer().getIRCUser(thisUser);

        if (!usersArray.contains(newUser))
        {
            usersArray.add(newUser);
            usersList.setSelectedIndex(0);
            createEvent("++ " + thisUser + " has entered " + channel);
            usersListModel.sort();
        }
    }

    public String getChannelTopic(String roomName)
    {
        return getChannelTopic();
    }

    /**
     * Removes a single user, good for when a user leaves the channel
     *
     * @param channel
     * @param user
     **/
    public void removeFromUsersList(final String channel, final String user)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                String thisUser = user;
                if (user.startsWith(":"))
                    thisUser = user.substring(1);


                int index = 0;
                while(index < usersArray.size())
                {
                    if (usersArray.get(index).getName().matches(thisUser))
                    {
                        usersArray.remove(index);
                        createEvent("-- " + thisUser + " has quit " + channel);
                    } else {
                        index++;
                    }
                }

                usersListModel.sort();
            }
        });
    }

    /** Clear the users list */
    public void clearUsersList(String channel)
    {
        usersArray.clear();
    }


    public String getChannelTopic()
    {
        return channelTopic;
    }

    public void setChannelTopic(String channelTopic)
    {
        this.channelTopic = channelTopic;
        this.createEvent(channelTopic);
    }

    public void writeHistoryFile(String line) throws IOException
    {
        if (gui.saveChannelHistory())
        {
            if(historyFileName == null || historyFileName.isEmpty())
            {
                historyFileName = historyDateFormat.format(todayDate) + " " + getName() + ".log";
            }

            FileWriter fw = new FileWriter(Constants.DIRECTORY_LOGS + historyFileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter outFile = new PrintWriter(bw);
            outFile.println(line);
            outFile.close();
        }
    }

    /** Rename user by removing old name and inserting new name. */
    public void renameUser(final String oldUserName, final String newUserName)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                IRCUser tempUser = getCreatedUsers(oldUserName);
                if (tempUser != null)
                {
                    createEvent("!! " + oldUserName + " changed name to " + newUserName);
                    tempUser.setName(newUserName);
                }
            }
        });
    }

    class WrapEditorKit extends StyledEditorKit
    {
        /**
         *
         */
        private static final long serialVersionUID = 980393121518733188L;
        ViewFactory defaultFactory = new WrapColumnFactory();

        public ViewFactory getViewFactory()
        {
            return defaultFactory;
        }

    }

    class WrapColumnFactory implements ViewFactory
    {
        public View create(Element elem)
        {
            String kind = elem.getName();

            return switch (kind) {
                case AbstractDocument.ContentElementName -> new WrapLabelView(elem);
                case AbstractDocument.ParagraphElementName -> new ParagraphView(elem);
                case AbstractDocument.SectionElementName -> new BoxView(elem, View.Y_AXIS);
                case StyleConstants.ComponentElementName -> new ComponentView(elem);
                case StyleConstants.IconElementName -> new IconView(elem);
                default -> new LabelView(elem);
            };
        }
    }

    class WrapLabelView extends LabelView
    {
        public WrapLabelView(Element elem)
        {
            super(elem);
        }

        public float getMinimumSpan(int axis)
        {
            return switch (axis) {
                case View.X_AXIS -> 0;
                case View.Y_AXIS -> super.getMinimumSpan(axis);
                default -> throw new IllegalArgumentException("Invalid axis: " + axis);
            };
        }
    }

    public class ChannelPopUp extends JPopupMenu
    {
        /**
         *
         */
        @Serial
        private static final long serialVersionUID = 640768684923757684L;
        JMenuItem nameItem;
        JMenuItem quitItem;
        JMenuItem hideUsersItem;
        JMenuItem hideTickerItem;
        public JMenuItem addAsFavouriteItem;
        JMenuItem chooseFont;

        public ChannelPopUp()
        {
            nameItem = new JMenuItem(IRCRoomBase.this.getName());
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

            addAsFavouriteItem = new JMenuItem("Add as Favourite");
            add(addAsFavouriteItem);
            addAsFavouriteItem.addActionListener(new AddAsFavourite());
            //
            chooseFont = new JMenuItem("Show Font Dialog");
            add(chooseFont);
            chooseFont.addActionListener(new ChooseFont());
        }

        @Override
        public void show(Component arg0, int arg1, int arg2)
        {
            if (gui.isFavourite(IRCRoomBase.this))
            {
                ((ChannelPopUp) IRCRoomBase.this.myMenu).addAsFavouriteItem.setText("Remove as Favourite");
            } else
            {
                ((ChannelPopUp) IRCRoomBase.this.myMenu).addAsFavouriteItem.setText("Add as Favourite");
            }

            super.show(arg0, arg1, arg2);
        }
    }

    private class AddAsFavourite implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            if(null != getServer())
            {
                if (!gui.isFavourite(IRCRoomBase.this))
                {
                    gui.addFavourite(getServer().getName(), getName());
                } else
                {
                    gui.removeFavourite(getServer().getName(), getName());
                }
            }
        }
    }

    private class QuitItem implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            if(null != getServer())
            {
                getServer().sendClientText("/part i'm outta here", getName());
            }
        }
    }

    /**
     * Used by the PopUpMenu to Toggle the Ticker
     *
     * @author Matt
     *
     */
    private class ToggleHideTickerListItem implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            eventTickerShown = !tickerPanel.isVisible();
            showEventTicker(!tickerPanel.isVisible());
        }
    }

    /**
     * Used by the PopUpMenu to Toggle the Ticker
     *
     * @author Matt
     *
     */
    private class ToggleHideUsersListItem implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {

            if(mainResizer.getDividerLocation() <= gui.getWidth() - (userScroller.getPreferredSize().width + mainResizer.getDividerSize()) )
            {
                usersListShown = false;
            } else {
                usersListShown = true;
            }
            toggleUsersList();
        }
    }

    private class ChooseFont implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            fontDialog.setVisible(true);
        }
    }

    private class SendTextListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
                if (!getUserTextBox().getText().trim().isEmpty())
                {
                    sendClientText(clientTextBox.getText(), getName());
                    if (gui.isClientHistoryEnabled())
                        userHistory.add(clientTextBox.getText());
                }
                clientTextBox.setText("");
        }
    }

    public void sendClientText(String line, String source)
    {
        if (null != getServer() && getServer().isConnected())
        {
            getServer().sendClientText(line, source);
        }
    }

    @Override
    public void setFont(Font f)
    {
        if(fontDialog != null)
        {
            fontDialog.getFontPanel().setDefaultFont(f);
            super.setFont(fontDialog.getFontPanel().getFont());
        } else {
            super.setFont(f);
        }
    }

    private class SaveFontListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            // fontDialog.saveFont(fontDialog.getFont());
            fontDialog.getFontPanel().setFont(fontDialog.getFontPanel().getFont(), true);
            setFont(fontDialog.getFontPanel().getFont());
        }
    }

    public void quitChannel ()
    {
        eventTickerTimer.stop();
        tickerPanel.setVisible(false);
        usersList.setVisible(false);
        removeAll();
        revalidate();
        repaint();
    }

    public boolean userIsTyping ()
    {
        return !clientTextBox.getText().isEmpty();
    }

    /**
     * Tab Auto Complete between where the Caret is positioned and the index of the last space.
     * Therefore it must be the last word of the sentence.
     *
     * @author Matt
     *
     */
    private class ChannelKeyListener implements KeyListener
    {
        public void keyPressed(KeyEvent e)
        {
            // When the user presses tab
            if (e.getKeyCode() == KeyEvent.VK_TAB)
            {
                // If we haven't already started pressing tab startingCharacters will be null
                if (startingCharacters == null)
                {
                    // If it's not the first word then get from where the last space is
                    if (clientTextBox.getText().lastIndexOf(" ") >= 0)
                    {
                        // get text between last space and caretPosition
                        for (String word : clientTextBox.getText().split(" "))
                        {
                            if (startingCharacters == null
                                    || (clientTextBox.getText().indexOf(word) < clientTextBox.getCaretPosition()
                                            && clientTextBox.getText().indexOf(word) > clientTextBox.getText()
                                                    .indexOf(startingCharacters)))
                            {
                                startingCharacters = word;
                            }
                        }

                        // startingCharacters =
                        // clientTextBox.getText().toLowerCase().substring(clientTextBox.getText().lastIndexOf("
                        // ")+1, clientTextBox.getCaretPosition());
                    } else
                    {
                        startingCharacters =
                                clientTextBox.getText().toLowerCase().substring(0, clientTextBox.getCaretPosition());
                    }
                }

                // If usersArray and clientText isn't empty.
                if (usersArray.size() > 0 && clientTextBox.getText().length() > 0)
                {
                    usersArray.stream()
                            .filter(user -> user.getName().toLowerCase().replace("@", "")
                                    .startsWith(startingCharacters.toLowerCase()))
                            .forEach(user -> autoCompleteNames.add(user.getName()));

                    // If the matches arean't already in autoCompleteNames
                    if (!autoCompleteNames.isEmpty())
                    {

                        String nextUser;
                        int currentCaretPosition = clientTextBox.getCaretPosition();
                        // If we haven't already chosen a previous match, starting from the
                        // beginning
                        if (lastUserToComplete == null)
                        {
                            lastUserToComplete = autoCompleteNames.get(0);
                            nextUser = autoCompleteNames.get(0);
                        } else
                        {
                            currentCaretPosition = clientTextBox.getText().indexOf(lastUserToComplete);
                            // Otherwise choose the next one along, or go back to the start if its
                            // the last match
                            if ((autoCompleteNames.indexOf(lastUserToComplete) + 1) == autoCompleteNames.size())
                                nextUser = autoCompleteNames.get(0);
                            else
                                nextUser = autoCompleteNames.get(autoCompleteNames.indexOf(lastUserToComplete) + 1);
                        }

                        // If the lastUser is already in the clientTextBox then just replace it
                        // otherwise put it where the cursor and the last space is
                        int completionLength = nextUser.length();
                        if (clientTextBox.getText().contains(lastUserToComplete))
                        {
                            // TODO: this should only replace the text closest to the caret
                            // position.
                            clientTextBox.setText(
                                    clientTextBox.getText().replace(lastUserToComplete + ": ", nextUser + ": "));
                            completionLength += 2;
                        } else
                        {
                            String textAfterCaret = clientTextBox.getText().substring(clientTextBox.getCaretPosition());
                            clientTextBox.setText(clientTextBox.getText().substring(0,
                                    (clientTextBox.getCaretPosition() - startingCharacters.length()))
                                    + (nextUser + ": ") + textAfterCaret);
                        }
                        clientTextBox.setCaretPosition((currentCaretPosition + completionLength));
                        lastUserToComplete = nextUser;
                    }
                }
            } else
            {
                int nextTextInt = 0;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> {
                        if (!userHistory.isEmpty()) {
                            nextTextInt = userHistory.indexOf(clientTextBox.getText()) - 1;
                            if (nextTextInt < 0)
                                nextTextInt = userHistory.size() - 1;

                            clientTextBox.setText(userHistory.get(nextTextInt));
                        }
                    }
                    case KeyEvent.VK_DOWN -> {
                        if (!userHistory.isEmpty()) {
                            nextTextInt = userHistory.indexOf(clientTextBox.getText()) + 1;
                            if (nextTextInt > userHistory.size() - 1)
                                nextTextInt = 0;

                            clientTextBox.setText(userHistory.get(nextTextInt));
                        }
                    }
                    case KeyEvent.VK_ESCAPE -> clientTextBox.setText("");
                    default -> {
                        if (lastUserToComplete != null)
                            lastUserToComplete = null;
                        if (startingCharacters != null)
                            startingCharacters = null;
                        if (!autoCompleteNames.isEmpty())
                            autoCompleteNames.clear();
                    }
                }
            }
        }

        public void keyTyped(KeyEvent ke)
        {}

        public void keyReleased(KeyEvent ke)
        {}
    }

    // Repaints the window, delayed by EVENT_DELAY
    private class TickerAction implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    if (IRCRoomBase.this.tickerPanel.isVisible())
                    {
                        Iterator<JLabel> labelIterator = eventLabels.iterator();
                        while (labelIterator.hasNext()) {
                            JLabel tempLabel = labelIterator.next();
                            tempLabel.setLocation(tempLabel.getX() - EVENT_VELOCITY, 0);
                            if (tempLabel.getX() + tempLabel.getWidth() < 0) {
                                labelIterator.remove(); // Safely remove the element
                                tickerPanel.remove(tempLabel);
                            }
                        }

                        if (eventLabels.isEmpty())
                            eventTickerTimer.stop();

                        if(DriverGUI.frame.isFocused())
                        {
                            tickerPanel.revalidate();
                            tickerPanel.repaint();
                        }
                    } else
                    {
                        eventTickerTimer.stop();

                        for (JLabel tempLabel : eventLabels) {
                            tickerPanel.remove(tempLabel);
                        }

                        eventLabels.clear();
                    }
                }
            });
        }
    }

    private class TickerListener extends MouseInputAdapter
    {
        public void mouseEntered(MouseEvent e)
        {
            eventTickerTimer.setDelay(gui.getEventTickerDelay() * 10);
        }

        public void mouseExited(MouseEvent e)
        {
            eventTickerTimer.setDelay(gui.getEventTickerDelay());
        }
    }

    /**
     * Used for when you right click on a user - displays the menu.
     *
     * @author Matt
     *
     */
    class UsersMouseListener extends MouseInputAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            final int index = usersList.locationToIndex(e.getPoint());
            if (index > -1)
            {
                usersList.setSelectedIndex(index);
                IRCUser userName = usersList.getSelectedValue();

                if (SwingUtilities.isRightMouseButton(e))
                    userName.myMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
