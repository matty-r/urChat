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
import urChatBasic.base.IRCRoomBase;
import urChatBasic.base.IRCServerBase;
import urChatBasic.frontend.dialogs.FontDialog;
import urChatBasic.frontend.dialogs.MessageDialog;
import urChatBasic.base.UserGUIBase;
import urChatBasic.base.capabilities.CapTypeBase;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.frontend.components.FontPanel;
import urChatBasic.frontend.components.UCAuthTypeComboBox;

public class UserGUI extends JPanel implements Runnable, UserGUIBase
{
    /**
     *
     */
    private static final long serialVersionUID = 2595649865577419300L;
    // private String creationTime = (new Date()).toString();
    // Tabs
    public DnDTabbedPane tabbedPane = new DnDTabbedPane();
    private final int OPTIONS_INDEX = 0;

    // Options Panel
    private JPanel optionsMainPanel = new JPanel();
    private JPanel optionsLeftPanel = new JPanel();
    private DefaultListModel<String> optionsArray = new DefaultListModel<String>();
    private JList<String> optionsList = new JList<String>(optionsArray);
    private JPanel optionsRightPanel = new JPanel();

    // Client Options Panel
    private static final JPanel optionsClientPanel = new JPanel();
    private static final JScrollPane clientScroller = new JScrollPane(optionsClientPanel);
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
    private FontPanel clientFontPanel;

    private static final JTextField limitServerLinesCount = new JTextField();
    private static final JTextField limitChannelLinesCount = new JTextField();

    private static final int TICKER_DELAY_MIN = 0;
    private static final int TICKER_DELAY_MAX = 30;
    private static final int TICKER_DELAY_INIT = 20;
    private static final int DEFAULT_LINES_LIMIT = 500;
    private static final JLabel eventTickerLabel = new JLabel("Event Ticker Delay:");
    private final JSlider eventTickerDelay =
            new JSlider(JSlider.HORIZONTAL, TICKER_DELAY_MIN, TICKER_DELAY_MAX, TICKER_DELAY_INIT);

    private final JButton saveSettings = new JButton("Save");

    // Server Options Panel
    private static final JPanel serverOptionsPanel = new JPanel();
    private static final JScrollPane serverScroller = new JScrollPane(serverOptionsPanel);

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
    private static final JCheckBox autoConnectToFavourites = new JCheckBox("Automatically connect to favourites");
    private static final DefaultListModel<FavouritesItem> favouritesListModel = new DefaultListModel<FavouritesItem>();
    private static final JList<FavouritesItem> favouritesList = new JList<FavouritesItem>(favouritesListModel);
    private static final JScrollPane favouritesScroller = new JScrollPane(favouritesList);

    // public static Font universalFont = new Font("Consolas", Font.PLAIN, 12); // different font area
    // for the interface?

