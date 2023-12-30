package urChatBasic.frontend;

import java.awt.*;
import java.util.logging.Level;
import java.util.prefs.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import urChatBasic.backend.utils.URPreferencesUtil;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.backend.utils.URStyle;
import urChatBasic.backend.utils.URUncaughtExceptionHandler;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCRoomBase;
import urChatBasic.base.IRCServerBase;
import urChatBasic.frontend.dialogs.FontDialog;
import urChatBasic.frontend.dialogs.MessageDialog;
import urChatBasic.frontend.panels.UROptionsPanel;
import urChatBasic.frontend.utils.Panels;
import urChatBasic.base.UserGUIBase;
import urChatBasic.base.Constants.EventType;
import urChatBasic.base.Constants.Placement;
import urChatBasic.base.Constants.Size;
import urChatBasic.base.capabilities.CapTypeBase;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.frontend.LineFormatter.ClickableText;
import urChatBasic.frontend.components.*;

public class UserGUI extends JPanel implements Runnable, UserGUIBase
{
    /**
     *
     */
    private static final long serialVersionUID = 2595649865577419300L;
    // private String creationTime = (new Date()).toString();
    // Tabs
    public JTabbedPane tabbedPane = new DnDTabbedPane();
    public Component previousSelectedTab;
    public Component currentSelectedTab;

    // Profile Preferences
    protected static EventListenerList profileListenerList = new EventListenerList();
    protected static transient ActionEvent actionEvent = null;

    // Options Panel
    private static JPanel optionsMainPanel = new MainOptionsPanel();
    // Server Options Panel
    private final UROptionsPanel connectionPanel = new UROptionsPanel("Connection", (MainOptionsPanel) optionsMainPanel, Optional.of(0));

    private final UROptionsPanel interfacePanel = new UROptionsPanel("Interface", (MainOptionsPanel) optionsMainPanel, Optional.of(1));

    // Appearance Options Panel
    private final UROptionsPanel appearancePanel = new UROptionsPanel("Appearance", (MainOptionsPanel) optionsMainPanel, Optional.of(2));

    // Profile Panel
    private static final UROptionsPanel profilePanel = new ProfilePanel((MainOptionsPanel) optionsMainPanel);

    // public static final JScrollPane interfaceScroller = new JScrollPane(interfacePanel);

    private static final JComboBox<LookAndFeelInfo> lafOptions =
            new JComboBox<LookAndFeelInfo>(UIManager.getInstalledLookAndFeels());

    private static final JCheckBox showEventTicker = new JCheckBox("Show Event Ticker");
    private static final JCheckBox showUsersList = new JCheckBox("Show Users List");
    private static final JCheckBox enableClickableLinks = new JCheckBox("Make links clickable");
    private static final JCheckBox showJoinsQuitsEventTicker = new JCheckBox("Show Joins/Quits in the Event Ticker");
    private static final JCheckBox showJoinsQuitsMainWindow = new JCheckBox("Show Joins/Quits in the Chat Window");
    private static final JCheckBox logChannelText = new JCheckBox("Save and log all channel text");
    private static final JCheckBox logServerActivity = new JCheckBox("Save and log all Server activity");
    private static final JCheckBox logClientText = new JCheckBox("Log client text (Allows up or down history)");
    private static final JCheckBox limitServerLines = new JCheckBox("Limit the number of lines in Server activity");
    private static final JCheckBox limitChannelLines = new JCheckBox("Limit the number of lines in channel text");
    private static final JCheckBox enableTimeStamps = new JCheckBox("Time Stamp chat messages");

