package urChatBasic.frontend;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serial;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import urChatBasic.backend.Connection;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.ConnectionBase;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCChannelBase;
import urChatBasic.base.IRCServerBase;
import urChatBasic.base.Constants.EventType;
import urChatBasic.base.capabilities.CapTypeBase;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.base.proxy.ProxyTypeBase;
import urChatBasic.frontend.utils.URPanels;

public class IRCServer extends IRCChannelBase implements IRCServerBase
{
    /**
     *
     */
    private static final long serialVersionUID = -4685985875752613136L;
    // Connection Properties
    // TODO: Should remove the connection stuff from here into Connection instead of being in IRCServer?
    // Should also probably be called IRCNetwork?
    private ConnectionBase serverConnection = null;

    private String name;
    private String password;
    private String port;
    private String nick;
    private String login;
    private Boolean isTLS;
    private String proxyHost;
    private String proxyPort;
    private ProxyTypeBase proxyType;
    private CapTypeBase authentication;

    // Created channels/tabs
    public List<IRCChannelBase> createdChannels = new ArrayList<IRCChannelBase>();

    // Server capabilities
    private ArrayList<CapabilityTypes> capabilities = new ArrayList<CapabilityTypes>();


    public IRCServer (String serverName, String nick, String login, String password, String portNumber, Boolean isTLS, String proxyHost, String proxyPort,
            ProxyTypeBase proxyType, CapTypeBase authentication)
    {
        super(serverName);
        setServer(this);

        myMenu = new ServerPopUp();
        hideUsersList();
        hideEventTicker();

        port = portNumber;
        this.isTLS = isTLS;

        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyType = proxyType;
        this.name = serverName;
        this.password = password;
        this.login = login;
        this.nick = nick;
        this.authentication = authentication;

        URL imgPath = null;
        try
        {
            imgPath = new URL(Constants.IMAGES_DIR + "Server.png");
            icon = new ImageIcon(imgPath);
        } catch (IOException e)
        {
            Constants.LOGGER.warn("COULD NOT LOAD Server.png " + e.getLocalizedMessage());
        }
    }

    @Override
    public void saslRequestAuthentication ()
    {
        sendClientText("CAP REQ sasl", getName());
    }

    @Override
    public void nickservRequestAuthentication ()
    {
        if (!getPassword().isEmpty())
        {
            sendClientText("/msg nickserv identify " + getNick() + " " + getPassword(), getName());
        } else
        {
            sendClientText("/msg nickserv ACC", getName());
        }

    }

    @Override
    public void saslCompleteAuthentication ()
    {
        sendClientText("CAP END", getName());
        reconnectChannels();
    }

    @Override
    public void saslDoAuthentication ()
    {
        sendClientText("AUTHENTICATE PLAIN", getName());
    }

    public void reconnectChannels ()
    {
        for (IRCChannelBase channel : createdChannels)
        {
            sendClientText("/join " + channel.getName(), getServer().getName());
        }
    }

