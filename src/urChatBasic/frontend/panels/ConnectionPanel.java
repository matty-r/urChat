package urChatBasic.frontend.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.base.Constants.EventType;
import urChatBasic.base.Constants.Placement;
import urChatBasic.base.Constants.Size;
import urChatBasic.base.IRCRoomBase;
import urChatBasic.base.IRCServerBase;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.base.proxy.ProxyTypes;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.IRCServer;
import urChatBasic.frontend.components.URAuthTypeComboBox;
import urChatBasic.frontend.components.URProxyTypeComboBox;
import urChatBasic.frontend.dialogs.FontDialog;
import urChatBasic.frontend.dialogs.MessageDialog;
import urChatBasic.frontend.utils.URPanels;

public class ConnectionPanel extends UROptionsPanel {
    public static final String PANEL_DISPLAY_NAME = "Connection";

    // Identification
    private final JTextField userNameTextField = new JTextField("", 12);
    private final JTextField realNameTextField = new JTextField("");

    // Authentication
    private final URAuthTypeComboBox authenticationTypeChoice = new URAuthTypeComboBox();
    private final JPasswordField passwordTextField = new JPasswordField("");
    private final JCheckBox rememberPassCheckBox = new JCheckBox();

    // Connection
    private final JTextField servernameTextField = new JTextField("", 8);
    private final JTextField serverPortTextField = new JTextField("", 4);
    private final JCheckBox serverTLSCheckBox = new JCheckBox();
    private final JButton connectButton = new JButton("Connect");

    // Proxy
    private final JTextField proxyHostNameTextField = new JTextField("");
    private final JTextField proxyPortTextField = new JTextField("");
    private final URProxyTypeComboBox proxyTypeChoice = new URProxyTypeComboBox();

    private final JTextField firstChannelTextField = new JTextField("");

    // Favourites Panel
    private final JCheckBox autoConnectToFavourites = new JCheckBox("Automatically join favourite channels");
    private final DefaultListModel<FavouritesItem> favouritesListModel = new DefaultListModel<FavouritesItem>();
    private final JList<FavouritesItem> favouritesList = new JList<FavouritesItem>(favouritesListModel);
    private final JScrollPane favouritesScroller = new JScrollPane(favouritesList);



    public ConnectionPanel (MainOptionsPanel optionsPanel)
    {
        super(PANEL_DISPLAY_NAME, optionsPanel);
        setupConnectionPanel();
    }

    public ConnectionPanel (MainOptionsPanel optionsPanel, Optional<Integer> preferredIndex)
    {
        super(PANEL_DISPLAY_NAME, optionsPanel, preferredIndex);
        setupConnectionPanel();
    }