    // Appearance Panel
    private FontPanel clientFontPanel;
    /**
     * This should only ever be changed if the LAF changes
     */
    private static URStyle defaultStyle = new URStyle("", UIManager.getFont(Constants.DEFAULT_FONT_STRING), UIManager.getColor(Constants.DEFAULT_FOREGROUND_STRING),
            UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING));
    private static final JTextField timeStampField = new JTextField();
    public static final JTextPane previewTextArea = new JTextPane();
    private static final JScrollPane previewTextScroll = new JScrollPane(previewTextArea);
    private static final JLabel styleLabel = new JLabel("Mouse over text to view style, right-click to edit.");
    public static LineFormatter previewLineFormatter;


    private static final JTextField limitServerLinesCount = new JTextField();
    private static final JTextField limitChannelLinesCount = new JTextField();

    private static final int TICKER_DELAY_MIN = 0;
    private static final int TICKER_DELAY_MAX = 30;
    private static final int TICKER_DELAY_INIT = 20;
    private static final int DEFAULT_LINES_LIMIT = 500;
    private static final JLabel eventTickerLabel = new JLabel("Event Ticker Delay:");
    private final JSlider eventTickerDelay =
            new JSlider(JSlider.HORIZONTAL, TICKER_DELAY_MIN, TICKER_DELAY_MAX, TICKER_DELAY_INIT);

    // Identification
    private static final JLabel userNameLabel = new JLabel("Nick:");
    private static final JTextField userNameTextField = new JTextField("", 12);
    private static final JLabel realNameLabel = new JLabel("Real name:");
    private static final JTextField realNameTextField = new JTextField("");

    // Authentication
    private static final JLabel authenticationTypeLabel = new JLabel("Authentication Type:");
    private static final UCAuthTypeComboBox authenticationTypeChoice = new UCAuthTypeComboBox();
    private static final JLabel passwordLabel = new JLabel("Password:");
    private static final JPasswordField passwordTextField = new JPasswordField("");
    private static final JLabel rememberPassLabel = new JLabel("Remember:");
    private static final JCheckBox rememberPassCheckBox = new JCheckBox();

    // Connection
    private static final JLabel serverNameLabel = new JLabel("Server:");
    private static final JTextField servernameTextField = new JTextField("", 8);
    private static final JLabel serverPortLabel = new JLabel("Port:");
    private static final JTextField serverPortTextField = new JTextField("", 4);
    private static final JLabel serverUseTLSLabel = new JLabel("TLS:");
    private static final JCheckBox serverTLSCheckBox = new JCheckBox();
    private static final JButton connectButton = new JButton("Connect");

    // Proxy
    private static final JLabel proxyHostLabel = new JLabel("Proxy Host:");
    private static final JTextField proxyHostNameTextField = new JTextField("");
    private static final JLabel proxyPortLabel = new JLabel("Port:", 4);
    private static final JTextField proxyPortTextField = new JTextField("");
    private static final JLabel serverUseProxyLabel = new JLabel("Use SOCKS:");
    private static final JCheckBox serverProxyCheckBox = new JCheckBox();

    private static final JLabel firstChannelLabel = new JLabel("Channel:");
    private static final JTextField firstChannelTextField = new JTextField("");

    // Favourites Panel
    private static final JCheckBox autoConnectToFavourites = new JCheckBox("Automatically join favourite channels");
    private static final DefaultListModel<FavouritesItem> favouritesListModel = new DefaultListModel<FavouritesItem>();
    private static final JList<FavouritesItem> favouritesList = new JList<FavouritesItem>(favouritesListModel);
    private static final JScrollPane favouritesScroller = new JScrollPane(favouritesList);

    // Created Servers/Tabs
    private final List<IRCServerBase> createdServers = new ArrayList<IRCServerBase>();

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#getLimitServerLinesCount()
     */
    @Override
    public int getLimitServerLinesCount ()
    {
        try
        {
            return Integer.parseInt(limitServerLinesCount.getText());
        } catch (Exception e)
        {
            // Was an error, default to 1000
            return DEFAULT_LINES_LIMIT;
        }
    }

    public void setLimitChannelLines (int limit)
    {
        limitChannelLinesCount.setText(Integer.toString(limit));
    }

    public void setLimitServerLines (int limit)
    {
        limitServerLinesCount.setText(Integer.toString(limit));
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#getLimitChannelLinesCount()
     */
    @Override
    public int getLimitChannelLinesCount ()
    {
        try
        {
            return Integer.parseInt(limitChannelLinesCount.getText());
        } catch (Exception e)
        {
            // Was an error, set to default
            return DEFAULT_LINES_LIMIT;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#setCurrentTab(int)
     */
    @Override
    public void setCurrentTab (int indexNum)
    {
        tabbedPane.setSelectedIndex(indexNum);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#setCurrentTab(java.lang.String)
     */
    @Override
    public void setCurrentTab (String tabName)
    {
        for (int x = 0; x < tabbedPane.getTabCount(); x++)
            if (tabbedPane.getTitleAt(x).toLowerCase().equals(tabName.toLowerCase()))
                tabbedPane.setSelectedIndex(x);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#getTabIndex(java.lang.String)
     */
    @Override
    public int getTabIndex (String tabName)
    {
        int currentTabCount = tabbedPane.getTabCount();

        for (int x = 0; x < currentTabCount; x++)
        {
            if (tabbedPane.getTitleAt(x).toLowerCase().equals(tabName.toLowerCase()))
                return x;
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#saveChannelHistory()
     */
    @Override
    public Boolean saveChannelHistory ()
    {
        return logChannelText.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#saveServerHistory()
     */
    @Override
    public Boolean saveServerHistory ()
    {
        return logServerActivity.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#getCreatedServer(java.lang.String)
     */
    @Override
    public IRCServerBase getCreatedServer (String serverName)
    {
        // for(int x = 0; x < createdChannels.size(); x++)
        for (IRCServerBase createdServer : createdServers)
        {
            if (createdServer.getName().equals(serverName.toLowerCase()))
            {
                return createdServer;
            }
        }
        return null;
    }

    public static void addProfileChangeListener (ActionListener actionListener)
    {
        profileListenerList.add(ActionListener.class, actionListener);
    }

    public static void fireProfileChangeListeners ()
    {
        Object[] listeners = profileListenerList.getListenerList();

        // Reverse order
        // for (int i = listeners.length - 2; i >= 0; i -= 2)
        // {
        //     if (listeners[i] == ActionListener.class)
        //     {
        //         if (this.actionEvent == null)
        //         {
        //             this.actionEvent = new ActionEvent(SAVE_BUTTON, i, TOOL_TIP_TEXT_KEY);
        //         }

        //         ((ActionListener) listeners[i + 1]).actionPerformed(this.actionEvent);
        //     }
        // }

        for (int i = 0; i <= listeners.length - 2; i += 2)
        {
            if (listeners[i] == ActionListener.class)
            {
                if (actionEvent == null)
                {
                    actionEvent = new ActionEvent(getProfilePicker(), i, TOOL_TIP_TEXT_KEY);
                }

                ((ActionListener) listeners[i + 1]).actionPerformed(actionEvent);
            }
        }
    }

    public static ProfilePicker getProfilePicker ()
    {
        return (((MainOptionsPanel) optionsMainPanel).getProfilePicker());
    }

    public static UROptionsPanel getProfilePanel ()
    {
        return profilePanel;
    }

    // /**
    //  * Sets the current active profile - if the newProfileName doesn't exist it will be created.
    //  * @param newProfileName
    //  */
    // public void setActiveProfile (String newProfileName)
    // {
    //     // save the current profile settings, if it exists
    //     if (URProfilesUtil.profileExists(URProfilesUtil.getActiveProfileName()))
    //     {
    //         setClientSettings();
    //     }

    //     if(!URProfilesUtil.profileExists(newProfileName))
    //     {
    //         URProfilesUtil.createProfile(newProfileName);
    //     }

    //     // change the profile name
    //     URProfilesUtil.setActiveProfileName(newProfileName);

    //     // now load the new profile settings
    //     getClientSettings(false);
    // }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#addToCreatedServers(java.lang.String)
     */
    @Override
    public void addToCreatedServers (String serverName)
    {
        if (getCreatedServer(serverName) == null)
        {
            createdServers.add(new IRCServer(serverName.trim(), userNameTextField.getText().trim(),
                    realNameTextField.getText().trim(), new String(passwordTextField.getPassword()),
                    serverPortTextField.getText().trim(), serverTLSCheckBox.isSelected(),
                    proxyHostNameTextField.getText(), proxyPortTextField.getText(), serverProxyCheckBox.isSelected()));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isCreatedServersEmpty()
     */
    @Override
    public Boolean isCreatedServersEmpty ()
    {
        return createdServers.isEmpty();
    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isShowingEventTicker()
     */
    @Override
    public Boolean isShowingEventTicker ()
    {
        return showEventTicker.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isShowingUsersList()
     */
    @Override
    public Boolean isShowingUsersList ()
    {
        return showUsersList.isSelected();
    }

    @Override
    public Boolean isClickableLinksEnabled ()
    {
        return enableClickableLinks.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isJoinsQuitsTickerEnabled()
     */
    @Override
    public Boolean isJoinsQuitsTickerEnabled ()
    {
        return showJoinsQuitsEventTicker.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isJoinsQuitsMainEnabled()
     */
    @Override
    public Boolean isJoinsQuitsMainEnabled ()
    {
        return showJoinsQuitsMainWindow.isSelected();
    }

    public void setJoinsQuitsMain (boolean enable)
    {
        showJoinsQuitsMainWindow.setSelected(enable);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isChannelHistoryEnabled()
     */
    @Override
    public Boolean isChannelHistoryEnabled ()
    {
        return logChannelText.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isLimitedServerActivity()
     */
    @Override
    public Boolean isLimitedServerActivity ()
    {
        return limitServerLines.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isLimitedChannelActivity()
     */
    @Override
    public Boolean isLimitedChannelActivity ()
    {
        return limitChannelLines.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isTimeStampsEnabled()
     */
    @Override
    public Boolean isTimeStampsEnabled ()
    {
        return enableTimeStamps.isSelected();
    }

    @Override
    public CapTypeBase authenticationType ()
    {
        return (CapTypeBase) authenticationTypeChoice.getSelectedItem();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isClientHistoryEnabled()
     */
    @Override
    public Boolean isClientHistoryEnabled ()
    {
        return logClientText.isSelected();

    }

    /**
     * Add the components to the Server Options Panel.
     */
    private void setupConnectionPanel ()
    {
        // connectionPanel.setLayout(new BoxLayout(connectionPanel, BoxLayout.PAGE_AXIS));
        setupConnectionLayout();

        // User stuff
        connectionPanel.add(userNameLabel);
        connectionPanel.add(userNameTextField);
        // userNameTextField.setPreferredSize(new Dimension(100, 24));
        // userNameTextField.setMinimumSize(new Dimension(100, 0));

        connectionPanel.add(realNameLabel);
        connectionPanel.add(realNameTextField);
        // realNameTextField.setMinimumSize(new Dimension(100, 0));

        connectionPanel.add(authenticationTypeLabel);
        connectionPanel.add(authenticationTypeChoice);
        authenticationTypeChoice.addActionListener(new UCAuthTypeComboBoxChangeHandler());
        // authenticationTypeChoice.setPreferredSize(new Dimension(200, 20));

        connectionPanel.add(passwordLabel);
        connectionPanel.add(passwordTextField);
        passwordTextField.setEchoChar('*');

        connectionPanel.add(rememberPassLabel);
        connectionPanel.add(rememberPassCheckBox);
        // passwordTextField.setPreferredSize(new Dimension(200, 20));

        // Server Stuff
        connectionPanel.add(serverNameLabel);
        connectionPanel.add(servernameTextField);
        // servernameTextField.setPreferredSize(new Dimension(100, 20));

        connectionPanel.add(serverPortLabel);
        connectionPanel.add(serverPortTextField);
        // serverPortTextField.setPreferredSize(new Dimension(50, 20));

        connectionPanel.add(serverUseTLSLabel);
        connectionPanel.add(serverTLSCheckBox);
        // serverTLSCheckBox.setPreferredSize(new Dimension(50, 20));

        // Proxy Stuff
        connectionPanel.add(proxyHostLabel);
        connectionPanel.add(proxyHostNameTextField);
        // proxyHostNameTextField.setPreferredSize(new Dimension(100, 20));

        connectionPanel.add(proxyPortLabel);
        connectionPanel.add(proxyPortTextField);
        // proxyPortTextField.setPreferredSize(new Dimension(50, 20));

        connectionPanel.add(serverUseProxyLabel);
        connectionPanel.add(serverProxyCheckBox);
        // serverProxyCheckBox.setPreferredSize(new Dimension(50, 20));

        // Channel Stuff
        connectionPanel.add(firstChannelLabel);
        connectionPanel.add(firstChannelTextField);
        // firstChannelTextField.setPreferredSize(new Dimension(100, 20));

        connectionPanel.add(connectButton);
        connectButton.addActionListener(new ConnectPressed());
        connectionPanel.add(autoConnectToFavourites);

        favouritesScroller.setPreferredSize(new Dimension(200, 100));
        favouritesList.addMouseListener(new FavouritesPopClickListener());
        connectionPanel.add(favouritesScroller);
    }

    /**
     * Aligns components on the Server Options Panel
     */
    private void setupConnectionLayout ()
    {
        SpringLayout connectionLayout = new SpringLayout();
        connectionPanel.setLayout(connectionLayout);

        // Used to make it more obvious what is going on -
        // and perhaps more readable.
        // 0 means THAT edge will be flush with the opposing components edge
        // Yes, negative numbers will make it overlap
        final int TOP_SPACING = 6;
        final int TOP_ALIGNED = 0;
        final int LEFT_ALIGNED = 0;
        final int RIGHT_ALIGNED = 0;
        final int LEFT_SPACING = 6;

        // Components are aligned off the top label
        // User stuff
        connectionLayout.putConstraint(SpringLayout.NORTH, userNameLabel, TOP_SPACING * 2, SpringLayout.NORTH,
                connectionPanel);
        connectionLayout.putConstraint(SpringLayout.WEST, userNameLabel, LEFT_SPACING * 2, SpringLayout.WEST,
                connectionPanel);

        connectionLayout.putConstraint(SpringLayout.NORTH, userNameTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                userNameLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, userNameTextField, LEFT_ALIGNED, SpringLayout.WEST,
                userNameLabel);

        connectionLayout.putConstraint(SpringLayout.NORTH, realNameLabel, TOP_SPACING, SpringLayout.SOUTH,
                userNameTextField);
        connectionLayout.putConstraint(SpringLayout.WEST, realNameLabel, LEFT_ALIGNED, SpringLayout.WEST,
                userNameTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, realNameTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                realNameLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, realNameTextField, LEFT_ALIGNED, SpringLayout.WEST,
                realNameLabel);
        connectionLayout.putConstraint(SpringLayout.EAST, realNameTextField, RIGHT_ALIGNED, SpringLayout.EAST,
                userNameTextField);

        // Authentication Stuff

        connectionLayout.putConstraint(SpringLayout.NORTH, authenticationTypeLabel, TOP_SPACING, SpringLayout.SOUTH,
                realNameTextField);
        connectionLayout.putConstraint(SpringLayout.WEST, authenticationTypeLabel, LEFT_ALIGNED, SpringLayout.WEST,
                realNameTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, authenticationTypeChoice, TOP_ALIGNED, SpringLayout.SOUTH,
                authenticationTypeLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, authenticationTypeChoice, LEFT_ALIGNED, SpringLayout.WEST,
                authenticationTypeLabel);
        connectionLayout.putConstraint(SpringLayout.EAST, authenticationTypeChoice, RIGHT_ALIGNED, SpringLayout.EAST,
                realNameTextField);

        // Password
        connectionLayout.putConstraint(SpringLayout.NORTH, passwordLabel, TOP_SPACING, SpringLayout.SOUTH,
                authenticationTypeChoice);
        connectionLayout.putConstraint(SpringLayout.WEST, passwordLabel, LEFT_ALIGNED, SpringLayout.WEST,
                authenticationTypeChoice);

        connectionLayout.putConstraint(SpringLayout.NORTH, passwordTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                passwordLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, passwordTextField, LEFT_ALIGNED, SpringLayout.WEST,
                passwordLabel);
        connectionLayout.putConstraint(SpringLayout.EAST, passwordTextField, RIGHT_ALIGNED, SpringLayout.EAST,
                authenticationTypeChoice);

        connectionLayout.putConstraint(SpringLayout.NORTH, rememberPassLabel, TOP_ALIGNED, SpringLayout.NORTH,
                passwordLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, rememberPassLabel, LEFT_ALIGNED, SpringLayout.EAST,
                passwordTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, rememberPassCheckBox, TOP_ALIGNED, SpringLayout.SOUTH,
                rememberPassLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, rememberPassCheckBox, LEFT_ALIGNED, SpringLayout.EAST,
                passwordTextField);


        // Server stuff
        connectionLayout.putConstraint(SpringLayout.NORTH, serverNameLabel, TOP_SPACING, SpringLayout.SOUTH,
                passwordTextField);
        connectionLayout.putConstraint(SpringLayout.WEST, serverNameLabel, LEFT_ALIGNED, SpringLayout.WEST,
                passwordTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, servernameTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                serverNameLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, servernameTextField, LEFT_ALIGNED, SpringLayout.WEST,
                serverNameLabel);

        connectionLayout.putConstraint(SpringLayout.NORTH, serverPortLabel, TOP_ALIGNED, SpringLayout.NORTH,
                serverNameLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, serverPortLabel, LEFT_ALIGNED, SpringLayout.EAST,
                servernameTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, serverPortTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                serverPortLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, serverPortTextField, LEFT_ALIGNED, SpringLayout.WEST,
                serverPortLabel);

        connectionLayout.putConstraint(SpringLayout.NORTH, serverUseTLSLabel, TOP_ALIGNED, SpringLayout.NORTH,
                serverPortLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, serverUseTLSLabel, LEFT_ALIGNED, SpringLayout.EAST,
                serverPortTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, serverTLSCheckBox, TOP_ALIGNED, SpringLayout.SOUTH,
                serverUseTLSLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, serverTLSCheckBox, LEFT_ALIGNED, SpringLayout.WEST,
                serverUseTLSLabel);

        // Proxy stuff
        connectionLayout.putConstraint(SpringLayout.NORTH, proxyHostLabel, TOP_SPACING, SpringLayout.SOUTH,
                servernameTextField);
        connectionLayout.putConstraint(SpringLayout.WEST, proxyHostLabel, LEFT_ALIGNED, SpringLayout.WEST,
                servernameTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, proxyHostNameTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                proxyHostLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, proxyHostNameTextField, LEFT_ALIGNED, SpringLayout.WEST,
                proxyHostLabel);
        connectionLayout.putConstraint(SpringLayout.EAST, proxyHostNameTextField, RIGHT_ALIGNED, SpringLayout.EAST,
                servernameTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, proxyPortLabel, TOP_ALIGNED, SpringLayout.NORTH,
                proxyHostLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, proxyPortLabel, LEFT_ALIGNED, SpringLayout.EAST,
                proxyHostNameTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, proxyPortTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                proxyPortLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, proxyPortTextField, LEFT_ALIGNED, SpringLayout.WEST,
                proxyPortLabel);
        connectionLayout.putConstraint(SpringLayout.EAST, proxyPortTextField, RIGHT_ALIGNED, SpringLayout.EAST,
                serverPortTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, serverUseProxyLabel, TOP_ALIGNED, SpringLayout.NORTH,
                proxyPortLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, serverUseProxyLabel, LEFT_ALIGNED, SpringLayout.EAST,
                proxyPortTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, serverProxyCheckBox, TOP_ALIGNED, SpringLayout.SOUTH,
                serverUseProxyLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, serverProxyCheckBox, LEFT_ALIGNED, SpringLayout.WEST,
                serverUseProxyLabel);

        // Channel Stuff
        connectionLayout.putConstraint(SpringLayout.NORTH, firstChannelLabel, TOP_SPACING, SpringLayout.SOUTH,
                proxyHostNameTextField);
        connectionLayout.putConstraint(SpringLayout.WEST, firstChannelLabel, LEFT_ALIGNED, SpringLayout.WEST,
                proxyHostNameTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, firstChannelTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                firstChannelLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, firstChannelTextField, LEFT_ALIGNED, SpringLayout.WEST,
                firstChannelLabel);
        connectionLayout.putConstraint(SpringLayout.EAST, firstChannelTextField, RIGHT_ALIGNED, SpringLayout.EAST,
                proxyHostNameTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, connectButton, TOP_SPACING * TOP_SPACING, SpringLayout.SOUTH,
                firstChannelTextField);
        connectionLayout.putConstraint(SpringLayout.WEST, connectButton, LEFT_ALIGNED, SpringLayout.WEST,
                firstChannelTextField);

        connectionLayout.putConstraint(SpringLayout.NORTH, autoConnectToFavourites, TOP_ALIGNED, SpringLayout.NORTH,
                userNameLabel);
        connectionLayout.putConstraint(SpringLayout.WEST, autoConnectToFavourites, LEFT_SPACING, SpringLayout.EAST,
                serverUseProxyLabel);

        connectionLayout.putConstraint(SpringLayout.NORTH, favouritesScroller, TOP_SPACING, SpringLayout.SOUTH,
                autoConnectToFavourites);
        connectionLayout.putConstraint(SpringLayout.WEST, favouritesScroller, LEFT_ALIGNED, SpringLayout.WEST,
                autoConnectToFavourites);
        connectionLayout.putConstraint(SpringLayout.EAST, favouritesScroller, LEFT_ALIGNED, SpringLayout.EAST,
                autoConnectToFavourites);
        connectionLayout.putConstraint(SpringLayout.SOUTH, favouritesScroller, TOP_SPACING, SpringLayout.SOUTH,
                connectButton);
    }

    private void setupAppearancePanel ()
    {
        Panels.addToPanel(appearancePanel, lafOptions, "Theme", Placement.DEFAULT, Size.MEDIUM);

        // Set a custom renderer to display the look and feel names
        lafOptions.setRenderer(new DefaultListCellRenderer()
        {
            @Override
            public Component getListCellRendererComponent (JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus)
            {
                LookAndFeelInfo info = (LookAndFeelInfo) value;
                return super.getListCellRendererComponent(list, info.getName(), index, isSelected, cellHasFocus);
            }
        });

        clientFontPanel = new FontPanel("", defaultStyle, URProfilesUtil.getActiveProfilePath());
        clientFontPanel.setPreferredSize(new Dimension(700, 64));

        addProfileChangeListener(e -> {
            clientFontPanel.setSettingsPath(URProfilesUtil.getActiveProfilePath());
        });

        // clientFontPanel.getSaveButton().addActionListener(new SaveFontListener());
        clientFontPanel.addSaveListener(new SaveFontListener());
        // clientFontPanel.getResetButton().addActionListener(new ResetFontListener());

        previewTextScroll.setPreferredSize(new Dimension(700, 150));
        previewTextArea.setEditable(false);

        timeStampField.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped (KeyEvent e)
            {
                // Not used
            }

            @Override
            public void keyPressed (KeyEvent e)
            {
                // Not used
            }

            @Override
            public void keyReleased (KeyEvent e)
            {
                updatePreviewTextArea();
            }
        });

        updatePreviewTextArea();

        Panels.addToPanel(appearancePanel, clientFontPanel, "Profile Font", Placement.DEFAULT, null);
        Panels.addToPanel(appearancePanel, timeStampField, "Timestamp Format", Placement.DEFAULT, Size.MEDIUM);

        Panels.addToPanel(appearancePanel, previewTextScroll, "Font Preview", Placement.DEFAULT, null);
        Panels.addToPanel(appearancePanel, styleLabel, "Preview Style", Placement.DEFAULT, null);
    }

    public void updatePreviewTextArea ()
    {
        StyledDocument previewDoc = previewTextArea.getStyledDocument();

        // previewTextArea.setFont(clientFontPanel.getFont());
        if (previewLineFormatter == null)
        {
            previewLineFormatter = new LineFormatter(clientFontPanel.getStyle(), previewTextArea, null, URProfilesUtil.getActiveProfilePath());

            addProfileChangeListener(e -> {
                previewLineFormatter.setSettingsPath(URProfilesUtil.getActiveProfilePath());
                previewLineFormatter.updateStyles(getStyle());
            });
        }

        if (previewDoc.getLength() <= 0)
        {
            previewTextArea.setCaretPosition(previewTextArea.getDocument().getLength());
            previewTextArea.addMouseListener(new PreviewClickListener());
            previewTextArea.addMouseMotionListener(new PreviewMovementListener());
            IRCUser tempUser = new IRCUser(null, "matty_r");
            IRCUser tempUser2 = new IRCUser(null, System.getProperty("user.name"));
            previewLineFormatter.setNick(System.getProperty("user.name"));
            previewLineFormatter.formattedDocument(new Date(), null, Constants.EVENT_USER,
                    "urChat has loaded - this is an Event");
            previewLineFormatter.formattedDocument(new Date(), tempUser, "matty_r", "Normal line. Hello, world!");
            previewLineFormatter.formattedDocument(new Date(), tempUser, "matty_r",
                    "This is what it looks like when your nick is mentioned, " + System.getProperty("user.name") + "!");
            previewLineFormatter.formattedDocument(new Date(), tempUser2, System.getProperty("user.name"),
                    "Go to https://github.com/matty-r/urChat");
            previewLineFormatter.formattedDocument(new Date(), tempUser2, System.getProperty("user.name"),
                    "Join #urchat on irc.libera.chat");
        } else
        {
            previewLineFormatter.updateStyles(getStyle());
        }
    }

    class PreviewClickListener extends MouseInputAdapter
    {
        public void mouseClicked (MouseEvent mouseEvent)
        {
            StyledDocument doc = previewTextArea.getStyledDocument();
            Element wordElement = doc.getCharacterElement(previewTextArea.viewToModel2D((mouseEvent.getPoint())));
            AttributeSet wordAttributeSet = wordElement.getAttributes();
            ClickableText isClickableText = (ClickableText) wordAttributeSet.getAttribute("clickableText");

            if (SwingUtilities.isRightMouseButton(mouseEvent) && wordAttributeSet.getAttribute("name") != null)
            {
                String styleName = styleLabel.getText();
                FontDialog styleFontDialog =
                        new FontDialog(styleName, previewLineFormatter.getStyleDefault(styleName), URProfilesUtil.getActiveProfilePath());

                styleFontDialog.addSaveListener(new SaveFontListener());
                styleFontDialog.setVisible(true);
            } else if (SwingUtilities.isLeftMouseButton(mouseEvent) && null != isClickableText)
            {
                isClickableText.execute();
            }
        }
    }

    class PreviewMovementListener extends MouseAdapter
    {
        public void mouseMoved (MouseEvent e)
        {
            StyledDocument doc = previewTextArea.getStyledDocument();
            Element wordElement = doc.getCharacterElement(previewTextArea.viewToModel2D((e.getPoint())));
            AttributeSet wordAttributeSet = wordElement.getAttributes();
            ClickableText isClickableText = (ClickableText) wordAttributeSet.getAttribute("clickableText");

            if (wordAttributeSet.getAttribute("name") != null)
                styleLabel.setText(wordAttributeSet.getAttribute("name").toString());
            else
                styleLabel.setText("Mouse over text to view style, right-click to edit.");

            if (isClickableText != null && isClickableLinksEnabled())
            {
                previewTextArea.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else
            {
                previewTextArea.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    public static String getTimeLineString (Date date)
    {
        SimpleDateFormat chatDateFormat = new SimpleDateFormat(timeStampField.getText());

        return chatDateFormat.format(date);
    }

    public static void setTimeLineString (String newFormat)
    {
        timeStampField.setText(newFormat);
    }

    private void setupInterfacePanel ()
    {
        interfacePanel.add(showEventTicker);
        interfacePanel.add(showUsersList);
        interfacePanel.add(enableClickableLinks);
        interfacePanel.add(showJoinsQuitsEventTicker);
        interfacePanel.add(showJoinsQuitsMainWindow);
        interfacePanel.add(logChannelText);
        interfacePanel.add(logServerActivity);
        interfacePanel.add(logClientText);
        interfacePanel.add(limitServerLines);
        interfacePanel.add(limitServerLinesCount);
        interfacePanel.add(limitChannelLines);
        interfacePanel.add(limitChannelLinesCount);
        interfacePanel.add(enableTimeStamps);

        // Turn on labels at major tick mark.
        eventTickerDelay.setMajorTickSpacing(10);
        eventTickerDelay.setMinorTickSpacing(1);
        eventTickerDelay.setPaintTicks(true);

        eventTickerDelay.setPaintLabels(true);
        // eventTickerDelay.setMaximumSize(new Dimension(400, 40));

        eventTickerDelay.setToolTipText("Event Ticker movement delay (Lower is faster)");

        interfacePanel.add(eventTickerLabel);
        interfacePanel.add(eventTickerDelay);

        setupInterfaceLayout();
    }

    /**
     * Aligns components on the Client Options Panel
     */
    private void setupInterfaceLayout ()
    {
        SpringLayout interfaceLayout = new SpringLayout();
        interfacePanel.setLayout(interfaceLayout);

        // Used to make it more obvious what is going on -
        // and perhaps more readable.
        // 0 means THAT edge will be flush with the opposing components edge
        // Yes, negative numbers will make it overlap
        final int TOP_SPACING = 6;
        final int TOP_ALIGNED = 0;
        final int LEFT_ALIGNED = 0;
        final int LEFT_SPACING = 6;

        // Components are aligned off the top label

        interfaceLayout.putConstraint(SpringLayout.WEST, showEventTicker, LEFT_SPACING * 2, SpringLayout.WEST,
                interfacePanel);
        interfaceLayout.putConstraint(SpringLayout.NORTH, showEventTicker, TOP_SPACING * 2, SpringLayout.NORTH,
                interfacePanel);

        interfaceLayout.putConstraint(SpringLayout.NORTH, showUsersList, TOP_SPACING, SpringLayout.SOUTH,
                showEventTicker);
        interfaceLayout.putConstraint(SpringLayout.WEST, showUsersList, LEFT_ALIGNED, SpringLayout.WEST,
                showEventTicker);

        interfaceLayout.putConstraint(SpringLayout.NORTH, enableClickableLinks, TOP_SPACING, SpringLayout.SOUTH,
                showUsersList);

        interfaceLayout.putConstraint(SpringLayout.WEST, enableClickableLinks, LEFT_ALIGNED, SpringLayout.WEST,
                showUsersList);

        interfaceLayout.putConstraint(SpringLayout.NORTH, showJoinsQuitsEventTicker, TOP_SPACING, SpringLayout.SOUTH,
                enableClickableLinks);
        interfaceLayout.putConstraint(SpringLayout.WEST, showJoinsQuitsEventTicker, LEFT_ALIGNED, SpringLayout.WEST,
                enableClickableLinks);

        interfaceLayout.putConstraint(SpringLayout.NORTH, showJoinsQuitsMainWindow, TOP_SPACING, SpringLayout.SOUTH,
                showJoinsQuitsEventTicker);
        interfaceLayout.putConstraint(SpringLayout.WEST, showJoinsQuitsMainWindow, LEFT_ALIGNED, SpringLayout.WEST,
                showJoinsQuitsEventTicker);

        interfaceLayout.putConstraint(SpringLayout.NORTH, logChannelText, TOP_SPACING, SpringLayout.SOUTH,
                showJoinsQuitsMainWindow);
        interfaceLayout.putConstraint(SpringLayout.WEST, logChannelText, LEFT_ALIGNED, SpringLayout.WEST,
                showJoinsQuitsMainWindow);

        interfaceLayout.putConstraint(SpringLayout.NORTH, logServerActivity, TOP_SPACING, SpringLayout.SOUTH,
                logChannelText);
        interfaceLayout.putConstraint(SpringLayout.WEST, logServerActivity, LEFT_ALIGNED, SpringLayout.WEST,
                logChannelText);

        interfaceLayout.putConstraint(SpringLayout.NORTH, logClientText, TOP_SPACING, SpringLayout.SOUTH,
                logServerActivity);
        interfaceLayout.putConstraint(SpringLayout.WEST, logClientText, LEFT_ALIGNED, SpringLayout.WEST,
                logServerActivity);

        interfaceLayout.putConstraint(SpringLayout.NORTH, limitServerLines, TOP_SPACING, SpringLayout.SOUTH,
                logClientText);
        interfaceLayout.putConstraint(SpringLayout.WEST, limitServerLines, LEFT_ALIGNED, SpringLayout.WEST,
                logClientText);

        interfaceLayout.putConstraint(SpringLayout.NORTH, limitServerLinesCount, TOP_ALIGNED, SpringLayout.NORTH,
                limitServerLines);
        interfaceLayout.putConstraint(SpringLayout.WEST, limitServerLinesCount, TOP_SPACING, SpringLayout.EAST,
                limitServerLines);

        interfaceLayout.putConstraint(SpringLayout.NORTH, limitChannelLines, TOP_SPACING, SpringLayout.SOUTH,
                limitServerLines);
        interfaceLayout.putConstraint(SpringLayout.WEST, limitChannelLines, LEFT_ALIGNED, SpringLayout.WEST,
                limitServerLines);

        interfaceLayout.putConstraint(SpringLayout.NORTH, limitChannelLinesCount, TOP_ALIGNED, SpringLayout.NORTH,
                limitChannelLines);
        interfaceLayout.putConstraint(SpringLayout.WEST, limitChannelLinesCount, LEFT_SPACING, SpringLayout.EAST,
                limitChannelLines);

        interfaceLayout.putConstraint(SpringLayout.NORTH, enableTimeStamps, TOP_SPACING, SpringLayout.SOUTH,
                limitChannelLines);
        interfaceLayout.putConstraint(SpringLayout.WEST, enableTimeStamps, LEFT_ALIGNED, SpringLayout.WEST,
                limitChannelLines);

        interfaceLayout.putConstraint(SpringLayout.NORTH, eventTickerLabel, TOP_SPACING, SpringLayout.SOUTH,
                enableTimeStamps);
        interfaceLayout.putConstraint(SpringLayout.WEST, eventTickerLabel, LEFT_ALIGNED, SpringLayout.WEST,
                enableTimeStamps);

        interfaceLayout.putConstraint(SpringLayout.NORTH, eventTickerDelay, TOP_SPACING, SpringLayout.SOUTH,
                eventTickerLabel);
        interfaceLayout.putConstraint(SpringLayout.WEST, eventTickerDelay, LEFT_ALIGNED, SpringLayout.WEST,
                eventTickerLabel);
    }

    /**
     * Create an element in the favourites list. Contains a constructor plus a pop up menu for the
     * element.
     *
     * @author Matt
     * @param String server
     * @param String channel
     */
    // TODO update javadoc
    class FavouritesItem
    {
        String favServer;
        String favChannel;
        Preferences settingsPath;
        FavouritesPopUp myMenu;
        FontDialog favFontDialog;

        public FavouritesItem (String favServer, String favChannel)
        {
            this.favServer = favServer;
            this.favChannel = favChannel;
            settingsPath = URProfilesUtil.getActiveFavouritesPath().node(favServer).node(favChannel);

            addProfileChangeListener(e -> {
                settingsPath = URProfilesUtil.getActiveFavouritesPath().node(favServer).node(favChannel);
            });

            createPopUp();
        }

        @Override
        public String toString ()
        {
            return favServer + ":" + favChannel;
        }

        public void createPopUp ()
        {
            myMenu = new FavouritesPopUp();
        }

        protected class SaveChannelFontListener implements ActionListener
        {
            @Override
            public void actionPerformed (ActionEvent arg0)
            {
                for (int index = 0; index < tabbedPane.getTabCount(); index++)
                {
                    Component tab = tabbedPane.getComponentAt(index);

                    if (tab instanceof IRCRoomBase)
                    {
                        IRCRoomBase tabRoom = (IRCRoomBase) tab;
                        if (tabRoom.getServer().getName().equals(favServer) && tabRoom.getName().equals(favChannel))
                        {
                            tabRoom.getFontPanel().setFont(favFontDialog.getFontPanel().getStyle(), true);
                            tabRoom.setFont(favFontDialog.getFontPanel().getFont());
                        }
                    }
                }
            }
        }

        private class FavouritesPopUp extends JPopupMenu
        {
            /**
             *
             */
            private static final long serialVersionUID = -3599612559330380653L;
            JMenuItem nameItem;
            JMenuItem removeItem;
            JMenuItem fontItem;

            public FavouritesPopUp ()
            {
                nameItem = new JMenuItem(FavouritesItem.this.toString());
                add(nameItem);
                this.addSeparator();
                //
                fontItem = new JMenuItem("Channel Font");
                fontItem.addActionListener(new ShowFontDialog());
                add(fontItem);
                // nameItem.setEnabled(false);
                removeItem = new JMenuItem("Delete");
                removeItem.addActionListener(new RemoveFavourite());
                add(removeItem);
            }

        }

        private class ShowFontDialog implements ActionListener
        {
            @Override
            public void actionPerformed (ActionEvent arg0)
            {
                if(favFontDialog == null)
                {
                    favFontDialog = new FontDialog(favChannel, UserGUI.this.getStyle(), settingsPath);
                    favFontDialog.addSaveListener(new SaveChannelFontListener());
                }

                if (favouritesList.getSelectedIndex() > -1)
                {
                    FavouritesItem tempItem = favouritesListModel.elementAt(favouritesList.getSelectedIndex());
                    tempItem.favFontDialog.getFontPanel().loadStyle();
                    tempItem.favFontDialog.setVisible(true);
                }
            }
        }

        private class RemoveFavourite implements ActionListener
        {
            @Override
            public void actionPerformed (ActionEvent arg0)
            {
                if (favouritesList.getSelectedIndex() > -1)
                {
                    FavouritesItem tempItem = favouritesListModel.elementAt(favouritesList.getSelectedIndex());
                    removeFavourite(tempItem.favServer, tempItem.favChannel);
                    Preferences channelNode = URProfilesUtil.getActiveFavouritesPath().node(tempItem.favServer).node(tempItem.favChannel);
                    try
                    {
                        String[] channelKeys = channelNode.keys();
                        if (channelKeys.length > 0)
                        {
                            int keyLength = channelKeys.length;

                            do
                            {
                                channelNode.remove(channelKeys[keyLength - 1]);
                                keyLength = channelNode.keys().length;
                            } while (keyLength > 0);
                        }
                    } catch (BackingStoreException e)
                    {
                        Constants.LOGGER.log(Level.WARNING, e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#addFavourite(java.lang.String, java.lang.String)
     */
    @Override
    public void addFavourite (String favServer, String favChannel)
    {
        favouritesListModel.addElement(new FavouritesItem(favServer, favChannel));

        URProfilesUtil.getActiveFavouritesPath().node(favServer).node(favChannel).put("PORT", getCreatedServer(favServer).getPort());
    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isFavourite(urChatBasic.frontend.IRCChannel)
     */
    @Override
    public Boolean isFavourite (IRCRoomBase channel)
    {
        FavouritesItem castItem;

        for (Object tempItem : favouritesListModel.toArray())
        {
            castItem = (FavouritesItem) tempItem;
            if (castItem.favChannel.equals(channel.getName()) && castItem.favServer.equals(channel.getServer()))
            {
                return true;
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#removeFavourite(java.lang.String, java.lang.String)
     */
    @Override
    public void removeFavourite (String favServer, String favChannel)
    {
        FavouritesItem castItem;

        for (Object tempItem : favouritesListModel.toArray())
        {
            castItem = (FavouritesItem) tempItem;
            if (castItem.favChannel.equals(favChannel) && castItem.favServer.equals(favServer))
            {
                favouritesListModel.removeElement(castItem);
                break;
            }
        }
    }


    class FavouritesPopClickListener extends MouseAdapter
    {
        public void mousePressed (MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                int row = favouritesList.locationToIndex(e.getPoint());
                if (row > -1)
                {
                    favouritesList.setSelectedIndex(row);
                    doPop(e);
                }
            }
        }

        public void mouseReleased (MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                int row = favouritesList.locationToIndex(e.getPoint());
                if (row > -1)
                {
                    favouritesList.setSelectedIndex(row);
                    doPop(e);
                }
            }
        }

        private void doPop (MouseEvent e)
        {
            favouritesList.getSelectedValue().myMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    /**
     * Used to initiate server connection
     *
     * @author Matt
     *
     */
    private class ConnectPressed implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            if (passwordTextField.getPassword().length > 0
                    || authenticationType().equals(CapabilityTypes.NONE.getType()))
            {
                addToCreatedServers(servernameTextField.getText().trim());

                if (autoConnectToFavourites.isSelected())
                {
                    FavouritesItem castItem;
                    for (Object tempItem : favouritesListModel.toArray())
                    {
                        castItem = (FavouritesItem) tempItem;
                        addToCreatedServers(castItem.favServer);
                    }
                }

                for (IRCServerBase server : createdServers)
                {
                    server.connect();
                }

                // profilePicker.setEnabled(false);
            } else if (!authenticationType().equals(CapabilityTypes.NONE.getType()))
            {
                MessageDialog dialog = new MessageDialog(
                        "Password field is empty and is required for your chosen authentication method.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                dialog.setVisible(true);
            }
        }
    }

    /**
     * (non-Javadoc)
     *
     * Connection was a success, setup the server tab
     *
     * @see urChatBasic.frontend.UserGUIBase#setupServerTab(urChatBasic.base.IRCServerBase)
     */
    public void setupServerTab (IRCServerBase server)
    {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run ()
            {
                if (server instanceof IRCServer)
                {
                    tabbedPane.addTab(server.getName(), ((IRCServer) server).icon, ((IRCServer) server));
                    setCurrentTab(server.getName());
                    // ((IRCServer) server).getUserTextBox().requestFocus();
                }
            }

        });
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#sendGlobalMessage(java.lang.String, java.lang.String)
     */
    @Override
    public void sendGlobalMessage (String message, String sender)
    {
        for (IRCServerBase tempServer : createdServers)
            tempServer.sendClientText(message, sender);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#connectFavourites(urChatBasic.base.IRCServerBase)
     */
    @Override
    public void connectFavourites (IRCServerBase server)
    {
        if (servernameTextField.getText().trim().equals(server.getName()))
            server.sendClientText("/join " + firstChannelTextField.getText().trim(),
                    servernameTextField.getText().trim());

        if (autoConnectToFavourites.isSelected())
        {
            FavouritesItem castItem;
            for (Object tempItem : favouritesListModel.toArray())
            {
                castItem = (FavouritesItem) tempItem;
                if (castItem.favServer.equals(server.getName()))
                    if (server.getCreatedChannel(castItem.favChannel) == null)
                        server.sendClientText("/join " + castItem.favChannel, castItem.favServer);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#shutdownAll()
     */
    @Override
    public void shutdownAll ()
    {
        if (!isCreatedServersEmpty())
        {
            quitServers();
            connectButton.setText("Connect");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#quitServers()
     */
    @Override
    public void quitServers ()
    {
        Iterator<IRCServerBase> serverIterator = createdServers.iterator();
        while (serverIterator.hasNext())
        {
            IRCServerBase tempServer = serverIterator.next();
            tempServer.disconnect();
            if (tempServer instanceof IRCServerBase)
            {
                tabbedPane.remove((IRCServer) tempServer);
            }
            createdServers.remove(tempServer);
        }

        // if(createdServers.size() == 0)
        //     profilePicker.setEnabled(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#quitServer(urChatBasic.base.IRCServerBase)
     */
    @Override
    public void quitServer (IRCServerBase server)
    {
        server.disconnect();
        tabbedPane.remove((IRCServer) server);
        createdServers.remove(server);

        // if(createdServers.size() == 0)
        //     profilePicker.setEnabled(true);
    }

    /**
     * Saves the settings into the registry/Settings API
     */
    public void setClientSettings ()
    {
        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_FIRST_CHANNEL, firstChannelTextField.getText());
        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_FIRST_SERVER, servernameTextField.getText());
        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_FIRST_PORT, serverPortTextField.getText());
        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_AUTH_TYPE, authenticationTypeChoice.getSelectedItem().toString());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_PASSWORD_REMEMBER, rememberPassCheckBox.isSelected());

        String rememberString = "";

        if (rememberPassCheckBox.isSelected())
        {
            rememberString = new String(passwordTextField.getPassword());
        }

        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_PASSWORD, rememberString);

        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_USE_TLS, serverTLSCheckBox.isSelected());
        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_PROXY_HOST, proxyHostNameTextField.getText());
        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_PROXY_PORT, proxyPortTextField.getText());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_USE_PROXY, serverProxyCheckBox.isSelected());
        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_NICK_NAME, userNameTextField.getText());
        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_REAL_NAME, realNameTextField.getText());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_TIME_STAMPS, enableTimeStamps.isSelected());
        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_TIME_STAMP_FORMAT, timeStampField.getText());
        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_LAF_NAME, ((LookAndFeelInfo) lafOptions.getSelectedItem()).getClassName());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_EVENT_TICKER_ACTIVE, showEventTicker.isSelected());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_USERS_LIST_ACTIVE, showUsersList.isSelected());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_CLICKABLE_LINKS_ENABLED, enableClickableLinks.isSelected());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_EVENT_TICKER_JOINS_QUITS, showJoinsQuitsEventTicker.isSelected());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_MAIN_WINDOW_JOINS_QUITS, showJoinsQuitsMainWindow.isSelected());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_LOG_CHANNEL_ACTIVITY, logChannelText.isSelected());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_LOG_SERVER_ACTIVITY, logServerActivity.isSelected());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_LIMIT_CHANNEL_LINES, limitChannelLines.isSelected());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_AUTO_CONNECT_FAVOURITES, autoConnectToFavourites.isSelected());
        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_LIMIT_CHANNEL_LINES_COUNT, limitChannelLinesCount.getText());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_LIMIT_SERVER_LINES, limitServerLines.isSelected());
        URProfilesUtil.getActiveProfilePath().put(Constants.KEY_LIMIT_SERVER_LINES_COUNT, limitServerLinesCount.getText());
        URProfilesUtil.getActiveProfilePath().putBoolean(Constants.KEY_LOG_CLIENT_TEXT, logClientText.isSelected());
        URPreferencesUtil.saveStyle(defaultStyle, clientFontPanel.getStyle(), URProfilesUtil.getActiveProfilePath());
        URProfilesUtil.getActiveProfilePath().putInt(Constants.KEY_EVENT_TICKER_DELAY, eventTickerDelay.getValue());

        URProfilesUtil.getActiveProfilePath().putInt(Constants.KEY_WINDOW_X, (int) DriverGUI.frame.getBounds().getX());
        URProfilesUtil.getActiveProfilePath().putInt(Constants.KEY_WINDOW_Y, (int) DriverGUI.frame.getBounds().getY());
        URProfilesUtil.getActiveProfilePath().putInt(Constants.KEY_WINDOW_WIDTH, (int) DriverGUI.frame.getBounds().getWidth());
        URProfilesUtil.getActiveProfilePath().putInt(Constants.KEY_WINDOW_HEIGHT, (int) DriverGUI.frame.getBounds().getHeight());
    }

    /**
     * Loads the settings from the registry/Settings API
     */
    public void getClientSettings (boolean loadWindowSettings)
    {
        firstChannelTextField
                .setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_FIRST_CHANNEL, Constants.DEFAULT_FIRST_CHANNEL));
        servernameTextField.setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_FIRST_SERVER, Constants.DEFAULT_FIRST_SERVER));
        serverPortTextField.setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_FIRST_PORT, Constants.DEFAULT_FIRST_PORT));
        serverTLSCheckBox.setSelected(URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_USE_TLS, Constants.DEFAULT_USE_TLS));

        authenticationTypeChoice.setSelectedItem(CapabilityTypes.getCapType(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_AUTH_TYPE, Constants.DEFAULT_AUTH_TYPE)));

        rememberPassCheckBox.setSelected(
                URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_PASSWORD_REMEMBER, Constants.DEFAULT_PASSWORD_REMEMBER));

        if (rememberPassCheckBox.isSelected())
        {
            passwordTextField.setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_PASSWORD, Constants.DEFAULT_PASSWORD));
        }

        proxyHostNameTextField.setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_PROXY_HOST, Constants.DEFAULT_PROXY_HOST));
        proxyPortTextField.setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_PROXY_PORT, Constants.DEFAULT_PROXY_PORT));
        serverProxyCheckBox
                .setSelected(URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_USE_PROXY, Constants.DEFAULT_USE_PROXY));

        userNameTextField.setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_NICK_NAME, Constants.DEFAULT_NICK_NAME));
        realNameTextField.setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_REAL_NAME, Constants.DEFAULT_REAL_NAME));

        showUsersList.setSelected(
                URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_USERS_LIST_ACTIVE, Constants.DEFAULT_USERS_LIST_ACTIVE));

        showEventTicker.setSelected(
                URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_EVENT_TICKER_ACTIVE, Constants.DEFAULT_EVENT_TICKER_ACTIVE));

        enableClickableLinks.setSelected(URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_CLICKABLE_LINKS_ENABLED,
                Constants.DEFAULT_CLICKABLE_LINKS_ENABLED));

        enableTimeStamps
                .setSelected(URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_TIME_STAMPS, Constants.DEFAULT_TIME_STAMPS));

        lafOptions.setSelectedItem(getLAF(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_LAF_NAME, Constants.DEFAULT_LAF_NAME)));

        // setNewLAF(((LookAndFeelInfo) lafOptions.getSelectedItem()).getClassName());

        showJoinsQuitsEventTicker.setSelected(URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_EVENT_TICKER_JOINS_QUITS,
                Constants.DEFAULT_EVENT_TICKER_JOINS_QUITS));
        showJoinsQuitsMainWindow.setSelected(URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_MAIN_WINDOW_JOINS_QUITS,
                Constants.DEFAULT_MAIN_WINDOW_JOINS_QUITS));
        logChannelText.setSelected(
                URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_LOG_CHANNEL_ACTIVITY, Constants.DEFAULT_LOG_CHANNEL_ACTIVITY));
        logServerActivity.setSelected(
                URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_LOG_SERVER_ACTIVITY, Constants.DEFAULT_LOG_SERVER_ACTIVITY));
        limitChannelLines.setSelected(
                URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_LIMIT_CHANNEL_LINES, Constants.DEFAULT_LIMIT_CHANNEL_LINES));
        limitChannelLinesCount.setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_LIMIT_CHANNEL_LINES_COUNT,
                Constants.DEFAULT_LIMIT_CHANNEL_LINES_COUNT));
        limitServerLines.setSelected(
                URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_LIMIT_SERVER_LINES, Constants.DEFAULT_LIMIT_SERVER_LINES));
        limitServerLinesCount.setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_LIMIT_SERVER_LINES_COUNT,
                Constants.DEFAULT_LIMIT_SERVER_LINES_COUNT));
        logClientText.setSelected(
                URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_LOG_CLIENT_TEXT, Constants.DEFAULT_LOG_CLIENT_TEXT));

        clientFontPanel.loadStyle();

        timeStampField
                .setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_TIME_STAMP_FORMAT, Constants.DEFAULT_TIME_STAMP_FORMAT));

        updatePreviewTextArea();

        eventTickerDelay.setValue(
                URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_EVENT_TICKER_DELAY, Constants.DEFAULT_EVENT_TICKER_DELAY));
        autoConnectToFavourites.setSelected(URProfilesUtil.getActiveProfilePath().getBoolean(Constants.KEY_AUTO_CONNECT_FAVOURITES,
                Constants.DEFAULT_AUTO_CONNECT_FAVOURITES));

        if (loadWindowSettings)
        {
            DriverGUI.frame.setBounds(URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_WINDOW_X, Constants.DEFAULT_WINDOW_X),
                    URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_WINDOW_Y, Constants.DEFAULT_WINDOW_Y),
                    URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_WIDTH),
                    URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_WINDOW_HEIGHT, Constants.DEFAULT_WINDOW_HEIGHT));

            this.setPreferredSize(
                    new Dimension(URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_WIDTH),
                            URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_WINDOW_HEIGHT, Constants.DEFAULT_WINDOW_HEIGHT)));
        }

        // TODO Add Port number to favourites.
        try
        {
            favouritesListModel.removeAllElements();
            for (String serverNode : URProfilesUtil.getActiveFavouritesPath().childrenNames())
            {
                for (String channelNode : URProfilesUtil.getActiveFavouritesPath().node(serverNode).childrenNames())
                {
                    if (URProfilesUtil.getActiveFavouritesPath().node(serverNode).node(channelNode).keys().length > 0)
                    {
                        favouritesListModel.addElement(new FavouritesItem(serverNode, channelNode));
                    }
                }
            }
        } catch (BackingStoreException e)
        {
            Constants.LOGGER.log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#removeClientSetting(java.lang.String, java.lang.String)
     */
    @Override
    public void removeClientSetting (String node, String key)
    {
        URProfilesUtil.getActiveProfilePath().node(node).remove(key);
    }

    @Override
    public void cleanUpSettings ()
    {
        // TODO: Clean up all nodes if they have empty keys
        Constants.LOGGER.log(Level.INFO, "Cleaning up settings");
        try
        {
            Constants.LOGGER.log(Level.INFO, "Remove empty favourites");
            for (String serverNode : URProfilesUtil.getActiveFavouritesPath().childrenNames())
            {
                for (String channelNode : URProfilesUtil.getActiveFavouritesPath().node(serverNode).childrenNames())
                {
                    if (URProfilesUtil.getActiveFavouritesPath().node(serverNode).node(channelNode).keys().length == 0)
                    {
                        URProfilesUtil.getActiveFavouritesPath().node(serverNode).node(channelNode).removeNode();
                    }
                }
            }

            Constants.LOGGER.log(Level.INFO, "Remove empty profiles");
            for (String profileNode : URProfilesUtil.getActiveProfilePath().childrenNames())
            {
                if (URProfilesUtil.getActiveProfilePath().node(profileNode) != URProfilesUtil.getActiveFavouritesPath()
                        && URProfilesUtil.getActiveProfilePath().node(profileNode).keys().length == 0)
                {
                    URProfilesUtil.getActiveProfilePath().node(profileNode).removeNode();
                }
            }
        } catch (BackingStoreException e)
        {
            Constants.LOGGER.log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#getEventTickerDelay()
     */
    @Override
    public int getEventTickerDelay ()
    {
        return eventTickerDelay.getValue();
    }

    class UCAuthTypeComboBoxChangeHandler implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent e)
        {
            authenticationTypeChoice.runChangeListener();

            if (authenticationType().equals(CapabilityTypes.NONE.getType()))
            {
                passwordLabel.setVisible(false);
                passwordTextField.setVisible(false);
                rememberPassCheckBox.setVisible(false);
                rememberPassLabel.setVisible(false);
            } else
            {
                passwordLabel.setText(authenticationTypeChoice.getPasswordFieldName());
                passwordLabel.setVisible(true);
                passwordTextField.setVisible(true);
                rememberPassCheckBox.setVisible(true);
                rememberPassLabel.setVisible(true);
            }
        }
    }

    class ChangeLAFListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent e)
        {
            setNewLAF(((LookAndFeelInfo) lafOptions.getSelectedItem()).getClassName());
        }
    }

    private void setupTabbedPane ()
    {
        tabbedPane.addChangeListener(new MainTabbedPanel_changeAdapter(this));
        tabbedPane.addMouseListener(new TabbedMouseListener());
        tabbedPane.addTab("Options", optionsMainPanel);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    /**
     * Used to listen to the right click on the tabs to determine what type we clicked on and pop up a
     * menu or exit. I will add menus to all types eventually.
     *
     * @author Matt
     *
     */
    private class TabbedMouseListener extends MouseInputAdapter
    {
        public void mouseClicked (MouseEvent e)
        {
            final int index = tabbedPane.getUI().tabForCoordinate(tabbedPane, e.getX(), e.getY());
            if (index > -1)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    Component selectedComponent = tabbedPane.getComponentAt(index);

                    if (selectedComponent instanceof IRCPrivate)
                    {
                        IRCServerBase tempServer =
                                getCreatedServer(((IRCRoomBase) selectedComponent).getServer().getName());
                        tempServer.quitRoom((IRCRoomBase) selectedComponent);
                    } else
                    {
                        ((IRCRoomBase) selectedComponent).myMenu.show(tabbedPane, e.getX(), e.getY());
                    }
                }
            }
        }
    }

    /**
     * Sets focus to the appropriate textBox when tab is changed
     *
     * @param e
     */
    private void TabbedPanel_stateChanged (ChangeEvent e)
    {
        int index = tabbedPane.getSelectedIndex();
        if (index > -1)
        {
            Component selectedComponent = tabbedPane.getComponentAt(index);
            if (selectedComponent instanceof IRCRoomBase)
            {
                IRCRoomBase tempTab = (IRCRoomBase) selectedComponent;
                if (!(selectedComponent instanceof IRCServer))
                {
                    tempTab.toggleEventTicker(isShowingEventTicker());
                    tempTab.toggleUsersList(isShowingUsersList());
                }

                tempTab.getUserTextBox().requestFocus();
                tempTab.enableFocus();
            }
            currentSelectedTab = selectedComponent;
        }
    }

    class MainTabbedPanel_changeAdapter implements javax.swing.event.ChangeListener
    {
        UserGUI adaptee;

        MainTabbedPanel_changeAdapter (UserGUI adaptee)
        {
            this.adaptee = adaptee;
        }

        public void stateChanged (ChangeEvent e)
        {
            previousSelectedTab = currentSelectedTab;
            adaptee.TabbedPanel_stateChanged(e);
        }
    }

    protected class SaveFontListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            clientFontPanel.loadStyle();

            for (int index = 0; index < tabbedPane.getTabCount(); index++)
            {
                Component tab = tabbedPane.getComponentAt(index);

                if (tab instanceof IRCRoomBase)
                {
                    tab.setFont(getStyle().getFont());
                    ((IRCRoomBase) tab).getFontPanel().setDefaultStyle(getStyle());
                }
            }

            for (int index = 0; index < favouritesList.getModel().getSize(); index++)
            {
                FavouritesItem favouriteItem = favouritesList.getModel().getElementAt(index);
                if(favouriteItem.favFontDialog != null)
                {
                    favouriteItem.favFontDialog.getFontPanel().setDefaultStyle(getStyle());
                    favouriteItem.favFontDialog.getFontPanel().loadStyle();
                }
            }

            // defaultStyle = clientFontPanel.getStyle();
            previewLineFormatter.updateStyles(getStyle());
        }
    }

    public UserGUI (Optional<String> initialProfile)
    {
        if(initialProfile.isPresent())
            URProfilesUtil.setActiveProfileName(initialProfile.get());
        else
            URProfilesUtil.setActiveProfileName(URProfilesUtil.getDefaultProfile());
    }

    public void setupUserGUI ()
    {
        // Create the initial size of the panel
        setupTabbedPane();
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        setupConnectionPanel();
        setupInterfacePanel();
        setupAppearancePanel();

        // this.setBackground(Color.gray);
        getClientSettings(true);

        URProfilesUtil.addListener(EventType.CHANGE, e-> {
            getClientSettings(false);
        });
        lafOptions.addActionListener(new ChangeLAFListener());
    }

    // Disables focus in the text pane
    public void lostFocus ()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run ()
            {
                int index = tabbedPane.getSelectedIndex();
                Component selectedComponent = tabbedPane.getComponentAt(index);
                if (selectedComponent instanceof IRCRoomBase)
                {
                    IRCRoomBase tempTab = (IRCRoomBase) selectedComponent;
                    tempTab.disableFocus();
                }
            }
        });
    }

    // Sets focus to the clientTextBox, then reenables focus in the text pane
    public void regainedFocus ()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run ()
            {
                TabbedPanel_stateChanged(null);
            }
        });
    }

    /**
     * Returns the clientFontPanel style, otherwise creates the new default style.
     *
     * @return
     */
    public URStyle getStyle ()
    {
        if (clientFontPanel != null)
        {
            return clientFontPanel.getStyle();
        }

        return defaultStyle;
    }

    private LookAndFeelInfo getLAF (String lafClassName)
    {
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
        {
            if (lafClassName.equals(info.getClassName()))
            {
                return info;
            }
        }

        Constants.LOGGER.log(Level.SEVERE, "Unable to set LAF to " + lafClassName);

        // Set to the System LAF if we've chosen an invalid/unavailable LAF theme
        return getLAF(UIManager.getSystemLookAndFeelClassName());
    }

    public void setNewLAF (String newLAFname)
    {
        // String previousDefaultForeground = URColour.hexEncode(UIManager.getColor(Constants.DEFAULT_FOREGROUND_STRING));
        // String previousDefaultBackground = URColour.hexEncode(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING));
        // Font previousDefaultFont = getFont();

        Constants.LOGGER.log(Level.INFO, "Setting to LookAndFeel to " + newLAFname);
        boolean flatLafAvailable = false;
        try
        {

            try
            {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                {
                    // System.out.println(info.getName());
                    if (newLAFname.equals(info.getClassName()))
                    {
                        UIManager.setLookAndFeel(info.getClassName());
                        flatLafAvailable = true;
                        break;
                    }
                }
            } catch (Exception e)
            {
                throw e;
            }
        } catch (Exception e)
        {
            Constants.LOGGER.log(Level.WARNING, "Failed to set Pluggable LAF! " + e.getLocalizedMessage());
        } finally
        {
            if (!flatLafAvailable)
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e)
                {
                    Constants.LOGGER.log(Level.WARNING, "Failed to setLookAndFeel! " + e.getLocalizedMessage());
                }
            }
        }

        // Required because it doesn't pickup the default ui
        tabbedPane.setUI((new JTabbedPane()).getUI());

        defaultStyle.setFont(UIManager.getFont(Constants.DEFAULT_FONT_STRING));

        // reset the defaults on the guiStyle if they were already at the default
        // if (previousDefaultForeground.equals(URColour.hexEncode(guiStyle.getForeground())))
            defaultStyle.setForeground(UIManager.getColor(Constants.DEFAULT_FOREGROUND_STRING));

        // if (previousDefaultBackground.equals(URColour.hexEncode(guiStyle.getBackground())))
            defaultStyle.setBackground(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING));

        clientFontPanel.setDefaultStyle(defaultStyle);

        SwingUtilities.updateComponentTreeUI(DriverGUI.frame);
        updateExtras();
        // DriverGUI.frame.dispose();
        DriverGUI.frame.validate();
    }

    // Update the fonts and popup menus - these aren't under the component tree
    private void updateExtras ()
    {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run ()
            {
                for (int index = 0; index < tabbedPane.getTabCount(); index++)
                {
                    Component tab = tabbedPane.getComponentAt(index);

                    if (tab instanceof IRCRoomBase)
                    {
                        // tab.setFont(clientFontPanel.getFont());
                        IRCRoomBase roomTab = IRCRoomBase.class.cast(tab);
                        // roomTab.getFontPanel().setDefaultFont(clientFontPanel.getFont());
                        roomTab.getFontPanel().setDefaultStyle(defaultStyle);
                        // roomTab.resetLineFormatter();
                        roomTab.getLineFormatter().updateStyles(getStyle());
                        SwingUtilities.updateComponentTreeUI(roomTab.myMenu);
                        SwingUtilities.updateComponentTreeUI(roomTab.getFontPanel());
                    }
                }

                for (int index = 0; index < favouritesList.getModel().getSize(); index++)
                {
                    FavouritesItem favouriteItem = favouritesList.getModel().getElementAt(index);
                    SwingUtilities.updateComponentTreeUI(favouriteItem.myMenu);
                }

                // update the styles in the preview text area
                updatePreviewTextArea();
            }

        });
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#run()
     */
    @Override
    public void run ()
    {
        setupUserGUI();

        Thread.currentThread().setContextClassLoader(DriverGUI.contextClassLoader);
        Thread.currentThread().setUncaughtExceptionHandler(new URUncaughtExceptionHandler());

        setNewLAF(((LookAndFeelInfo) lafOptions.getSelectedItem()).getClassName());
    }
}