    @Override
    public void saslSendAuthentication ()
    {
        String escapedDelim = Character.toString(0x0);
        String saslString = escapedDelim + getNick() + escapedDelim + getPassword();
        try
        {
            saslString = Base64.getEncoder().encodeToString(saslString.getBytes(StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e)
        {
            Constants.LOGGER.warn(e.getLocalizedMessage(), e);
        }
        sendClientText("AUTHENTICATE " + saslString, getName());
    }

    @Override
    public void setCapabilities (String capabilityMessage)
    {
        // example message: account-notify away-notify chghost extended-join multi-prefix sasl=PLAIN,ECDSA-NIST256P-CHALLENGE,EXTERNAL tls account-tag
        // cap-notify echo-message server-time solanum.chat/identify-msg solanum.chat/oper solanum.chat/realhost
        String[] components = capabilityMessage.split(" ");

        for (String component : components)
        {
            for (CapabilityTypes capability : CapabilityTypes.values())
            {
                if (capability.getType().matches(component))
                {
                    capabilities.add(capability);
                } else if (component.startsWith(capability.name().toLowerCase() + "="))
                {
                    capabilities.add(capability);
                    String[] subComponents = component.replace(capability.name().toLowerCase() + "=", "").split(",");

                    for (String subComponent : subComponents)
                    {
                        for (CapTypeBase subType : capability.getType().availableSubTypes())
                        {
                            if (subType.matches(subComponent))
                                capability.getType().addSubtype(subType);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean hasCapability (CapabilityTypes capability)
    {
        for (CapabilityTypes capabilityType : capabilities)
        {
            if (capabilityType.equals(capability))
            {
                return true;
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#getNick()
     */
    @Override
    public String getNick ()
    {
        return nick;
    }

    @Override
    public void setNick (String newNick)
    {
        nick = newNick;
    }


    @Override
    public String getLogin ()
    {
        return login;
    }

    @Override
    public String getPassword ()
    {
        return password;
    }

    @Override
    public CapTypeBase getAuthentication ()
    {
        return authentication;
    }

    @Override
    public boolean isConnected ()
    {
        return hasConnection() && serverConnection.isConnected();
    }

    @Override
    public boolean hasConnection ()
    {
        return serverConnection != null;
    }

    @Override
    public String getPort ()
    {
        return this.port;
    }

    @Override
    public ProxyTypeBase usingProxy ()
    {
        return proxyType;
    }

    @Override
    public Boolean usingTLS ()
    {
        return isTLS;
    }

    @Override
    public String getProxyHost ()
    {
        return proxyHost;
    }

    @Override
    public String getProxyPort ()
    {
        return proxyPort;
    }

    /**
     * Server doesn't need the User List, so don't set it up.
     */
    @Override
    protected void setupMainPanel ()
    {
        mainPanel.setLayout(new BorderLayout());
        setupMainTextArea();
        mainPanel.add(channelScroll, BorderLayout.CENTER);
        setupBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    // The server tab right-click menu
    class ServerPopUp extends JPopupMenu
    {
        /**
         *
         */
        @Serial
        private static final long serialVersionUID = 640768684923757684L;

        JMenuItem nameItem;
        JMenuItem quitItem;
        JMenuItem reconnectItem;
        JMenuItem chooseFont;

        public ServerPopUp ()
        {
            nameItem = new JMenuItem(IRCServer.this.getName());
            add(nameItem);
            addSeparator();
            //
            quitItem = new JMenuItem("Quit");
            add(quitItem);
            quitItem.addActionListener(new QuitItem());

            reconnectItem = new JMenuItem("Reconnect");
            add(reconnectItem);
            reconnectItem.addActionListener(new ReconnectServerItem());

            //
            chooseFont = new JMenuItem("Show Font Dialog");
            add(chooseFont);
            chooseFont.addActionListener(new ChooseFont());
        }
    }

    @Override
    public void setPingReceived ()
    {
        serverConnection.setPingReceived();
    }

    private class QuitItem implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            if (IRCServer.this.isConnected())
            {
                Constants.LOGGER.info("send quit message");
                // Send the /quit message, which disconnects and remove the gui elements
                sendClientText("/quit Goodbye cruel world", getName());
            } else
            {
                // We aren't connected, so just remove the GUI elements
                gui.quitServer(IRCServer.this);
            }
        }
    }

    private class ReconnectServerItem implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            IRCServer.this.reconnect();
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


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#serverConnect(java.lang.String, java.lang.String) serverConnect(nick, login, portNumber, isTLS, proxyHost,
     * proxyPort, useSOCKS, Constants.BACKEND_CLASS);
     */
    @Override
    public void connect (String[] autoConnectChannels)
    {
        try
        {
            serverConnection = new Connection(this);

            for (String autoChannel : autoConnectChannels)
            {
                IRCChannelBase newChannel = new IRCChannel(this, autoChannel);
                createdChannels.add(newChannel);
            }

        } catch (Exception e)
        {
            Constants.LOGGER.error("Failed to create backend! " + e.getLocalizedMessage());
        }

        new Thread(serverConnection).start();
    }

    public void reconnect ()
    {
        serverConnection.reconnect();
    }

    @Override
    public void disconnect ()
    {
        serverConnection.disconnect();
        quitChannels();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#toString()
     */
    @Override
    public String toString ()
    {
        return this.name;
    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#setName(java.lang.String)
     */
    @Override
    public void setName (String serverName)
    {
        this.name = serverName;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#getName()
     */
    @Override
    public String getName ()
    {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.IRCServerBase#getIRCUser(java.lang.String)
     */
    @Override
    public IRCUser getIRCUser (String userName)
    {
        for (IRCChannelBase tempChannel : createdChannels)
            if (tempChannel.getCreatedUser(userName) != null)
                return tempChannel.getCreatedUser(userName);
        return new IRCUser(this, userName);
    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#getCreatedPrivateChannel(java.lang.String)
     */
    @Override
    public IRCPrivate getCreatedPrivateChannel (String privateChannel)
    {
        IRCChannelBase tempChannel = getCreatedChannel(privateChannel, true);

        if (tempChannel != null)
        {
            return (IRCPrivate) tempChannel;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#getCreatedPrivateChannel(java.lang.String)
     */
    @Override
    public IRCChannel getCreatedChannel (String channelName)
    {
        IRCChannelBase tempChannel = getCreatedChannel(channelName, false);

        if (tempChannel != null && tempChannel instanceof IRCChannel)
        {
            return (IRCChannel) tempChannel;
        }

        return null;
    }

    @Override
    public void quitChannels ()
    {
        URProfilesUtil.removeListener(EventType.CHANGE, changeListener);

        Iterator<IRCChannelBase> channelIterator = createdChannels.iterator();
        while (channelIterator.hasNext())
        {
            IRCChannelBase removeChannel = channelIterator.next();
            channelIterator.remove();
            quitChannel(removeChannel);
        }
    }

    @Override
    public void quitChannel (IRCChannelBase ircChannel)
    {
        ircChannel.closeChannel();
        createdChannels.remove(ircChannel);

        boolean tabExists = Arrays.stream(gui.tabbedPane.getComponents()).anyMatch(channel -> channel.equals(ircChannel));

        try
        {
            if (tabExists && gui.tabbedPane.getSelectedComponent().equals(ircChannel))
                gui.tabbedPane.setSelectedComponent(gui.previousSelectedTab);
        } catch (IllegalArgumentException iae)
        {
            Constants.LOGGER.debug("Previous Selected tab doesn't exist, unable to revert selection");
        }

        if (tabExists)
            gui.tabbedPane.remove(ircChannel);
    }

    @Override
    public IRCChannelBase getCreatedChannel (String channelName, boolean asPrivate)
    {
        IRCChannelBase returnChannel = null;

        for (IRCChannelBase tempChannel : createdChannels)
            if (tempChannel.getName().equalsIgnoreCase(channelName))
            {
                if (asPrivate && tempChannel instanceof IRCPrivate || !asPrivate)
                    returnChannel = tempChannel;
            }
        return returnChannel;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#addToCreatedChannels(java.lang.String)
     */
    @Override
    public void addToCreatedChannels (String channelName, boolean asPrivate)
    {

        if (getCreatedChannel(channelName, asPrivate) == null)
        {
            createdChannels.add(asPrivate ? new IRCPrivate(this, getIRCUser(channelName)) : new IRCChannel(this, channelName));
        }

        if (getCreatedChannel(channelName, asPrivate) == null || DriverGUI.gui.getTabIndex(getCreatedChannel(channelName, asPrivate)) < 0)
        {
            boolean iconsShown = (boolean) URPanels.getKeyComponentValue(Constants.KEY_SHOW_TAB_ICON);


            SwingUtilities.invokeLater(new Runnable()
            {

                @Override
                public void run ()
                {
                    IRCChannelBase tempChannel = getCreatedChannel(channelName, asPrivate);
                    int newIndex = gui.tabbedPane.indexOfComponent(gui.currentSelectedTab) + 1;
                    gui.tabbedPane.insertTab(channelName, iconsShown ? tempChannel.icon : null, tempChannel, null,
                            gui.tabbedPane.indexOfComponent(gui.currentSelectedTab) + 1);

                    // gui.tabbedPane.addTab(channelName, tempChannel.icon, tempChannel);
                    Component currentTab = gui.tabbedPane.getSelectedComponent();
                    if (currentTab instanceof IRCChannelBase)
                    {
                        if (!((IRCChannelBase) currentTab).userIsTyping())
                        {
                            gui.tabbedPane.setSelectedIndex(newIndex);
                            tempChannel.getUserTextBox().requestFocus();
                        } else
                        {
                            tempChannel.callForAttention();
                        }
                    } else if (currentTab instanceof IRCServer)
                    {
                        if (clientTextBox.getText().isEmpty())
                        {
                            gui.tabbedPane.setSelectedIndex(newIndex);
                            tempChannel.getUserTextBox().requestFocus();
                        } else
                        {
                            tempChannel.callForAttention();
                        }
                    }
                }
            });
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#addToPrivateChannels(urChatBasic.frontend.IRCUser)
     */
    @Override
    public IRCPrivate addToPrivateChannels (IRCUser fromUser)
    {
        IRCPrivate privateChannel = getCreatedPrivateChannel(fromUser.getName());
        if (privateChannel == null)
        {
            addToCreatedChannels(fromUser.getName(), true);
            privateChannel = getCreatedPrivateChannel(fromUser.getName());
            // gui.tabbedPane.addTab(privateChannel.getName(), privateChannel.icon, privateChannel);
            // gui.tabbedPane.setSelectedIndex(gui.tabbedPane.indexOfComponent(privateChannel));
            // privateChannel.getUserTextBox().requestFocus();
            return privateChannel;
        }

        return privateChannel;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#printChannelText(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void printChannelText (String channelName, String line, String fromUser)
    {

        IRCChannelBase tempChannel = getCreatedChannel(channelName, false);

        if (channelName.equals(fromUser) || null == tempChannel)
        {
            printPrivateText(channelName, line, fromUser);
        } else
        {
            tempChannel.printText(line, fromUser);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#printPrivateText(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void printPrivateText (String userName, String line, String fromUser)
    {
        // private messages aren't linked to a channel, so create it - also
        // if they aren't muted
        if (getIRCUser(userName) != null && !getIRCUser(userName).isMuted())
        {
            IRCPrivate privateChannel = addToPrivateChannels(getIRCUser(userName));

            privateChannel.printText(line, fromUser);
            // Make a noise if the user hasn't got the current tab selected
            // TODO: Make it work on linux, and also add a focus request
            if (gui.getTabIndex(userName) != gui.tabbedPane.getSelectedIndex())
            {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#printServerText(java.lang.String)
     */
    @Override
    public void printServerText (String line)
    {
        printText(line, Constants.EVENT_USER);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#printEventTicker(java.lang.String, java.lang.String)
     */
    @Override
    public void printEventTicker (String channelName, String eventText)
    {
        getCreatedChannel(channelName).createEvent(eventText);
    }


    // Adds users to the list in the users array[]
    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#addToUsersList(java.lang.String, java.lang.String[])
     */
    @Override
    public void addToUsersList (final String channelName, final String[] users)
    {
        if (!channelName.matches("Server"))
        {
            IRCChannel tempChannel = getCreatedChannel(channelName);
            if (tempChannel != null)
                tempChannel.addToUsersList(users);
        }
    }

    // Adds a single user, good for when a user joins the channel
    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#addToUsersList(java.lang.String, java.lang.String)
     */
    @Override
    public void addToUsersList (final String channelName, final String user)
    {
        addToUsersList(channelName, new String[] {user});
    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#removeFromUsersList(java.lang.String, java.lang.String)
     */
    @Override
    public void removeFromUsersList (final String channelName, final String user)
    {
        String thisUser = user;
        if (user.startsWith(":"))
            thisUser = user.substring(1);

        if (channelName.equals(getName()))
        {
            for (IRCChannelBase tempChannel : createdChannels)
            {
                tempChannel.removeFromUsersList(thisUser);
            }
        } else
        {
            IRCChannel tempChannel = getCreatedChannel(channelName);
            if (tempChannel != null)
                if (thisUser.equals(getNick()))
                    quitChannel(tempChannel);
                else
                    tempChannel.removeFromUsersList(thisUser);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#setChannelTopic(java.lang.String, java.lang.String)
     */
    @Override
    public void setChannelTopic (String channelName, String channelTopic)
    {
        getCreatedChannel(channelName).setChannelTopic(channelTopic);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#sendClientText(java.lang.String, java.lang.String)
     */
    @Override
    public void sendClientText (String line, String source)
    {
        try
        {
            if (serverConnection.isConnected())
            {
                serverConnection.sendClientText(line, source);
            }
        } catch (IOException e)
        {
            Constants.LOGGER.error("Couldn't send text! " + e.getLocalizedMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#renameUser(java.lang.String, java.lang.String)
     */
    @Override
    public void renameUser (final String oldUserName, final String newUserName)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run ()
            {
                for (IRCChannelBase tempChannel : createdChannels)
                {
                    tempChannel.renameUser(oldUserName.replace(":", ""), newUserName);
                }
            }
        });
    }
}