    /**
     * Adds all the components to the panel, with the related preference Keys for that component.
     * i.e showEventTicker is set via the KEY_EVENT_TICKER_ACTIVE key ('show event ticker')
     */
    private void setupConnectionPanel ()
    {
        URPanels.addToPanel(this, userNameTextField, "Nick", Placement.DEFAULT, Size.LARGE, Constants.KEY_NICK_NAME);
        URPanels.addToPanel(this, realNameTextField, "Real Name", Placement.DEFAULT, Size.LARGE, Constants.KEY_REAL_NAME);

        authenticationTypeChoice.addActionListener(new UCAuthTypeComboBoxChangeHandler());

        URPanels.addToPanel(this, authenticationTypeChoice, "Authentication Type", Placement.DEFAULT, null, Constants.KEY_AUTH_TYPE);
        URPanels.addToPanel(this, passwordTextField, "Password", Placement.DEFAULT, Size.LARGE, Constants.KEY_PASSWORD);
        URPanels.addToPanel(this, rememberPassCheckBox, "Remember", Placement.RIGHT, null, Constants.KEY_PASSWORD_REMEMBER);
        URPanels.addToPanel(this, servernameTextField, "Server", Placement.DEFAULT, Size.MEDIUM, Constants.KEY_FIRST_SERVER);
        URPanels.addToPanel(this, serverPortTextField, "Port", Placement.RIGHT, Size.SMALL, Constants.KEY_FIRST_PORT);
        URPanels.addToPanel(this, serverTLSCheckBox, "TLS", Placement.RIGHT, null, Constants.KEY_USE_TLS);
        URPanels.addToPanel(this, proxyHostNameTextField, "Proxy Host", Placement.DEFAULT, Size.MEDIUM, Constants.KEY_PROXY_HOST);
        URPanels.addToPanel(this, proxyPortTextField, "Port", Placement.RIGHT, Size.SMALL, Constants.KEY_PROXY_PORT);
        URPanels.addToPanel(this, proxyTypeChoice, "Proxy Type", Placement.RIGHT, null, Constants.KEY_PROXY_TYPE);
        URPanels.addToPanel(this, firstChannelTextField, "Channel", Placement.DEFAULT, Size.MEDIUM, Constants.KEY_FIRST_CHANNEL);

        add(connectButton);
        connectButton.addActionListener(new ConnectPressed());
        add(autoConnectToFavourites);

        favouritesScroller.setPreferredSize(new Dimension(200, 100));
        favouritesList.addMouseListener(new FavouritesPopClickListener());
        add(favouritesScroller);

        final int TOP_SPACING = 6;
        final int TOP_ALIGNED = 0;
        final int LEFT_ALIGNED = 0;
        final int RIGHT_ALIGNED = 0;
        final int LEFT_SPACING = 6;

        SpringLayout connectionLayout = (SpringLayout) this.getLayout();

        // Puts the connect button further below Channel
        connectionLayout.putConstraint(SpringLayout.NORTH, connectButton, TOP_SPACING * TOP_SPACING, SpringLayout.SOUTH,
                firstChannelTextField);
        connectionLayout.putConstraint(SpringLayout.WEST, connectButton, LEFT_ALIGNED, SpringLayout.WEST,
                firstChannelTextField);


        // Aligns the autoConnectToFavourites checkbox to the label of userNameTextField
        connectionLayout.putConstraint(SpringLayout.NORTH, autoConnectToFavourites, TOP_ALIGNED, SpringLayout.NORTH,
                URPanels.getLabelForComponent(this,userNameTextField));
        connectionLayout.putConstraint(SpringLayout.WEST, autoConnectToFavourites, LEFT_SPACING, SpringLayout.EAST,
                proxyTypeChoice);

        URPanels.addKeyAssociation(this, autoConnectToFavourites, Constants.KEY_AUTO_CONNECT_FAVOURITES);

        // Puts the Favourites box inline with the autoConnectToFavourites check box and the connect button
        connectionLayout.putConstraint(SpringLayout.NORTH, favouritesScroller, TOP_SPACING, SpringLayout.SOUTH,
                autoConnectToFavourites);
        connectionLayout.putConstraint(SpringLayout.WEST, favouritesScroller, LEFT_ALIGNED, SpringLayout.WEST,
                autoConnectToFavourites);
        connectionLayout.putConstraint(SpringLayout.EAST, favouritesScroller, LEFT_ALIGNED, SpringLayout.EAST,
                autoConnectToFavourites);
        connectionLayout.putConstraint(SpringLayout.SOUTH, favouritesScroller, TOP_SPACING, SpringLayout.SOUTH,
                connectButton);

        URProfilesUtil.addListener(EventType.CHANGE, e -> {
            loadFavouritesList();
        });
    }

    public void addFavourite (String favServer, String favChannel)
    {
        favouritesListModel.addElement(new FavouritesItem(favServer, favChannel));

        URProfilesUtil.getActiveFavouritesPath().node(favServer).node(favChannel).put("PORT", DriverGUI.gui.getCreatedServer(favServer).getPort());
    }

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

    public void loadFavouritesList ()
    {
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
            Constants.LOGGER.error(e.getLocalizedMessage());
        }
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

            URProfilesUtil.addListener(EventType.CHANGE, e -> {
                settingsPath = URProfilesUtil.getActiveFavouritesPath().node(favServer).node(favChannel);
            });