    // Created Servers/Tabs
    private final List<IRCServerBase> createdServers = new ArrayList<IRCServerBase>();

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#getLimitServerLinesCount()
     */
    @Override
    public int getLimitServerLinesCount()
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

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#getLimitChannelLinesCount()
     */
    @Override
    public int getLimitChannelLinesCount()
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
    public void setCurrentTab(int indexNum)
    {
        tabbedPane.setSelectedIndex(indexNum);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#setCurrentTab(java.lang.String)
     */
    @Override
    public void setCurrentTab(String tabName)
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
    public int getTabIndex(String tabName)
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
    public Boolean saveChannelHistory()
    {
        return logChannelText.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#saveServerHistory()
     */
    @Override
    public Boolean saveServerHistory()
    {
        return logServerActivity.isSelected();
    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#getCreatedServer(java.lang.String)
     */
    @Override
    public IRCServerBase getCreatedServer(String serverName)
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


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#addToCreatedServers(java.lang.String)
     */
    @Override
    public void addToCreatedServers(String serverName)
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
    public Boolean isCreatedServersEmpty()
    {
        return createdServers.isEmpty();
    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isShowingEventTicker()
     */
    @Override
    public Boolean isShowingEventTicker()
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
    public Boolean isJoinsQuitsTickerEnabled()
    {
        return showJoinsQuitsEventTicker.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isJoinsQuitsMainEnabled()
     */
    @Override
    public Boolean isJoinsQuitsMainEnabled()
    {
        return showJoinsQuitsMainWindow.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isChannelHistoryEnabled()
     */
    @Override
    public Boolean isChannelHistoryEnabled()
    {
        return logChannelText.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isLimitedServerActivity()
     */
    @Override
    public Boolean isLimitedServerActivity()
    {
        return limitServerLines.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isLimitedChannelActivity()
     */
    @Override
    public Boolean isLimitedChannelActivity()
    {
        return limitChannelLines.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isTimeStampsEnabled()
     */
    @Override
    public Boolean isTimeStampsEnabled()
    {
        return enableTimeStamps.isSelected();
    }

    @Override
    public CapTypeBase authenticationType()
    {
        return (CapTypeBase) authenticationTypeChoice.getSelectedItem();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isClientHistoryEnabled()
     */
    @Override
    public Boolean isClientHistoryEnabled()
    {
        return logClientText.isSelected();

    }

    /*
     * public Boolean isLinksClickable(){ return enableClickableLinks.isSelected(); }
     */

    private void setupOptionsPanel()
    {
        optionsMainPanel.setLayout(new BorderLayout());

        optionsArray.addElement("Server");
        optionsArray.addElement("Client");

        setupLeftOptionsPanel();
        setupRightOptionsPanel();

        optionsMainPanel.add(optionsLeftPanel, BorderLayout.LINE_START);
        optionsMainPanel.add(optionsRightPanel, BorderLayout.CENTER);
        optionsList.setSelectedIndex(OPTIONS_INDEX);

        optionsClientPanel.setPreferredSize(new Dimension(500, 0));
        serverOptionsPanel.setPreferredSize(new Dimension(200, 0));

        optionsRightPanel.add(serverScroller, "Server");
        optionsRightPanel.add(clientScroller, "Client");
    }

    /**
     * Houses the options list
     */
    private void setupLeftOptionsPanel()
    {
        optionsLeftPanel.setBackground(optionsList.getBackground());
        optionsLeftPanel.setPreferredSize(new Dimension(100, 0));
        optionsLeftPanel.setLayout(new BorderLayout());
        // optionsLeftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        optionsLeftPanel.add(saveSettings, BorderLayout.SOUTH);
        optionsLeftPanel.add(optionsList, BorderLayout.CENTER);
    }

    private void setupRightOptionsPanel()
    {
        ListSelectionModel listSelectionModel = optionsList.getSelectionModel();
        listSelectionModel.addListSelectionListener(new OptionsListSelectionHandler());

        // optionsRightPanel.setBackground(Color.BLACK);
        optionsRightPanel.setLayout(new CardLayout());

        setupServerOptionsPanelComponents();
        setupClientOptionsPanelComponents();
        setupFavouritesOptionsPanel();
    }


    /**
     * Add the components to the Server Options Panel.
     */
    private void setupServerOptionsPanelComponents()
    {
        // serverOptionsPanel.setLayout(new BoxLayout(serverOptionsPanel, BoxLayout.PAGE_AXIS));
        setupServerOptionsLayout();

        // User stuff
        serverOptionsPanel.add(userNameLabel);
        serverOptionsPanel.add(userNameTextField);
        // userNameTextField.setPreferredSize(new Dimension(100, 24));
        // userNameTextField.setMinimumSize(new Dimension(100, 0));

        serverOptionsPanel.add(realNameLabel);
        serverOptionsPanel.add(realNameTextField);
        // realNameTextField.setMinimumSize(new Dimension(100, 0));

        serverOptionsPanel.add(authenticationTypeLabel);
        serverOptionsPanel.add(authenticationTypeChoice);
        authenticationTypeChoice.addActionListener(new UCAuthTypeComboBoxChangeHandler());
        // authenticationTypeChoice.setPreferredSize(new Dimension(200, 20));

        serverOptionsPanel.add(passwordLabel);
        serverOptionsPanel.add(passwordTextField);
        passwordTextField.setEchoChar('*');
        // passwordTextField.setPreferredSize(new Dimension(200, 20));

        // Server Stuff
        serverOptionsPanel.add(serverNameLabel);
        serverOptionsPanel.add(servernameTextField);
        // servernameTextField.setPreferredSize(new Dimension(100, 20));

        serverOptionsPanel.add(serverPortLabel);
        serverOptionsPanel.add(serverPortTextField);
        // serverPortTextField.setPreferredSize(new Dimension(50, 20));

        serverOptionsPanel.add(serverUseTLSLabel);
        serverOptionsPanel.add(serverTLSCheckBox);
        // serverTLSCheckBox.setPreferredSize(new Dimension(50, 20));

        // Proxy Stuff
        serverOptionsPanel.add(proxyHostLabel);
        serverOptionsPanel.add(proxyHostNameTextField);
        // proxyHostNameTextField.setPreferredSize(new Dimension(100, 20));

        serverOptionsPanel.add(proxyPortLabel);
        serverOptionsPanel.add(proxyPortTextField);
        // proxyPortTextField.setPreferredSize(new Dimension(50, 20));

        serverOptionsPanel.add(serverUseProxyLabel);
        serverOptionsPanel.add(serverProxyCheckBox);
        // serverProxyCheckBox.setPreferredSize(new Dimension(50, 20));

        // Channel Stuff
        serverOptionsPanel.add(firstChannelLabel);
        serverOptionsPanel.add(firstChannelTextField);
        // firstChannelTextField.setPreferredSize(new Dimension(100, 20));

        serverOptionsPanel.add(connectButton);
        connectButton.addActionListener(new ConnectPressed());
        serverOptionsPanel.add(autoConnectToFavourites);

        favouritesScroller.setPreferredSize(new Dimension(200, 100));
        favouritesList.addMouseListener(new FavouritesPopClickListener());
        serverOptionsPanel.add(favouritesScroller);
    }

    /**
     * Aligns components on the Server Options Panel
     */
    private void setupServerOptionsLayout()
    {
        SpringLayout serverLayout = new SpringLayout();
        serverOptionsPanel.setLayout(serverLayout);

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
        serverLayout.putConstraint(SpringLayout.NORTH, userNameLabel, TOP_SPACING * 2, SpringLayout.NORTH,
                serverOptionsPanel);
        serverLayout.putConstraint(SpringLayout.WEST, userNameLabel, LEFT_SPACING * 2, SpringLayout.WEST,
                serverOptionsPanel);

        serverLayout.putConstraint(SpringLayout.NORTH, userNameTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                userNameLabel);
        serverLayout.putConstraint(SpringLayout.WEST, userNameTextField, LEFT_ALIGNED, SpringLayout.WEST,
                userNameLabel);

        serverLayout.putConstraint(SpringLayout.NORTH, realNameLabel, TOP_SPACING, SpringLayout.SOUTH,
                userNameTextField);
        serverLayout.putConstraint(SpringLayout.WEST, realNameLabel, LEFT_ALIGNED, SpringLayout.WEST,
                userNameTextField);

        serverLayout.putConstraint(SpringLayout.NORTH, realNameTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                realNameLabel);
        serverLayout.putConstraint(SpringLayout.WEST, realNameTextField, LEFT_ALIGNED, SpringLayout.WEST,
                realNameLabel);
        serverLayout.putConstraint(SpringLayout.EAST, realNameTextField, RIGHT_ALIGNED, SpringLayout.EAST,
                userNameTextField);

        // Authentication Stuff

        serverLayout.putConstraint(SpringLayout.NORTH, authenticationTypeLabel, TOP_SPACING, SpringLayout.SOUTH,
                realNameTextField);
        serverLayout.putConstraint(SpringLayout.WEST, authenticationTypeLabel, LEFT_ALIGNED, SpringLayout.WEST,
                realNameTextField);

        serverLayout.putConstraint(SpringLayout.NORTH, authenticationTypeChoice, TOP_ALIGNED, SpringLayout.SOUTH,
                authenticationTypeLabel);
        serverLayout.putConstraint(SpringLayout.WEST, authenticationTypeChoice, LEFT_ALIGNED, SpringLayout.WEST,
                authenticationTypeLabel);
        serverLayout.putConstraint(SpringLayout.EAST, authenticationTypeChoice, RIGHT_ALIGNED, SpringLayout.EAST,
                realNameTextField);

        // Password
        serverLayout.putConstraint(SpringLayout.NORTH, passwordLabel, TOP_SPACING, SpringLayout.SOUTH,
                authenticationTypeChoice);
        serverLayout.putConstraint(SpringLayout.WEST, passwordLabel, LEFT_ALIGNED, SpringLayout.WEST,
                authenticationTypeChoice);

        serverLayout.putConstraint(SpringLayout.NORTH, passwordTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                passwordLabel);
        serverLayout.putConstraint(SpringLayout.WEST, passwordTextField, LEFT_ALIGNED, SpringLayout.WEST,
                passwordLabel);
        serverLayout.putConstraint(SpringLayout.EAST, passwordTextField, RIGHT_ALIGNED, SpringLayout.EAST,
                authenticationTypeChoice);

        // Server stuff
        serverLayout.putConstraint(SpringLayout.NORTH, serverNameLabel, TOP_SPACING, SpringLayout.SOUTH,
                passwordTextField);
        serverLayout.putConstraint(SpringLayout.WEST, serverNameLabel, LEFT_ALIGNED, SpringLayout.WEST,
                passwordTextField);

        serverLayout.putConstraint(SpringLayout.NORTH, servernameTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                serverNameLabel);
        serverLayout.putConstraint(SpringLayout.WEST, servernameTextField, LEFT_ALIGNED, SpringLayout.WEST,
                serverNameLabel);

        serverLayout.putConstraint(SpringLayout.NORTH, serverPortLabel, TOP_ALIGNED, SpringLayout.NORTH,
                serverNameLabel);
        serverLayout.putConstraint(SpringLayout.WEST, serverPortLabel, LEFT_ALIGNED, SpringLayout.EAST,
                servernameTextField);

        serverLayout.putConstraint(SpringLayout.NORTH, serverPortTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                serverPortLabel);
        serverLayout.putConstraint(SpringLayout.WEST, serverPortTextField, LEFT_ALIGNED, SpringLayout.WEST,
                serverPortLabel);

        serverLayout.putConstraint(SpringLayout.NORTH, serverUseTLSLabel, TOP_ALIGNED, SpringLayout.NORTH,
                serverPortLabel);
        serverLayout.putConstraint(SpringLayout.WEST, serverUseTLSLabel, LEFT_ALIGNED, SpringLayout.EAST,
                serverPortTextField);

        serverLayout.putConstraint(SpringLayout.NORTH, serverTLSCheckBox, TOP_ALIGNED, SpringLayout.SOUTH,
                serverUseTLSLabel);
        serverLayout.putConstraint(SpringLayout.WEST, serverTLSCheckBox, LEFT_ALIGNED, SpringLayout.WEST,
                serverUseTLSLabel);

        // Proxy stuff
        serverLayout.putConstraint(SpringLayout.NORTH, proxyHostLabel, TOP_SPACING, SpringLayout.SOUTH,
                servernameTextField);
        serverLayout.putConstraint(SpringLayout.WEST, proxyHostLabel, LEFT_ALIGNED, SpringLayout.WEST,
                servernameTextField);

        serverLayout.putConstraint(SpringLayout.NORTH, proxyHostNameTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                proxyHostLabel);
        serverLayout.putConstraint(SpringLayout.WEST, proxyHostNameTextField, LEFT_ALIGNED, SpringLayout.WEST,
                proxyHostLabel);
        serverLayout.putConstraint(SpringLayout.EAST, proxyHostNameTextField, RIGHT_ALIGNED, SpringLayout.EAST,
                servernameTextField);

        serverLayout.putConstraint(SpringLayout.NORTH, proxyPortLabel, TOP_ALIGNED, SpringLayout.NORTH, proxyHostLabel);
        serverLayout.putConstraint(SpringLayout.WEST, proxyPortLabel, LEFT_ALIGNED, SpringLayout.EAST,
                proxyHostNameTextField);

        serverLayout.putConstraint(SpringLayout.NORTH, proxyPortTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                proxyPortLabel);
        serverLayout.putConstraint(SpringLayout.WEST, proxyPortTextField, LEFT_ALIGNED, SpringLayout.WEST,
                proxyPortLabel);

        serverLayout.putConstraint(SpringLayout.NORTH, serverUseProxyLabel, TOP_ALIGNED, SpringLayout.NORTH,
                proxyPortLabel);
        serverLayout.putConstraint(SpringLayout.WEST, serverUseProxyLabel, LEFT_ALIGNED, SpringLayout.EAST,
                proxyPortTextField);

        serverLayout.putConstraint(SpringLayout.NORTH, serverProxyCheckBox, TOP_ALIGNED, SpringLayout.SOUTH,
                serverUseProxyLabel);
        serverLayout.putConstraint(SpringLayout.WEST, serverProxyCheckBox, LEFT_ALIGNED, SpringLayout.WEST,
                serverUseProxyLabel);

        // Channel Stuff
        serverLayout.putConstraint(SpringLayout.NORTH, firstChannelLabel, TOP_SPACING, SpringLayout.SOUTH,
                proxyHostNameTextField);
        serverLayout.putConstraint(SpringLayout.WEST, firstChannelLabel, LEFT_ALIGNED, SpringLayout.WEST,
                proxyHostNameTextField);

        serverLayout.putConstraint(SpringLayout.NORTH, firstChannelTextField, TOP_ALIGNED, SpringLayout.SOUTH,
                firstChannelLabel);
        serverLayout.putConstraint(SpringLayout.WEST, firstChannelTextField, LEFT_ALIGNED, SpringLayout.WEST,
                firstChannelLabel);
        serverLayout.putConstraint(SpringLayout.EAST, firstChannelTextField, RIGHT_ALIGNED, SpringLayout.EAST,
                proxyHostNameTextField);

        serverLayout.putConstraint(SpringLayout.NORTH, connectButton, TOP_SPACING * TOP_SPACING, SpringLayout.SOUTH,
                firstChannelTextField);
        serverLayout.putConstraint(SpringLayout.WEST, connectButton, LEFT_ALIGNED, SpringLayout.WEST,
                firstChannelTextField);

        serverLayout.putConstraint(SpringLayout.NORTH, autoConnectToFavourites, TOP_ALIGNED, SpringLayout.NORTH,
                userNameLabel);
        serverLayout.putConstraint(SpringLayout.WEST, autoConnectToFavourites, LEFT_SPACING, SpringLayout.EAST,
                serverUseProxyLabel);

        serverLayout.putConstraint(SpringLayout.NORTH, favouritesScroller, TOP_SPACING, SpringLayout.SOUTH,
                autoConnectToFavourites);
        serverLayout.putConstraint(SpringLayout.WEST, favouritesScroller, LEFT_ALIGNED, SpringLayout.WEST,
                autoConnectToFavourites);
        serverLayout.putConstraint(SpringLayout.EAST, favouritesScroller, LEFT_ALIGNED, SpringLayout.EAST,
                autoConnectToFavourites);
        serverLayout.putConstraint(SpringLayout.SOUTH, favouritesScroller, TOP_SPACING, SpringLayout.SOUTH,
                connectButton);
    }

    private void setupClientOptionsPanelComponents()
    {

        // clientScroller.setPreferredSize(new Dimension(this.getSize()));
        // clientScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // Settings for these are loaded with the settings API
        // found in getClientSettings()
        optionsClientPanel.add(showEventTicker);
        optionsClientPanel.add(showUsersList);
        optionsClientPanel.add(enableClickableLinks);
        optionsClientPanel.add(showJoinsQuitsEventTicker);
        optionsClientPanel.add(showJoinsQuitsMainWindow);
        optionsClientPanel.add(logChannelText);
        optionsClientPanel.add(logServerActivity);
        optionsClientPanel.add(logClientText);
        optionsClientPanel.add(limitServerLines);
        optionsClientPanel.add(limitServerLinesCount);

        // limitServerLinesCount.setPreferredSize(new Dimension(50, 20));
        optionsClientPanel.add(limitChannelLines);

        optionsClientPanel.add(limitChannelLinesCount);

        // limitChannelLinesCount.setPreferredSize(new Dimension(50, 20));
        optionsClientPanel.add(enableTimeStamps);

        clientFontPanel = new FontPanel(getFont(), Constants.FRONTEND_PREFS);
        clientFontPanel.setPreferredSize(new Dimension(500, 48));
        clientFontPanel.getSaveButton().addActionListener(new SaveFontListener());
        optionsClientPanel.add(clientFontPanel);

        // Turn on labels at major tick mark.
        eventTickerDelay.setMajorTickSpacing(10);
        eventTickerDelay.setMinorTickSpacing(1);
        eventTickerDelay.setPaintTicks(true);

        eventTickerDelay.setPaintLabels(true);
        // eventTickerDelay.setMaximumSize(new Dimension(400, 40));

        eventTickerDelay.setToolTipText("Event Ticker movement delay (Lower is faster)");

        optionsClientPanel.add(eventTickerLabel);
        optionsClientPanel.add(eventTickerDelay);

        saveSettings.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                setClientSettings();
            }
        });

        setupClientOptionsLayout();
        // optionsRightPanel.add(optionsClientPanel, "Client");
    }

    /**
     * Aligns components on the Client Options Panel
     */
    private void setupClientOptionsLayout()
    {
        SpringLayout clientLayout = new SpringLayout();
        optionsClientPanel.setLayout(clientLayout);

        // Used to make it more obvious what is going on -
        // and perhaps more readable.
        // 0 means THAT edge will be flush with the opposing components edge
        // Yes, negative numbers will make it overlap
        final int TOP_SPACING = 6;
        final int TOP_ALIGNED = 0;
        final int LEFT_ALIGNED = 0;
        final int LEFT_SPACING = 0;

        // Components are aligned off the top label
        clientLayout.putConstraint(SpringLayout.WEST, showEventTicker, 6, SpringLayout.WEST, optionsClientPanel);
        clientLayout.putConstraint(SpringLayout.NORTH, showEventTicker, 12, SpringLayout.NORTH, optionsClientPanel);

        clientLayout.putConstraint(SpringLayout.NORTH, showUsersList, TOP_SPACING, SpringLayout.SOUTH, showEventTicker);
        clientLayout.putConstraint(SpringLayout.WEST, showUsersList, LEFT_ALIGNED, SpringLayout.WEST, showEventTicker);

        clientLayout.putConstraint(SpringLayout.NORTH, enableClickableLinks, TOP_SPACING, SpringLayout.SOUTH,
                showUsersList);

        clientLayout.putConstraint(SpringLayout.WEST, enableClickableLinks, LEFT_ALIGNED, SpringLayout.WEST,
                showUsersList);

        clientLayout.putConstraint(SpringLayout.NORTH, showJoinsQuitsEventTicker, TOP_SPACING, SpringLayout.SOUTH,
                enableClickableLinks);
        clientLayout.putConstraint(SpringLayout.WEST, showJoinsQuitsEventTicker, LEFT_ALIGNED, SpringLayout.WEST,
                enableClickableLinks);

        clientLayout.putConstraint(SpringLayout.NORTH, showJoinsQuitsMainWindow, TOP_SPACING, SpringLayout.SOUTH,
                showJoinsQuitsEventTicker);
        clientLayout.putConstraint(SpringLayout.WEST, showJoinsQuitsMainWindow, LEFT_ALIGNED, SpringLayout.WEST,
                showJoinsQuitsEventTicker);

        clientLayout.putConstraint(SpringLayout.NORTH, logChannelText, TOP_SPACING, SpringLayout.SOUTH,
                showJoinsQuitsMainWindow);
        clientLayout.putConstraint(SpringLayout.WEST, logChannelText, LEFT_ALIGNED, SpringLayout.WEST,
                showJoinsQuitsMainWindow);

        clientLayout.putConstraint(SpringLayout.NORTH, logServerActivity, TOP_SPACING, SpringLayout.SOUTH,
                logChannelText);
        clientLayout.putConstraint(SpringLayout.WEST, logServerActivity, LEFT_ALIGNED, SpringLayout.WEST,
                logChannelText);

        clientLayout.putConstraint(SpringLayout.NORTH, logClientText, TOP_SPACING, SpringLayout.SOUTH,
                logServerActivity);
        clientLayout.putConstraint(SpringLayout.WEST, logClientText, LEFT_ALIGNED, SpringLayout.WEST,
                logServerActivity);

        clientLayout.putConstraint(SpringLayout.NORTH, limitServerLines, TOP_SPACING, SpringLayout.SOUTH,
                logClientText);
        clientLayout.putConstraint(SpringLayout.WEST, limitServerLines, LEFT_ALIGNED, SpringLayout.WEST, logClientText);

        clientLayout.putConstraint(SpringLayout.NORTH, limitServerLinesCount, TOP_ALIGNED, SpringLayout.NORTH,
                limitServerLines);
        clientLayout.putConstraint(SpringLayout.WEST, limitServerLinesCount, TOP_SPACING, SpringLayout.EAST,
                limitServerLines);

        clientLayout.putConstraint(SpringLayout.NORTH, limitChannelLines, TOP_SPACING, SpringLayout.SOUTH,
                limitServerLines);
        clientLayout.putConstraint(SpringLayout.WEST, limitChannelLines, LEFT_ALIGNED, SpringLayout.WEST,
                limitServerLines);

        clientLayout.putConstraint(SpringLayout.NORTH, limitChannelLinesCount, TOP_ALIGNED, SpringLayout.NORTH,
                limitChannelLines);
        clientLayout.putConstraint(SpringLayout.WEST, limitChannelLinesCount, LEFT_SPACING, SpringLayout.EAST,
                limitChannelLines);

        clientLayout.putConstraint(SpringLayout.NORTH, enableTimeStamps, TOP_SPACING, SpringLayout.SOUTH,
                limitChannelLines);
        clientLayout.putConstraint(SpringLayout.WEST, enableTimeStamps, LEFT_ALIGNED, SpringLayout.WEST,
                limitChannelLines);

        clientLayout.putConstraint(SpringLayout.NORTH, clientFontPanel, TOP_SPACING, SpringLayout.SOUTH,
                enableTimeStamps);
        clientLayout.putConstraint(SpringLayout.WEST, clientFontPanel, LEFT_SPACING, SpringLayout.WEST,
                enableTimeStamps);


        clientLayout.putConstraint(SpringLayout.NORTH, eventTickerLabel, TOP_SPACING, SpringLayout.SOUTH,
                clientFontPanel);
        clientLayout.putConstraint(SpringLayout.WEST, eventTickerLabel, LEFT_ALIGNED, SpringLayout.WEST,
                clientFontPanel);

        clientLayout.putConstraint(SpringLayout.NORTH, eventTickerDelay, TOP_SPACING, SpringLayout.SOUTH,
                eventTickerLabel);
        clientLayout.putConstraint(SpringLayout.WEST, eventTickerDelay, LEFT_ALIGNED, SpringLayout.WEST,
                eventTickerLabel);
    }

    private void setupFavouritesOptionsPanel()
    {
        // optionsFavouritesPanel.setLayout(new BorderLayout());
        // optionsFavouritesPanel.add(autoConnectToFavourites, BorderLayout.NORTH);

        // optionsFavouritesPanel.add(favouritesScroller, BorderLayout.LINE_START);


        // optionsRightPanel.add(optionsFavouritesPanel, "Favourites");
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

        public FavouritesItem(String favServer, String favChannel)
        {
            this.favServer = favServer;
            this.favChannel = favChannel;
            settingsPath = Constants.FAVOURITES_PREFS.node(favServer).node(favChannel);

            favFontDialog = new FontDialog("Font: " + favChannel, UserGUI.this.getFont(), settingsPath);
            favFontDialog.addSaveListener(new SaveChannelFontListener());
            myMenu = new FavouritesPopUp();
        }

        @Override
        public String toString()
        {
            return favServer + ":" + favChannel;
        }


        protected class SaveChannelFontListener implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                for (int index = 0; index < tabbedPane.getComponents().length; index++)
                {
                    Component tab = tabbedPane.getComponentAt(index);

                    if (tab instanceof IRCRoomBase)
                    {
                        IRCRoomBase tabRoom = (IRCRoomBase) tab;
                        if (tabRoom.getServer().equals(favServer) && tabRoom.getName().equals(favChannel))
                        {
                            tabRoom.getFontPanel().setFont(favFontDialog.getFontPanel().getFont(), true);
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

            public FavouritesPopUp()
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
            public void actionPerformed(ActionEvent arg0)
            {
                if (favouritesList.getSelectedIndex() > -1)
                {
                    FavouritesItem tempItem = favouritesListModel.elementAt(favouritesList.getSelectedIndex());
                    tempItem.favFontDialog.getFontPanel().loadFont();
                    tempItem.favFontDialog.setVisible(true);
                }
            }
        }

        private class RemoveFavourite implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                if (favouritesList.getSelectedIndex() > -1)
                {
                    FavouritesItem tempItem = favouritesListModel.elementAt(favouritesList.getSelectedIndex());
                    removeFavourite(tempItem.favServer, tempItem.favChannel);
                    Preferences channelNode =
                            Constants.FAVOURITES_PREFS.node(tempItem.favServer).node(tempItem.favChannel);
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
    public void addFavourite(String favServer, String favChannel)
    {
        favouritesListModel.addElement(new FavouritesItem(favServer, favChannel));

        Constants.FAVOURITES_PREFS.node(favServer).node(favChannel).put("PORT", getCreatedServer(favServer).getPort());
    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isFavourite(urChatBasic.frontend.IRCChannel)
     */
    @Override
    public Boolean isFavourite(IRCRoomBase channel)
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
    public void removeFavourite(String favServer, String favChannel)
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
        public void mousePressed(MouseEvent e)
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

        public void mouseReleased(MouseEvent e)
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

        private void doPop(MouseEvent e)
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
        public void actionPerformed(ActionEvent arg0)
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
    public void setupServerTab(IRCServerBase server)
    {
        if (server instanceof IRCServer)
        {
            tabbedPane.addTab(server.getName(), ((IRCServer) server).icon, ((IRCServer) server));
            tabbedPane.setSelectedIndex(tabbedPane.indexOfComponent(((IRCServer) server)));
            ((IRCServer) server).getUserTextBox().requestFocus();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#sendGlobalMessage(java.lang.String, java.lang.String)
     */
    @Override
    public void sendGlobalMessage(String message, String sender)
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
    public void connectFavourites(IRCServerBase server)
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
    public void shutdownAll()
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
    public void quitServers()
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

        cleanUpSettings();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#quitServer(urChatBasic.base.IRCServerBase)
     */
    @Override
    public void quitServer(IRCServerBase server)
    {
        server.disconnect();
        tabbedPane.remove((IRCServer) server);
        createdServers.remove(server);
    }

    /**
     * Saves the settings into the registry/Settings API
     */
    public void setClientSettings()
    {
        Constants.FRONTEND_PREFS.put(Constants.KEY_FIRST_CHANNEL, firstChannelTextField.getText());
        Constants.FRONTEND_PREFS.put(Constants.KEY_FIRST_SERVER, servernameTextField.getText());
        Constants.FRONTEND_PREFS.put(Constants.KEY_FIRST_PORT, serverPortTextField.getText());
        Constants.FRONTEND_PREFS.put(Constants.KEY_AUTH_TYPE, authenticationTypeChoice.getSelectedItem().toString());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_USE_TLS, serverTLSCheckBox.isSelected());
        Constants.FRONTEND_PREFS.put(Constants.KEY_PROXY_HOST, proxyHostNameTextField.getText());
        Constants.FRONTEND_PREFS.put(Constants.KEY_PROXY_PORT, proxyPortTextField.getText());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_USE_PROXY, serverProxyCheckBox.isSelected());
        Constants.FRONTEND_PREFS.put(Constants.KEY_NICK_NAME, userNameTextField.getText());
        Constants.FRONTEND_PREFS.put(Constants.KEY_REAL_NAME, realNameTextField.getText());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_TIME_STAMPS, enableTimeStamps.isSelected());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_EVENT_TICKER_ACTIVE, showEventTicker.isSelected());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_USERS_LIST_ACTIVE, showUsersList.isSelected());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_CLICKABLE_LINKS_ENABLED, enableClickableLinks.isSelected());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_EVENT_TICKER_JOINS_QUITS,
                showJoinsQuitsEventTicker.isSelected());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_MAIN_WINDOW_JOINS_QUITS,
                showJoinsQuitsMainWindow.isSelected());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_LOG_CHANNEL_HISTORY, logChannelText.isSelected());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_LOG_SERVER_ACTIVITY, logServerActivity.isSelected());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_LIMIT_CHANNEL_LINES, limitChannelLines.isSelected());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_AUTO_CONNECT_FAVOURITES,
                autoConnectToFavourites.isSelected());
        Constants.FRONTEND_PREFS.put(Constants.KEY_LIMIT_CHANNEL_LINES_COUNT, limitChannelLinesCount.getText());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_LIMIT_SERVER_LINES, limitServerLines.isSelected());
        Constants.FRONTEND_PREFS.put(Constants.KEY_LIMIT_SERVER_LINES_COUNT, limitServerLinesCount.getText());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_LOG_CLIENT_TEXT, logClientText.isSelected());
        Constants.FRONTEND_PREFS.put(Constants.KEY_FONT_FAMILY, clientFontPanel.getFont().getFamily());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_FONT_BOLD, clientFontPanel.getFont().isBold());
        Constants.FRONTEND_PREFS.putBoolean(Constants.KEY_FONT_ITALIC, clientFontPanel.getFont().isItalic());
        Constants.FRONTEND_PREFS.putInt(Constants.KEY_FONT_SIZE, clientFontPanel.getFont().getSize());
        Constants.FRONTEND_PREFS.putInt(Constants.KEY_EVENT_TICKER_DELAY, eventTickerDelay.getValue());
        Constants.FRONTEND_PREFS.putInt(Constants.KEY_WINDOW_X, (int) DriverGUI.frame.getBounds().getX());
        Constants.FRONTEND_PREFS.putInt(Constants.KEY_WINDOW_Y, (int) DriverGUI.frame.getBounds().getY());
        Constants.FRONTEND_PREFS.putInt(Constants.KEY_WINDOW_WIDTH, (int) DriverGUI.frame.getBounds().getWidth());
        Constants.FRONTEND_PREFS.putInt(Constants.KEY_WINDOW_HEIGHT, (int) DriverGUI.frame.getBounds().getHeight());
    }

    /**
     * Loads the settings from the registry/Settings API
     */
    public void getClientSettings()
    {
        firstChannelTextField
                .setText(Constants.FRONTEND_PREFS.get(Constants.KEY_FIRST_CHANNEL, Constants.DEFAULT_FIRST_CHANNEL));
        servernameTextField
                .setText(Constants.FRONTEND_PREFS.get(Constants.KEY_FIRST_SERVER, Constants.DEFAULT_FIRST_SERVER));
        serverPortTextField
                .setText(Constants.FRONTEND_PREFS.get(Constants.KEY_FIRST_PORT, Constants.DEFAULT_FIRST_PORT));
        serverTLSCheckBox
                .setSelected(Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_USE_TLS, Constants.DEFAULT_USE_TLS));

        authenticationTypeChoice.setSelectedItem(CapabilityTypes
                .getCapType(Constants.FRONTEND_PREFS.get(Constants.KEY_AUTH_TYPE, Constants.DEFAULT_AUTH_TYPE)));

        proxyHostNameTextField
                .setText(Constants.FRONTEND_PREFS.get(Constants.KEY_PROXY_HOST, Constants.DEFAULT_PROXY_HOST));
        proxyPortTextField
                .setText(Constants.FRONTEND_PREFS.get(Constants.KEY_PROXY_PORT, Constants.DEFAULT_PROXY_PORT));
        serverProxyCheckBox
                .setSelected(Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_USE_PROXY, Constants.DEFAULT_USE_PROXY));

        userNameTextField.setText(Constants.FRONTEND_PREFS.get(Constants.KEY_NICK_NAME, Constants.DEFAULT_NICK_NAME));
        realNameTextField.setText(Constants.FRONTEND_PREFS.get(Constants.KEY_REAL_NAME, Constants.DEFAULT_REAL_NAME));

        showUsersList.setSelected(Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_USERS_LIST_ACTIVE,
                Constants.DEFAULT_USERS_LIST_ACTIVE));

        showEventTicker.setSelected(Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_EVENT_TICKER_ACTIVE,
                Constants.DEFAULT_EVENT_TICKER_ACTIVE));

        enableClickableLinks.setSelected(Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_CLICKABLE_LINKS_ENABLED,
                Constants.DEFAULT_CLICKABLE_LINKS_ENABLED));

        enableTimeStamps.setSelected(
                Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_TIME_STAMPS, Constants.DEFAULT_TIME_STAMPS));
        showJoinsQuitsEventTicker.setSelected(Constants.FRONTEND_PREFS
                .getBoolean(Constants.KEY_EVENT_TICKER_JOINS_QUITS, Constants.DEFAULT_EVENT_TICKER_JOINS_QUITS));
        showJoinsQuitsMainWindow.setSelected(Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_MAIN_WINDOW_JOINS_QUITS,
                Constants.DEFAULT_MAIN_WINDOW_JOINS_QUITS));
        logChannelText.setSelected(Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_LOG_CHANNEL_HISTORY,
                Constants.DEFAULT_LOG_CHANNEL_HISTORY));
        logServerActivity.setSelected(Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_LOG_SERVER_ACTIVITY,
                Constants.DEFAULT_LOG_SERVER_ACTIVITY));
        limitChannelLines.setSelected(Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_LIMIT_CHANNEL_LINES,
                Constants.DEFAULT_LIMIT_CHANNEL_LINES));
        limitChannelLinesCount.setText(Constants.FRONTEND_PREFS.get(Constants.KEY_LIMIT_CHANNEL_LINES_COUNT,
                Constants.DEFAULT_LIMIT_CHANNEL_LINES_COUNT));
        limitServerLines.setSelected(Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_LIMIT_SERVER_LINES,
                Constants.DEFAULT_LIMIT_SERVER_LINES));
        limitServerLinesCount.setText(Constants.FRONTEND_PREFS.get(Constants.KEY_LIMIT_SERVER_LINES_COUNT,
                Constants.DEFAULT_LIMIT_SERVER_LINES_COUNT));
        logClientText.setSelected(
                Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_LOG_CLIENT_TEXT, Constants.DEFAULT_LOG_CLIENT_TEXT));

        loadFont();

        eventTickerDelay.setValue(Constants.FRONTEND_PREFS.getInt(Constants.KEY_EVENT_TICKER_DELAY,
                Constants.DEFAULT_EVENT_TICKER_DELAY));
        autoConnectToFavourites.setSelected(Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_AUTO_CONNECT_FAVOURITES,
                Constants.DEFAULT_AUTO_CONNECT_FAVOURITES));
        DriverGUI.frame.setBounds(Constants.FRONTEND_PREFS.getInt(Constants.KEY_WINDOW_X, Constants.DEFAULT_WINDOW_X),
                Constants.FRONTEND_PREFS.getInt(Constants.KEY_WINDOW_Y, Constants.DEFAULT_WINDOW_Y),
                Constants.FRONTEND_PREFS.getInt(Constants.KEY_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_WIDTH),
                Constants.FRONTEND_PREFS.getInt(Constants.KEY_WINDOW_HEIGHT, Constants.DEFAULT_WINDOW_HEIGHT));
        this.setPreferredSize(new Dimension(
                Constants.FRONTEND_PREFS.getInt(Constants.KEY_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_WIDTH),
                Constants.FRONTEND_PREFS.getInt(Constants.KEY_WINDOW_HEIGHT, Constants.DEFAULT_WINDOW_HEIGHT)));

        // TODO Add Port number to favourites.
        try
        {
            for (String serverNode : Constants.FAVOURITES_PREFS.childrenNames())
            {
                for (String channelNode : Constants.FAVOURITES_PREFS.node(serverNode).childrenNames())
                {
                    if (Constants.FAVOURITES_PREFS.node(serverNode).node(channelNode).keys().length > 0)
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
    public void removeClientSetting(String node, String key)
    {
        Constants.FRONTEND_PREFS.node(node).remove(key);
    }

    @Override
    public void cleanUpSettings()
    {
        Constants.LOGGER.log(Level.INFO, "Cleaning up settings");
        try
        {
            for (String serverNode : Constants.FAVOURITES_PREFS.childrenNames())
            {
                for (String channelNode : Constants.FAVOURITES_PREFS.node(serverNode).childrenNames())
                {
                    if (Constants.FAVOURITES_PREFS.node(serverNode).node(channelNode).keys().length == 0)
                    {
                        Constants.FAVOURITES_PREFS.node(serverNode).node(channelNode).removeNode();
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
     * @see urChatBasic.frontend.UserGUIBase#getEventTickerDelay()
     */
    @Override
    public int getEventTickerDelay()
    {
        return eventTickerDelay.getValue();
    }

    class UCAuthTypeComboBoxChangeHandler implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            authenticationTypeChoice.runChangeListener();

            if (authenticationType().equals(CapabilityTypes.NONE.getType()))
            {
                passwordLabel.setVisible(false);
                passwordTextField.setVisible(false);
            } else
            {
                passwordLabel.setText(authenticationTypeChoice.getPasswordFieldName());
                passwordLabel.setVisible(true);
                passwordTextField.setVisible(true);
            }
        }
    }

    /**
     * Used to change which panel to show when you choose an option under the Options Tab.
     *
     * @author Matt
     *
     */
    class OptionsListSelectionHandler implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            if (!(lsm.isSelectionEmpty()))
            {
                // Find out which indexes are selected.
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++)
                {
                    if (lsm.isSelectedIndex(i))
                    {
                        CardLayout cl = (CardLayout) (optionsRightPanel.getLayout());
                        cl.show(optionsRightPanel, (String) optionsArray.getElementAt(i));
                    }
                }
            }
        }
    }

    private void setupTabbedPane()
    {
        tabbedPane.addChangeListener(new MainTabbedPanel_changeAdapter(this));
        tabbedPane.addMouseListener(new TabbedMouseListener());
        setupOptionsPanel();
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
        public void mouseClicked(MouseEvent e)
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
                                getCreatedServer(((IRCPrivate) selectedComponent).getServer().getName());
                        tempServer.quitPrivateRooms((IRCPrivate) selectedComponent);
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
    private void TabbedPanel_stateChanged(ChangeEvent e)
    {
        int index = tabbedPane.getSelectedIndex();
        if (index > -1)
        {
            Component selectedComponent = tabbedPane.getComponentAt(index);
            if (selectedComponent instanceof IRCRoomBase)
            {
                IRCRoomBase tempTab = (IRCRoomBase) selectedComponent;
                tempTab.showEventTicker(isShowingEventTicker());
                if (isShowingUsersList())
                {
                    tempTab.showUsersList();
                } else
                {
                    tempTab.hideUsersList();
                }

                tempTab.getUserTextBox().requestFocus();
                tempTab.enableFocus();
            }
        }
    }

    class MainTabbedPanel_changeAdapter implements javax.swing.event.ChangeListener
    {
        UserGUI adaptee;

        MainTabbedPanel_changeAdapter(UserGUI adaptee)
        {
            this.adaptee = adaptee;
        }

        public void stateChanged(ChangeEvent e)
        {
            adaptee.TabbedPanel_stateChanged(e);
        }
    }

    protected class SaveFontListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            for (int index = 0; index < tabbedPane.getComponents().length; index++)
            {
                Component tab = tabbedPane.getComponentAt(index);
                tab.setFont(clientFontPanel.getFont());

                if (tab instanceof IRCRoomBase)
                {
                    ((IRCRoomBase) tab).getFontPanel().setDefaultFont(clientFontPanel.getFont());
                }
            }

            for (int index = 0; index < favouritesList.getModel().getSize(); index++)
            {
                FavouritesItem favouriteItem = favouritesList.getModel().getElementAt(index);
                favouriteItem.favFontDialog.getFontPanel().setDefaultFont(clientFontPanel.getFont());
                favouriteItem.favFontDialog.getFontPanel().loadFont();
            }
        }
    }


    public UserGUI()
    {
        // this.creationTime = (new Date()).toString();

        // Create the initial size of the panel
        setupTabbedPane();
        this.setLayout(new BorderLayout());
        this.add(tabbedPane, BorderLayout.CENTER);

        // this.setBackground(Color.gray);
        getClientSettings();
    }

    // Disables focus in the text pane
    public void lostFocus()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
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
    public void regainedFocus()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                TabbedPanel_stateChanged(null);
            }
        });
    }

    @Override
    public Font getFont()
    {
        if (clientFontPanel != null)
        {
            return clientFontPanel.getFont();
        }

        return super.getFont();
    }

    public void loadFont()
    {
        // SAVED FONT STUFF
        int savedFontBoldItalic = 0;

        if (Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_FONT_BOLD, Constants.DEFAULT_FONT_GENERAL.isBold()))
            savedFontBoldItalic = Font.BOLD;
        if (Constants.FRONTEND_PREFS.getBoolean(Constants.KEY_FONT_ITALIC, Constants.DEFAULT_FONT_GENERAL.isItalic()))
            savedFontBoldItalic |= Font.ITALIC;

        Font savedFont = new Font(
                Constants.FRONTEND_PREFS.get(Constants.KEY_FONT_FAMILY, Constants.DEFAULT_FONT_GENERAL.getFamily()),
                savedFontBoldItalic,
                Constants.FRONTEND_PREFS.getInt(Constants.KEY_FONT_SIZE, Constants.DEFAULT_FONT_GENERAL.getSize()));

        clientFontPanel.setFont(savedFont);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#run()
     */
    @Override
    public void run()
    {
        // Auto-generated method stub
    }
}
