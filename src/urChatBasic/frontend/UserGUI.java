package urChatBasic.frontend;

import java.awt.*;
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
import urChatBasic.base.IRCChannelBase;
import urChatBasic.base.IRCServerBase;
import urChatBasic.frontend.dialogs.FontDialog;
import urChatBasic.frontend.panels.MainOptionsPanel;
import urChatBasic.frontend.panels.ProfilePanel;
import urChatBasic.frontend.panels.ConnectionPanel;
import urChatBasic.frontend.panels.InterfacePanel;
import urChatBasic.frontend.panels.UROptionsPanel;
import urChatBasic.frontend.utils.URPanels;
import urChatBasic.base.UserGUIBase;
import urChatBasic.base.Constants.EventType;
import urChatBasic.base.Constants.Placement;
import urChatBasic.base.Constants.Size;
import urChatBasic.frontend.LineFormatter.ClickableText;
import urChatBasic.frontend.components.*;

public class UserGUI extends JPanel implements Runnable, UserGUIBase
{
    /**
     *
     */
    private final long serialVersionUID = 2595649865577419300L;
    // private String creationTime = (new Date()).toString();
    // Tabs
    public JTabbedPane tabbedPane = new DnDTabbedPane();
    public Component previousSelectedTab;
    public Component currentSelectedTab;

    // Profile Preferences
    protected EventListenerList profileListenerList = new EventListenerList();
    protected transient ActionEvent actionEvent = null;

    // Options Panel
    private JPanel optionsMainPanel = new MainOptionsPanel();
    // Server Options Panel
    private final UROptionsPanel connectionPanel = new ConnectionPanel((MainOptionsPanel) optionsMainPanel, Optional.of(0));

    public final UROptionsPanel interfacePanel = new InterfacePanel((MainOptionsPanel) optionsMainPanel, Optional.of(1));

    // Appearance Options Panel
    private final UROptionsPanel appearancePanel = new UROptionsPanel("Appearance", (MainOptionsPanel) optionsMainPanel, Optional.of(2));

    // Profile Panel
    private final UROptionsPanel profilePanel = new ProfilePanel((MainOptionsPanel) optionsMainPanel);


    private final JComboBox<LookAndFeelInfo> lafOptions =
            new JComboBox<LookAndFeelInfo>(UIManager.getInstalledLookAndFeels());