            createPopUp();
        }

        @Override
        public String toString ()
        {
            return favServer + ":" + favChannel;
        }

        public String getChannelName ()
        {
            return favChannel;
        }

        public void createPopUp ()
        {
            myMenu = new FavouritesPopUp();
        }

        private class FavouritesPopUp extends JPopupMenu
        {
            /**
             *
             */
            private final long serialVersionUID = -3599612559330380653L;
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
                    favFontDialog = new FontDialog(favChannel, DriverGUI.gui.getStyle(), settingsPath);
                    // favFontDialog.addSaveListener(new SaveChannelFontListener());
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
                        Constants.LOGGER.error(e.getLocalizedMessage());
                    }
                }
            }
        }
    }

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

        // Show the popup menu, update it's styling first.
        private void doPop (MouseEvent e)
        {
            SwingUtilities.updateComponentTreeUI(favouritesList.getSelectedValue().myMenu);
            favouritesList.getSelectedValue().myMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    class UCAuthTypeComboBoxChangeHandler implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent e)
        {
            authenticationTypeChoice.runChangeListener();

            if (authenticationTypeChoice.getSelectedItem().equals(CapabilityTypes.NONE.getType()))
            {
                passwordTextField.setVisible(false);
                rememberPassCheckBox.setVisible(false);

                URPanels.getLabelForComponent(ConnectionPanel.this, passwordTextField).setVisible(false);
                URPanels.getLabelForComponent(ConnectionPanel.this, rememberPassCheckBox).setVisible(false);
            } else
            {
                passwordTextField.setVisible(true);
                rememberPassCheckBox.setVisible(true);

                URPanels.getLabelForComponent(ConnectionPanel.this, passwordTextField).setVisible(true);
                URPanels.getLabelForComponent(ConnectionPanel.this, rememberPassCheckBox).setVisible(true);
            }
        }
    }

    // TODO: This should be a backend thing
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
            if (!authenticationTypeChoice.getSelectedItem().equals(CapabilityTypes.NONE.getType()) && passwordTextField.getPassword().length == 0 )
            {
                MessageDialog dialog = new MessageDialog(
                        "Password field is empty and is required for your chosen authentication method.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                dialog.setVisible(true);
            } else if (!proxyTypeChoice.getSelectedItem().equals(ProxyTypes.NONE.getType()) && (proxyHostNameTextField.getText().isBlank() ||
                proxyPortTextField.getText().isBlank()) )
            {
                MessageDialog dialog = new MessageDialog(
                    "Hostname or Port field is empty which is required for your chosen Proxy method.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
                dialog.setVisible(true);
            } else
            {
                // DriverGUI.gui.addToCreatedServers(servernameTextField.getText().trim());

                IRCServerBase newServer = new IRCServer(servernameTextField.getText().trim(), userNameTextField.getText().trim(),
                    realNameTextField.getText().trim(), new String(passwordTextField.getPassword()),
                    serverPortTextField.getText().trim(), serverTLSCheckBox.isSelected(),
                    proxyHostNameTextField.getText(), proxyPortTextField.getText(), proxyTypeChoice.getSelectedItem(), authenticationTypeChoice.getSelectedItem());

                // TODO: Revisit when considering adding support for multiple servers
                // if (autoConnectToFavourites.isSelected())
                // {
                //     FavouritesItem castItem;
                //     for (Object tempItem : favouritesListModel.toArray())
                //     {
                //         castItem = (FavouritesItem) tempItem;
                //         DriverGUI.gui.addToCreatedServers(castItem.favServer);
                //     }
                // }

                List<String> favouriteChannels = new ArrayList<>();
                if(autoConnectToFavourites.isSelected())
                    favouriteChannels = Collections.list(favouritesListModel.elements())
                        .stream()
                        .map(FavouritesItem::getChannelName)
                        .collect(Collectors.toList());

                if(!firstChannelTextField.getText().isBlank())
                    favouriteChannels.add(firstChannelTextField.getText());

                newServer.connect(favouriteChannels.stream().toArray(size -> new String[size]));

                // profilePicker.setEnabled(false);
            }
        }
    }
}
