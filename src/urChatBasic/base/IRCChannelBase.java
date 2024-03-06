package urChatBasic.base;

import urChatBasic.backend.logging.URLogger;
import urChatBasic.backend.utils.ReverseLineInputStream;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.IRCChannelBase;
import urChatBasic.base.Constants.EventType;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.IRCActions;
import urChatBasic.frontend.IRCPrivate;
import urChatBasic.frontend.IRCServer;
import urChatBasic.frontend.IRCUser;
import urChatBasic.frontend.LineFormatter;
import urChatBasic.frontend.LineFormatter.ClickableText;
import urChatBasic.frontend.components.FontPanel;
import urChatBasic.frontend.dialogs.FontDialog;
import urChatBasic.frontend.panels.ConnectionPanel;
import urChatBasic.frontend.panels.InterfacePanel;
import urChatBasic.frontend.utils.URColour;
import urChatBasic.frontend.UserGUI;
import urChatBasic.frontend.UsersListModel;
import java.awt.event.*;
import static urChatBasic.backend.utils.LogPatternParser.parseLogLineFull;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.*;

public class IRCChannelBase extends JPanel
{
    // Channel information
    private String channelName;

    // Preferences
    private Preferences channelPrefs;

    // IRCServer information (Owner of channel)
    protected IRCServerBase server;
    protected IRCActions myActions;
    protected UserGUI gui = DriverGUI.gui;

    protected FontDialog fontDialog;

    // Logging Stuff
    private String markerName;

    // Icons
    public ImageIcon icon;

    public JPopupMenu myMenu;

    // Main Panel
    protected JPanel mainPanel = new JPanel();
    protected JSplitPane mainResizer = new JSplitPane();
    protected ActionListener changeListener = new ProfileChangeListener();

    // Bottom panel to hold the user text box
    protected JTextField clientTextBox = new JTextField();
    protected JPanel bottomPanel = new JPanel();
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

    private String channelTopic;

    // Tab complete stuff
    // TODO: Rename to tab complete
    private String startingCharacters = null;
    private String lastUserToComplete = null;
    private List<String> autoCompleteNames = new ArrayList<String>();

    // Text Area
    private JTextPane channelTextArea = new JTextPane();
    protected JScrollPane channelScroll = new JScrollPane(channelTextArea);
    private BlockingQueue<Message> messageQueue = new ArrayBlockingQueue<>(Constants.MAXIMUM_QUEUE_SIZE);
    public boolean messageQueueInProgress = false;
    private LineFormatter lineFormatter;


    // Users list area
    // TODO: Users should be created per Server, and instead have a property to hold what channels
    // they're in
    private ConcurrentHashMap<String, IRCUser> usersMap = new ConcurrentHashMap<>();
    protected UsersListModel usersListModel = new UsersListModel();
    @SuppressWarnings("unchecked")
    private JList<IRCUser> usersList = new JList<IRCUser>(usersListModel);
    protected JScrollPane userScroller = new JScrollPane(usersList);
    private Boolean usersListShown = null;
    private int usersListWidth = 100;

    // getters & setters

    public JTextField getUserTextBox ()
    {
        return clientTextBox;
    }

    public IRCServerBase getServer ()
    {
        if (this instanceof IRCServer)
        {
            return (IRCServerBase) this;
        }

        return this.server;
    }

    @Override
    public String getName ()
    {
        return this.channelName;
    }

    @Override
    public void setName (String newName)
    {
        channelName = newName;
    }

    public void hideEventTicker ()
    {
        eventTickerShown = false;
        tickerPanel.setVisible(false);
        bottomPanel.setPreferredSize(clientTextBox.getPreferredSize());
    }

    public void hideUsersList ()
    {
        usersListShown = false;
        toggleUsersList(usersListShown);
    }

    public void showUsersList ()
    {
        usersListShown = true;
        toggleUsersList(usersListShown);
    }

    protected IRCChannelBase (String channelName)
    {
        this.channelName = channelName;
        initChannel();
    }

    protected IRCChannelBase (IRCServerBase server, String channelName)
    {
        this.channelName = channelName;
        setServer(server);
        initChannel();
    }

    public void setServer (IRCServerBase server)
    {
        this.server = server;
    }