    // Appearance Panel
    private FontPanel clientFontPanel;
    /**
     * This should only ever be changed if the LAF changes
     */
    private URStyle defaultStyle = new URStyle("", UIManager.getFont(Constants.DEFAULT_FONT_STRING), UIManager.getColor(Constants.DEFAULT_FOREGROUND_STRING),
            UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING));
    private final JTextField timeStampField = new JTextField();
    private final JTextField nickFormatField = new JTextField();
    public final JTextPane previewTextArea = new JTextPane();
    private final JScrollPane previewTextScroll = new JScrollPane(previewTextArea);
    private final JLabel styleLabel = new JLabel("Mouse over text to view style, right-click to edit.");
    public LineFormatter previewLineFormatter;



    // Created Servers/Tabs
    public final List<IRCServerBase> createdServers = new ArrayList<IRCServerBase>();

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

    public int getTabIndex (IRCChannelBase targetTab)
    {
        for (int i = 0; i < DriverGUI.gui.tabbedPane.getTabCount(); i++)
        {
            Component currentTab = DriverGUI.gui.tabbedPane.getComponentAt(i);
            if (currentTab instanceof IRCChannelBase && currentTab.equals(targetTab))
            {
                return i;
            }
        }

        return -1;
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

    public List<IRCServerBase> getCreatedServers ()
    {
        return createdServers;
    }

    public ProfilePicker getProfilePicker ()
    {
        return (((MainOptionsPanel) optionsMainPanel).getProfilePicker());
    }

    public UROptionsPanel getProfilePanel ()
    {
        return profilePanel;
    }

    public UROptionsPanel getConnectionPanel ()
    {
        return interfacePanel;
    }


    public UROptionsPanel getAppearancePanel ()
    {
        return appearancePanel;
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

    // TODO: Is this needed any more or should we be adding IRCServer only?
    // @Override
    // public void addToCreatedServers (String serverName)
    // {
    //     if (getCreatedServer(serverName) == null)
    //     {
    //         createdServers.add(new IRCServer(serverName.trim(), userNameTextField.getText().trim(),
    //                 realNameTextField.getText().trim(), new String(passwordTextField.getPassword()),
    //                 serverPortTextField.getText().trim(), serverTLSCheckBox.isSelected(),
    //                 proxyHostNameTextField.getText(), proxyPortTextField.getText(), serverProxyCheckBox.isSelected()));
    //     }
    // }

    @Override
    public void addToCreatedServers (IRCServerBase newServer)
    {
        if(getCreatedServer(newServer.getName()) != null)
            createdServers.remove(newServer);

        createdServers.add(newServer);
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

    private void setupAppearancePanel ()
    {
        URPanels.addToPanel(appearancePanel, lafOptions, "Theme", Placement.DEFAULT, Size.MEDIUM, null);

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

        URProfilesUtil.addListener(EventType.CHANGE, e -> {
            clientFontPanel.setSettingsPath(URProfilesUtil.getActiveProfilePath());
        });

        // clientFontPanel.getSaveButton().addActionListener(new SaveFontListener());
        clientFontPanel.addFontSaveListener(new SaveFontListener());
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

        nickFormatField.addKeyListener(new KeyListener()
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

        URPanels.addToPanel(appearancePanel, clientFontPanel, "Profile Font", Placement.DEFAULT, null, null);
        URPanels.addToPanel(appearancePanel, timeStampField, "Timestamp", Placement.DEFAULT, Size.MEDIUM, null);
        URPanels.addToPanel(appearancePanel, nickFormatField, "Nick", Placement.RIGHT, Size.MEDIUM, null);

        URPanels.addToPanel(appearancePanel, previewTextScroll, "Font Preview", Placement.DEFAULT, null, null);
        URPanels.addToPanel(appearancePanel, styleLabel, "Preview Style", Placement.DEFAULT, null, null);
    }

    public void updatePreviewTextArea ()
    {
        StyledDocument previewDoc = previewTextArea.getStyledDocument();

        // previewTextArea.setFont(clientFontPanel.getFont());
        if (previewLineFormatter == null)
        {
            previewLineFormatter = new LineFormatter(clientFontPanel.getStyle(), previewTextArea, null, URProfilesUtil.getActiveProfilePath());

            URProfilesUtil.addListener(EventType.CHANGE, e -> {
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

                styleFontDialog.addFontSaveListener(new SaveFontListener());
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

            if (isClickableText != null && ((InterfacePanel) interfacePanel).isClickableLinksEnabled())
            {
                previewTextArea.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else
            {
                previewTextArea.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    public String getTimeStampString (Date date)
    {
        SimpleDateFormat chatDateFormat = new SimpleDateFormat(timeStampField.getText());

        return chatDateFormat.format(date);
    }

    public void setTimeLineString (String newFormat)
    {
        timeStampField.setText(newFormat);
    }

    public String[] getNickFormatString (String nick)
    {
        String[] nickParts = new String[3];
        nickParts[1] = nick;
        String nickString = nickFormatField.getText();

        if(nickString.indexOf("nick") >= 0)
        {
            int nickIndex = nickString.indexOf("nick");
            String leftPart = nickString.substring(0, nickIndex);
            String rightPart = nickString.substring(nickIndex + 4);

            nickParts[0] = leftPart;
            nickParts[2] = rightPart;
        } else if(nickString.length() == 1)
        {
            // both parts are the same
            nickParts[0] = nickString;
            nickParts[2] = nickString;
        } else
        {
            // split it in half
            nickParts[0] = nickString.substring(0, (int) Math.floor((float) nickString.length() / 2));
            nickParts[2] = nickString.substring((int) Math.ceil((float) nickString.length() / 2));
        }

        return nickParts;
    }

    public void setNickFormatString (String newFormat)
    {
        nickFormatField.setText(newFormat);
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
                    boolean iconsShown = (boolean) URPanels.getKeyComponentValue(Constants.KEY_SHOW_TAB_ICON);
                    int currentServerIndex = DriverGUI.gui.getTabIndex((IRCChannelBase) server);
                    if(currentServerIndex < 0)
                    {
                        tabbedPane.addTab(server.getName(), iconsShown ? ((IRCChannelBase) server).icon : null, ((IRCServer) server));
                        setCurrentTab(server.getName());
                    }
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
        URPreferencesUtil.putPref(Constants.KEY_TIME_STAMP_FORMAT, timeStampField.getText(), URProfilesUtil.getActiveProfilePath());
        URPreferencesUtil.putPref(Constants.KEY_LAF_NAME, ((LookAndFeelInfo) lafOptions.getSelectedItem()).getClassName(), URProfilesUtil.getActiveProfilePath());
        URPreferencesUtil.saveStyle(defaultStyle, clientFontPanel.getStyle(), URProfilesUtil.getActiveProfilePath());
        URPreferencesUtil.putPref(Constants.KEY_WINDOW_X, (int) DriverGUI.frame.getBounds().getX(), URProfilesUtil.getActiveProfilePath());
        URPreferencesUtil.putPref(Constants.KEY_WINDOW_Y, (int) DriverGUI.frame.getBounds().getY(), URProfilesUtil.getActiveProfilePath());
        URPreferencesUtil.putPref(Constants.KEY_WINDOW_WIDTH, (int) DriverGUI.frame.getBounds().getWidth(), URProfilesUtil.getActiveProfilePath());
        URPreferencesUtil.putPref(Constants.KEY_WINDOW_HEIGHT, (int) DriverGUI.frame.getBounds().getHeight(), URProfilesUtil.getActiveProfilePath());

        connectionPanel.putPreferences();
        interfacePanel.putPreferences();
        appearancePanel.putPreferences();
    }

    /**
     * Loads the settings from the registry/Settings API
     */
    public void getClientSettings (boolean loadWindowSettings)
    {
        lafOptions.setSelectedItem(getLAF(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_LAF_NAME, Constants.DEFAULT_LAF_NAME)));

        clientFontPanel.loadStyle();

        timeStampField
                .setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_TIME_STAMP_FORMAT, Constants.DEFAULT_TIME_STAMP_FORMAT));

        nickFormatField
                .setText(URProfilesUtil.getActiveProfilePath().get(Constants.KEY_NICK_FORMAT, Constants.DEFAULT_NICK_FORMAT));


        updatePreviewTextArea();

        if (loadWindowSettings && DriverGUI.frame != null)
        {
            DriverGUI.frame.setBounds(URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_WINDOW_X, Constants.DEFAULT_WINDOW_X),
                    URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_WINDOW_Y, Constants.DEFAULT_WINDOW_Y),
                    URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_WIDTH),
                    URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_WINDOW_HEIGHT, Constants.DEFAULT_WINDOW_HEIGHT));

            this.setPreferredSize(
                    new Dimension(URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_WIDTH),
                            URProfilesUtil.getActiveProfilePath().getInt(Constants.KEY_WINDOW_HEIGHT, Constants.DEFAULT_WINDOW_HEIGHT)));
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
                        ((IRCChannelBase) selectedComponent).getServer().quitChannel((IRCChannelBase) selectedComponent);
                    } else
                    {
                        ((IRCChannelBase) selectedComponent).myMenu.show(tabbedPane, e.getX(), e.getY());
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
            if (selectedComponent instanceof IRCChannelBase)
            {
                IRCChannelBase tempTab = (IRCChannelBase) selectedComponent;
                if (!(selectedComponent instanceof IRCServer))
                {
                    tempTab.toggleEventTicker(((InterfacePanel) interfacePanel).isShowingEventTicker());
                    tempTab.toggleUsersList(((InterfacePanel) interfacePanel).isShowingUsersList());
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

                if (tab instanceof IRCChannelBase)
                {
                    tab.setFont(getStyle().getFont());
                    ((IRCChannelBase) tab).getFontPanel().setDefaultStyle(getStyle());
                }
            }

            // TODO: Favourites handling to be done elsewhere
            // for (int index = 0; index < favouritesList.getModel().getSize(); index++)
            // {
            //     FavouritesItem favouriteItem = favouritesList.getModel().getElementAt(index);
            //     if(favouriteItem.favFontDialog != null)
            //     {
            //         favouriteItem.favFontDialog.getFontPanel().setDefaultStyle(getStyle());
            //         favouriteItem.favFontDialog.getFontPanel().loadStyle();
            //     }
            // }

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
                if (selectedComponent instanceof IRCChannelBase)
                {
                    IRCChannelBase tempTab = (IRCChannelBase) selectedComponent;
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

    public FontPanel getFontPanel ()
    {
        return clientFontPanel;
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

        Constants.LOGGER.error( "Unable to set LAF to " + lafClassName);

        // Set to the System LAF if we've chosen an invalid/unavailable LAF theme
        return getLAF(UIManager.getSystemLookAndFeelClassName());
    }

    public void setNewLAF (String newLAFname)
    {
        // String previousDefaultForeground = URColour.hexEncode(UIManager.getColor(Constants.DEFAULT_FOREGROUND_STRING));
        // String previousDefaultBackground = URColour.hexEncode(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING));
        // Font previousDefaultFont = getFont();

        Constants.LOGGER.info( "Setting to LookAndFeel to " + newLAFname);
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
            Constants.LOGGER.error("Failed to set Pluggable LAF! ", e);
        } finally
        {
            if (!flatLafAvailable)
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e)
                {
                    Constants.LOGGER.error("Failed to setLookAndFeel! ", e);
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

        if(DriverGUI.frame.isVisible())
            SwingUtilities.updateComponentTreeUI(DriverGUI.frame);

        updateExtras();

        // DriverGUI.frame.dispose();
        if(DriverGUI.frame.isVisible())
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

                    if (tab instanceof IRCChannelBase)
                    {
                        // tab.setFont(clientFontPanel.getFont());
                        IRCChannelBase channelTab = IRCChannelBase.class.cast(tab);
                        // channelTab.getFontPanel().setDefaultFont(clientFontPanel.getFont());
                        channelTab.getFontPanel().setDefaultStyle(defaultStyle);
                        // channelTab.resetLineFormatter();
                        channelTab.getLineFormatter().updateStyles(getStyle());
                        SwingUtilities.updateComponentTreeUI(channelTab.myMenu);
                        SwingUtilities.updateComponentTreeUI(channelTab.getFontPanel());
                    }
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