    private void initChannel ()
    {
        channelTextArea.setEditable(false);

        if (getServer() != null)
        {
            String nodeName = getServer().getName() != null ? getServer().getName() : channelName;
            markerName = getServer().getName() != null ? getServer().getName() + "-" + channelName : channelName;

            if (nodeName.equals(channelName))
                setSettingsPath(URProfilesUtil.getActiveFavouritesPath().node(nodeName));
            else
                setSettingsPath(URProfilesUtil.getActiveFavouritesPath().node(nodeName).node(channelName));

            fontDialog = new FontDialog(channelName, gui.getStyle(), channelPrefs);

            lineFormatter = new LineFormatter(getFontPanel().getStyle(), channelTextArea, channelScroll, getServer(), channelPrefs);
        } else
        {
            markerName = channelName;
            setSettingsPath(URProfilesUtil.getActiveFavouritesPath().node(channelName));
            fontDialog = new FontDialog(channelName, gui.getStyle(), channelPrefs);

            lineFormatter = new LineFormatter(getFontPanel().getStyle(), channelTextArea, channelScroll, null, channelPrefs);
        }

        // Add Logging Marker
        URLogger.addChannelMarker(markerName);

        URProfilesUtil.addListener(EventType.CHANGE, changeListener);

        setFont(getFontPanel().getFont());

        // Create the initial size of the panel
        // Set size of the overall panel
        setPreferredSize(new Dimension(Constants.MAIN_WIDTH, Constants.MAIN_HEIGHT));
        setupMainPanel();

        setName(channelName);
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);

        // this.myMenu = new ChannelPopUp();
        createChannelPopUp();
        fontDialog.setVisible(false);
        fontDialog.addFontSaveListener(new SaveFontListener());

        myActions = new IRCActions(this);

        if (((InterfacePanel) gui.interfacePanel).isLoadChannelLogsEnabled())
            loadChannelHistory();

    }

    private class ProfileChangeListener implements ActionListener
    {

        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            String nodeName = getServer().getName() != null ? getServer().getName() : channelName;

            if (nodeName.equals(channelName))
                setSettingsPath(URProfilesUtil.getActiveFavouritesPath().node(nodeName));
            else
                setSettingsPath(URProfilesUtil.getActiveFavouritesPath().node(nodeName).node(channelName));
        }

    }

    public void rejoin ()
    {
        server.sendClientText("/join " + getName(), server.getName());
    }

    public void setSettingsPath (Preferences settingsPath)
    {
        channelPrefs = settingsPath;
        if (getFontPanel() != null)
        {
            getFontPanel().setSettingsPath(settingsPath);
            setFont(getFontPanel().getFont());
        }
    }

    public Preferences getSettingsPath ()
    {
        return channelPrefs;
    }

    public void createChannelPopUp ()
    {
        myMenu = new ChannelPopUp();
    }

    protected void setupMainPanel ()
    {
        mainPanel.setLayout(new BorderLayout());
        setupMainTextArea();
        // mainPanel.add(channelScroll, BorderLayout.CENTER);
        setupUsersList();
        // mainPanel.add(userScroller, BorderLayout.LINE_END);

        // Create a split pane with the two scroll panes in it.
        mainResizer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, channelScroll, userScroller);
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

    public FontPanel getFontPanel ()
    {
        return fontDialog != null && fontDialog.getFontPanel() != null ? fontDialog.getFontPanel() : null;
    }

    protected void setupMainTextArea ()
    {
        channelScroll.setPreferredSize(new Dimension(Constants.MAIN_WIDTH - usersListWidth, Constants.MAIN_HEIGHT - BOTTOM_HEIGHT));
        channelScroll.setLocation(0, 0);
        channelScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        channelTextArea.addMouseListener(new ChannelClickListener());
        channelTextArea.addMouseMotionListener(new ChannelMovementListener());
        // channelTextArea.getDocument().addDocumentListener(new LimitLinesDocumentListener(gui.getLimitChannelLinesCount()));
        // channelTextArea.getDocument().addDocumentListener(new LineLimitListener());
        // channelTextArea.setEditable(false);
        // channelTextArea.setFont(getFontPanel().getFont());
        // channelTextArea.setEditorKit(new StyledEditorKit());
        // This is needed because the channelTextArea isn't the same after it was initialized.

        // resetLineFormatter();
    }

    protected void setupUsersList ()
    {
        usersList.setFont(getFontPanel().getFont());
        usersList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        usersList.setLayoutOrientation(JList.VERTICAL);
        usersList.setVisibleRowCount(-1);
        usersList.addMouseListener(new UsersMouseListener());
        userScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        userScroller.setPreferredSize(new Dimension(usersListWidth, Constants.MAIN_HEIGHT - BOTTOM_HEIGHT));
    }


    protected void setupBottomPanel ()
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

    private void setupTickerPanel ()
    {
        tickerPanel.setFont(getFontPanel().getFont());
        tickerPanel.setPreferredSize(labelSize);
        tickerPanel.setBackground(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING));
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

        IRCAlert (AlertType type)
        {
            this.type = type;
        }

    }

    public class ChannelClickListener extends MouseInputAdapter
    {
        public void mouseClicked (MouseEvent e)
        {
            StyledDocument doc = (StyledDocument) channelTextArea.getDocument();
            Element ele = doc.getCharacterElement(channelTextArea.viewToModel2D((e.getPoint())));
            AttributeSet as = ele.getAttributes();
            ClickableText isClickableText = (ClickableText) as.getAttribute("clickableText");
            if (isClickableText != null)
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
        public void mouseMoved (MouseEvent e)
        {
            StyledDocument doc = (StyledDocument) channelTextArea.getDocument();
            Element wordElement = doc.getCharacterElement(channelTextArea.viewToModel2D((e.getPoint())));
            AttributeSet wordAttributeSet = wordElement.getAttributes();
            ClickableText isClickableText = (ClickableText) wordAttributeSet.getAttribute("clickableText");
            if (isClickableText != null && ((InterfacePanel) gui.interfacePanel).isClickableLinksEnabled())
            {
                channelTextArea.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else
            {
                channelTextArea.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    public void createEvent (String eventText)
    {
        if (((InterfacePanel) gui.interfacePanel).isJoinsQuitsMainEnabled() && !(this instanceof IRCPrivate))
            printText(eventText, Constants.EVENT_USER);

        eventTickerTimer.setDelay(((InterfacePanel) gui.interfacePanel).getEventTickerDelay());
        if (((InterfacePanel) gui.interfacePanel).isJoinsQuitsTickerEnabled() && !(this instanceof IRCPrivate))
        {
            JLabel tempLabel = new JLabel(eventText);
            int tempX;
            if (!(eventLabels.isEmpty()))
            {
                if (eventLabels.get(eventLabels.size() - 1).getPreferredSize().width + eventLabels.get(eventLabels.size() - 1).getX()
                        + EVENT_BUFFER < super.getWidth())
                {
                    tempX = super.getWidth() + EVENT_BUFFER;
                } else
                {
                    tempX = eventLabels.get(eventLabels.size() - 1).getPreferredSize().width + eventLabels.get(eventLabels.size() - 1).getX() + EVENT_BUFFER;
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
    }

    public void callForAttention ()
    {
        myActions.callForAttention();
    }

    public String getMarker ()
    {
        return markerName;
    }

    class Message
    {
        private Optional<Date> date = Optional.empty();
        private String line;
        private String fromUser;

        public Message (String line, String fromUser)
        {
            this.line = line;
            this.fromUser = fromUser;
        }

        public Message (Date date, String line, String fromUser)
        {
            this.date = Optional.of(date);
            this.line = line;
            this.fromUser = fromUser;
        }

        public String getLine ()
        {
            return line;
        }

        public String getUser ()
        {
            return fromUser;
        }

        public Optional<Date> getDate ()
        {
            return date;
        }
    }

    // TODO: Change this to accept IRCUser instead
    // TODO: Overload method with date object
    public void printText (String line, String fromUser)
    {
        try
        {
            messageQueue.put(new Message(line, fromUser));

            if (!messageQueueInProgress)
                handleMessageQueue();

        } catch (InterruptedException e)
        {
            Constants.LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    public void printText (Date messageDate, String message, String fromUser)
    {
        try
        {
            messageQueue.put(new Message(messageDate, message, fromUser));

            if (!messageQueueInProgress)
                handleMessageQueue();

        } catch (InterruptedException e)
        {
            Constants.LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    public boolean messageQueueFull ()
    {

        return (messageQueue.remainingCapacity() == 0);
    }

    public boolean messageQueueWorking ()
    {
        return (!messageQueue.isEmpty() || messageQueueInProgress);
    }

    public void handleMessageQueue ()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run ()
            {
                while (!messageQueue.isEmpty())
                {
                    try
                    {
                        messageQueueInProgress = true;
                        Message message = messageQueue.take();

                        if (null == message)
                        {
                            messageQueueInProgress = false;
                            continue;
                        }

                        Optional<Date> messageDate = message.getDate();
                        String line = message.getLine();
                        String fromUser = message.getUser();

                        int lineLimit = ((InterfacePanel) gui.interfacePanel).getLimitChannelLinesCount();

                        if (IRCChannelBase.this instanceof IRCServer)
                            lineLimit = ((InterfacePanel) gui.interfacePanel).getLimitServerLinesCount();

                        if (null != message && getLineFormatter().getLineCount() > lineLimit)
                        {
                            getLineFormatter().removeFirstLine();
                        }

                        if (null == channelTextArea)
                        {
                            Constants.LOGGER.error("ChannelTextArea hasn't initialized or has disappeared.. not printing text.");
                            return;
                        }


                        // StyledDocument doc = channelTextArea.getStyledDocument();
                        IRCUser fromIRCUser = getCreatedUser(fromUser);

                        // If we received a message from a user that isn't in the channel
                        // then add them to the users list.
                        // But don't add them if it's from the Event Ticker or we're loading historical
                        if (fromIRCUser == null)
                        {
                            if (!fromUser.equals(Constants.EVENT_USER))
                            {
                                // TODO: Re-add later?
                                // addToUsersList(getName(), fromUser);
                                // fromIRCUser = getCreatedUsers(fromUser);
                                // Constants.LOGGER.error("Message from a user that isn't in the user list!");
                                fromIRCUser = server.getIRCUser(fromUser);

                                // Only add users if we haven't specified a date, which means we aren't loading historical messages
                                if (messageDate.isEmpty())
                                    addToUsersList(fromIRCUser);
                            }
                        }

                        if (fromUser.equals(Constants.EVENT_USER) || !fromIRCUser.isMuted())
                        {
                            lineFormatter.appendMessage(messageDate, fromIRCUser, fromUser, line);

                            // Only save history and call events if we aren't loading historical messages
                            if (messageDate.isEmpty())
                            {
                                if (IRCChannelBase.this instanceof IRCServerBase)
                                {
                                    if (((InterfacePanel) gui.interfacePanel).saveServerHistory())
                                        URLogger.logChannelComms(IRCChannelBase.this, (fromIRCUser != null ? fromIRCUser.getName() : fromUser) + ": " + line);
                                } else if (!(IRCChannelBase.this instanceof IRCServerBase))
                                {
                                    if (((InterfacePanel) gui.interfacePanel).saveChannelHistory())
                                        URLogger.logChannelComms(IRCChannelBase.this, (fromIRCUser != null ? fromIRCUser.getName() : fromUser) + ": " + line);
                                }

                                if (server.getNick() != null && line.indexOf(server.getNick()) > -1)
                                {
                                    callForAttention();
                                }

                                // Always alert on IRCPrivate messages
                                if (IRCChannelBase.this instanceof IRCPrivate)
                                {
                                    callForAttention();
                                }

                                // TODO: Scrolls to the bottom of the channelTextArea on message received, this should be
                                // disabled
                                // when the user has scrolled up
                                // channelTextArea.setCaretPosition(channelTextArea.getDocument().getLength());
                            }
                        }

                    } catch (InterruptedException e)
                    {
                        Constants.LOGGER.error(e.getLocalizedMessage());
                    }
                }
                messageQueueInProgress = false;
            }
        });

    }

    /**
     * First checks to make sure the user hasn't set it manually for this channel. usersListShown is only set by the pop up menu, so unless you've changed it,
     * it won't care about the global setting
     *
     * @param showIt
     */
    public void toggleUsersList (boolean showIt)
    {
        if (usersListShown == null || usersListShown == showIt)
        {
            // userScroller.setVisible(showIt);
            if (showIt)
                mainResizer.setDividerLocation(gui.getWidth() - (userScroller.getPreferredSize().width + mainResizer.getDividerSize()));
            else
                mainResizer.setDividerLocation(gui.getWidth());
        }
    }

    /**
     * First checks to make sure the user hasn't set it manually for this channel. eventTickerShown is only set by the pop up menu, so unless you've changed it,
     * it won't care about the global setting
     *
     * @param showIt
     */
    public void toggleEventTicker (boolean showIt)
    {
        if (eventTickerShown == null || eventTickerShown == showIt)
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
     * @param channelName
     * @return IRCChannel
     */
    public IRCUser getCreatedUser (String userName)
    {
        synchronized (usersMap)
        {
            return usersMap.get(userName.toLowerCase());
        }
    }

    public void disableFocus ()
    {
        channelTextArea.setFocusable(false);
    }

    public void enableFocus ()
    {
        channelTextArea.setFocusable(true);
    }

    // Adds users to the list in the users array[]
    public void addToUsersList (final String[] users)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run ()
            {
                synchronized (usersMap)
                {
                    if (users.length >= 0 && null != getServer())
                    {
                        for (int x = 0; x < users.length; x++)
                        {
                            String tempUserName = users[x];
                            if (users[x].startsWith(":"))
                                tempUserName = tempUserName.substring(1);

                            IRCUser newUser = getServer().getIRCUser(tempUserName);

                            if (null != newUser)
                            {
                                usersMap.put(newUser.getName().toLowerCase(), newUser);
                                if (users.length == 1)
                                    createEvent("++ " + newUser + " has joined.");
                                usersListModel.addUser(newUser);
                            }
                        }
                    }
                    usersListModel.sort();
                }
            }
        });
    }

    public void addToUsersList (final IRCUser newUser)
    {
        synchronized (usersMap)
        {
            usersMap.put(newUser.getName().toLowerCase(), newUser);
            createEvent("++ " + newUser + " has joined.");
            usersListModel.addUser(newUser);

            usersListModel.sort();
        }
    }

    // Adds a single user, good for when a user joins the channel
    public void addToUsersList (final String user)
    {
        addToUsersList(new String[] {user});
    }

    public String getChannelTopic (String channelName)
    {
        return getChannelTopic();
    }

    /**
     * Removes a single user, good for when a user leaves the channel
     *
     * @param user
     **/
    public void removeFromUsersList (final String user)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run ()
            {
                String thisUser = user;
                if (user.startsWith(":"))
                    thisUser = user.substring(1);


                usersMap.remove(thisUser.toLowerCase());
                if (usersListModel.hasUser(user))
                {
                    usersListModel.removeUser(thisUser);
                    createEvent("-- " + thisUser + " has quit.");
                    usersListModel.sort();
                }
            }
        });
    }

    /** Clear the users list */
    public void clearUsersList ()
    {
        usersMap.clear();
    }


    public String getChannelTopic ()
    {
        return channelTopic;
    }

    public void setChannelTopic (String channelTopic)
    {
        this.channelTopic = channelTopic;
        this.createEvent(channelTopic);
    }

    /** Rename user by removing old name and inserting new name. */
    public void renameUser (final String oldUserName, final String newUserName)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run ()
            {
                IRCUser tempUser = getCreatedUser(oldUserName);
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

        public ViewFactory getViewFactory ()
        {
            return defaultFactory;
        }

    }

    class WrapColumnFactory implements ViewFactory
    {
        public View create (Element elem)
        {
            String kind = elem.getName();

            return switch (kind)
            {
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
        public WrapLabelView (Element elem)
        {
            super(elem);
        }

        public float getMinimumSpan (int axis)
        {
            return switch (axis)
            {
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
        JMenuItem loadChannelHistory;
        JMenuItem hideUsersItem;
        JMenuItem hideTickerItem;
        public JMenuItem addAsFavouriteItem;
        JMenuItem chooseFont;

        public ChannelPopUp ()
        {
            nameItem = new JMenuItem(IRCChannelBase.this.getName());
            add(nameItem);
            addSeparator();
            //
            quitItem = new JMenuItem("Quit");
            add(quitItem);
            quitItem.addActionListener(new QuitItem());
            //
            loadChannelHistory = new JMenuItem("Load Channel History");
            add(loadChannelHistory);
            loadChannelHistory.addActionListener(new LoadChannelHistory());
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
        public void show (Component arg0, int arg1, int arg2)
        {
            if ((gui.getConnectionPanel() instanceof ConnectionPanel connectionPanel) && connectionPanel.isFavourite(IRCChannelBase.this))
            {
                ((ChannelPopUp) IRCChannelBase.this.myMenu).addAsFavouriteItem.setText("Remove as Favourite");
            } else
            {
                ((ChannelPopUp) IRCChannelBase.this.myMenu).addAsFavouriteItem.setText("Add as Favourite");
            }

            super.show(arg0, arg1, arg2);
        }
    }

    private class AddAsFavourite implements ActionListener
    {

        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            if (null != getServer() && (gui.getConnectionPanel() instanceof ConnectionPanel connectionPanel))
            {
                if (!connectionPanel.isFavourite(IRCChannelBase.this))
                {
                    connectionPanel.addFavourite(getServer().getName(), getName());
                } else
                {
                    connectionPanel.removeFavourite(getServer().getName(), getName());
                }
            }
        }
    }

    private class LoadChannelHistory implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            loadChannelHistory();
        }
    }

    private void loadChannelHistory ()
    {
        Thread fileReadingThread = new Thread( () -> {
            String logFilePath = URLogger.getLogFilePath(getMarker());
            String line = "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new ReverseLineInputStream(new File(logFilePath)))))
            {
                int maxCount = ((InterfacePanel) gui.interfacePanel).getLimitChannelLinesCount();
                Constants.LOGGER.info("Loading channel history, max line count [" + maxCount + "]");
                int loadCount = 0;
                while ((line = br.readLine()) != null && loadCount < maxCount)
                {
                    if (messageQueueFull())
                        Thread.sleep(10);

                    Map<String, Object> parsedLine = parseLogLineFull(line);
                    if (parsedLine.size() != 0)
                    {
                        printText((Date) parsedLine.get("DATE"), parsedLine.get("MESSAGE").toString(), parsedLine.get("USER").toString());
                        loadCount++;
                    }
                }
            } catch (NullPointerException npe)
            {
                Constants.LOGGER.info("Log File doesn't yet exist.");
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
        fileReadingThread.start();
    }

    private class QuitItem implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            if (null != getServer())
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
        public void actionPerformed (ActionEvent arg0)
        {
            eventTickerShown = !tickerPanel.isVisible();
            toggleEventTicker(!tickerPanel.isVisible());
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
        public void actionPerformed (ActionEvent arg0)
        {

            if (mainResizer.getDividerLocation() <= gui.getWidth() - (userScroller.getPreferredSize().width + mainResizer.getDividerSize()))
            {
                usersListShown = false;
            } else
            {
                usersListShown = true;
            }
            toggleUsersList(usersListShown);
        }
    }

    private class ChooseFont implements ActionListener
    {

        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            fontDialog.setVisible(true);
        }
    }

    private class SendTextListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            if (!getUserTextBox().getText().trim().isEmpty())
            {
                sendClientText(clientTextBox.getText(), getName());
                if (((InterfacePanel) gui.interfacePanel).isClientHistoryEnabled())
                    userHistory.add(clientTextBox.getText());
            }
            clientTextBox.setText("");
        }
    }

    public void sendClientText (String line, String source)
    {
        if (null != getServer() && getServer().isConnected())
        {
            getServer().sendClientText(line, source);
        }
    }

    public JTextPane getChannelTextPane ()
    {
        return channelTextArea;
    }

    public LineFormatter getLineFormatter ()
    {
        return lineFormatter;
    }

    @Override
    public void setFont (Font f)
    {
        if (fontDialog != null)
        {
            super.setFont(fontDialog.getFontPanel().getFont());
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run ()
                {
                    // lineFormatter.setFont(fontDialog.getFontPanel().getFont());
                    lineFormatter.setStyle(fontDialog.getFontPanel().getStyle());
                }
            });
        } else
        {
            super.setFont(f);
        }
    }

    private class SaveFontListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            // fontDialog.saveFont(fontDialog.getFont());
            fontDialog.getFontPanel().setFont(fontDialog.getFontPanel().getStyle(), true);
            setFont(fontDialog.getFontPanel().getFont());
        }
    }

    public void closeChannel ()
    {
        URProfilesUtil.removeListener(EventType.CHANGE, changeListener);
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
     * Tab Auto Complete between where the Caret is positioned and the index of the last space. Therefore it must be the last word of the sentence.
     *
     * @author Matt
     *
     */
    private class ChannelKeyListener implements KeyListener
    {
        public void keyPressed (KeyEvent e)
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
                            if (startingCharacters == null || (clientTextBox.getText().indexOf(word) < clientTextBox.getCaretPosition()
                                    && clientTextBox.getText().indexOf(word) > clientTextBox.getText().indexOf(startingCharacters)))
                            {
                                startingCharacters = word;
                            }
                        }

                        // startingCharacters =
                        // clientTextBox.getText().toLowerCase().substring(clientTextBox.getText().lastIndexOf("
                        // ")+1, clientTextBox.getCaretPosition());
                    } else
                    {
                        startingCharacters = clientTextBox.getText().toLowerCase().substring(0, clientTextBox.getCaretPosition());
                    }
                }

                // If usersArray and clientText isn't empty.
                if (!usersMap.isEmpty() && clientTextBox.getText().length() > 0)
                {
                    usersMap.values().stream().filter(user -> user.getName().toLowerCase().replace("@", "").startsWith(startingCharacters.toLowerCase()))
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
                            clientTextBox.setText(clientTextBox.getText().replace(lastUserToComplete + ": ", nextUser + ": "));
                            completionLength += 2;
                        } else
                        {
                            String textAfterCaret = clientTextBox.getText().substring(clientTextBox.getCaretPosition());
                            clientTextBox.setText(clientTextBox.getText().substring(0, (clientTextBox.getCaretPosition() - startingCharacters.length()))
                                    + (nextUser + ": ") + textAfterCaret);
                        }
                        clientTextBox.setCaretPosition((currentCaretPosition + completionLength));
                        lastUserToComplete = nextUser;
                    }
                }
            } else
            {
                int nextTextInt = 0;
                switch (e.getKeyCode())
                {
                    case KeyEvent.VK_UP -> {
                        if (!userHistory.isEmpty())
                        {
                            nextTextInt = userHistory.indexOf(clientTextBox.getText()) - 1;
                            if (nextTextInt < 0)
                                nextTextInt = userHistory.size() - 1;

                            clientTextBox.setText(userHistory.get(nextTextInt));
                        }
                    }
                    case KeyEvent.VK_DOWN -> {
                        if (!userHistory.isEmpty())
                        {
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

        public void keyTyped (KeyEvent ke)
        {}

        public void keyReleased (KeyEvent ke)
        {}
    }

    // Repaints the window, delayed by EVENT_DELAY
    private class TickerAction implements ActionListener
    {
        public void actionPerformed (ActionEvent event)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run ()
                {
                    if (IRCChannelBase.this.tickerPanel.isVisible())
                    {
                        Iterator<JLabel> labelIterator = eventLabels.iterator();
                        while (labelIterator.hasNext())
                        {
                            JLabel tempLabel = labelIterator.next();
                            tempLabel.setLocation(tempLabel.getX() - EVENT_VELOCITY, 0);
                            if (tempLabel.getX() + tempLabel.getWidth() < 0)
                            {
                                labelIterator.remove(); // Safely remove the element
                                tickerPanel.remove(tempLabel);
                            }
                        }

                        if (eventLabels.isEmpty())
                            eventTickerTimer.stop();

                        if (DriverGUI.frame.isFocused())
                        {
                            tickerPanel.revalidate();
                            tickerPanel.repaint();
                        }
                    } else
                    {
                        eventTickerTimer.stop();

                        for (JLabel tempLabel : eventLabels)
                        {
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
        public void mouseEntered (MouseEvent e)
        {
            eventTickerTimer.setDelay(((InterfacePanel) gui.interfacePanel).getEventTickerDelay() * 10);
        }

        public void mouseExited (MouseEvent e)
        {
            eventTickerTimer.setDelay(((InterfacePanel) gui.interfacePanel).getEventTickerDelay());
        }
    }

    /**
     * Used for when you right click on a user - create and display the menu.
     *
     * @author Matt
     *
     */
    class UsersMouseListener extends MouseInputAdapter
    {
        public void mouseClicked (MouseEvent e)
        {
            if (SwingUtilities.isRightMouseButton(e))
            {
                final int index = usersList.locationToIndex(e.getPoint());
                if (index > -1)
                {
                    usersList.setSelectedIndex(index);
                    IRCUser userName = usersList.getSelectedValue();
                    userName.createPopUp();
                    userName.myMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }
}
