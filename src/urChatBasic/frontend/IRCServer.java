package urChatBasic.frontend;

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
import urChatBasic.base.ConnectionBase;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCRoomBase;
import urChatBasic.base.IRCServerBase;
import urChatBasic.base.capabilities.CapTypeBase;
import urChatBasic.base.capabilities.CapabilityTypes;

public class IRCServer extends IRCRoomBase implements IRCServerBase
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
    private Boolean useSOCKS;

    // Created channels/tabs
    private List<IRCRoomBase> createdRooms = new ArrayList<IRCRoomBase>();

    // Server capabilities
    private ArrayList<CapabilityTypes> capabilities = new ArrayList<CapabilityTypes>();


    public IRCServer(String serverName, String nick, String login, String password, String portNumber, Boolean isTLS, String proxyHost,
            String proxyPort, Boolean useSOCKS)
    {
        super(serverName);
        setServer(this);
        resetLineFormatter();

        myMenu = new ServerPopUp();
        hideUsersList();
        hideEventTicker();

        port = portNumber;
        this.isTLS = isTLS;

        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.useSOCKS = useSOCKS;
        this.name = serverName;
        this.password = password;
        this.login = login;
        this.nick = nick;

        URL imgPath = null;
        try
        {
            imgPath =  new URL(Constants.RESOURCES_DIR + "Server.png");
            icon = new ImageIcon(imgPath);
        } catch (IOException e)
        {
            Constants.LOGGER.log(Level.SEVERE, "COULD NOT LOAD Server.png " + e.getLocalizedMessage());
        }
    }

    @Override
    public void saslRequestAuthentication()
    {
        sendClientText("CAP REQ sasl", getName());
    }

    @Override
    public void nickservRequestAuthentication()
    {
        if(!getPassword().isEmpty())
        {
            sendClientText("/msg nickserv identify "+getNick()+" "+getPassword(), getName());
        } else {
            sendClientText("/msg nickserv ACC",getName());
        }

    }

    @Override
    public void saslCompleteAuthentication()
    {
        sendClientText("CAP END", getName());
        gui.connectFavourites(this);
    }

    @Override
    public void saslDoAuthentication()
    {
        sendClientText("AUTHENTICATE PLAIN", getName());
    }

    @Override
    public void saslSendAuthentication()
    {
        String escapedDelim = Character.toString(0x0);
        String saslString = escapedDelim+getNick()+escapedDelim+getPassword();
        try
        {
            saslString = Base64.getEncoder().encodeToString(saslString.getBytes(StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sendClientText("AUTHENTICATE "+saslString, getName());
    }

    @Override
    public void setCapabilities (String capabilityMessage)
    {
        // example message: account-notify away-notify chghost extended-join multi-prefix sasl=PLAIN,ECDSA-NIST256P-CHALLENGE,EXTERNAL tls account-tag cap-notify echo-message server-time solanum.chat/identify-msg solanum.chat/oper solanum.chat/realhost
        String[] components = capabilityMessage.split(" ");

        for (String component : components) {
            for (CapabilityTypes capability : CapabilityTypes.values()) {
                if(capability.getType().matches(component))
                {
                    capabilities.add(capability);
                } else if(component.startsWith(capability.name().toLowerCase() + "="))
                {
                    capabilities.add(capability);
                    String[] subComponents = component.replace(capability.name().toLowerCase() + "=", "").split(",");

                    for (String subComponent : subComponents) {
                        for (CapTypeBase subType : capability.getType().availableSubTypes()) {
                            if(subType.matches(subComponent))
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
        for (CapabilityTypes capabilityType : capabilities) {
            if(capabilityType.equals(capability))
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
    public String getNick()
    {
        return nick;
    }

    @Override
    public void setNick(String newNick)
    {
        nick = newNick;
    }


    @Override
    public String getLogin()
    {
        return login;
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    @Override
    public boolean isConnected()
    {
        return serverConnection.isConnected();
    }

    @Override
    public String getPort()
    {
        return this.port;
    }

    @Override
    public Boolean usingSOCKS()
    {
        return useSOCKS;
    }

    @Override
    public Boolean usingTLS()
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
        JMenuItem chooseFont;

        public ServerPopUp()
        {
            nameItem = new JMenuItem(IRCServer.this.getName());
            add(nameItem);
            addSeparator();
            //
            quitItem = new JMenuItem("Quit");
            add(quitItem);
            quitItem.addActionListener(new QuitItem());
            //
            chooseFont = new JMenuItem("Show Font Dialog");
            add(chooseFont);
            chooseFont.addActionListener(new ChooseFont());
        }
    }



    private class QuitItem implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            if (IRCServer.this.isConnected())
            {
                System.out.println("send quit message");
                // Send the /quit message, which disconnects and remove the gui elements
                sendClientText("/quit Goodbye cruel world", getName());
            } else
            {
                // We aren't connected, so just remove the GUI elements
                gui.quitServer(IRCServer.this);
            }
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


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#serverConnect(java.lang.String, java.lang.String)
     * serverConnect(nick, login, portNumber, isTLS, proxyHost, proxyPort, useSOCKS,
     * Constants.BACKEND_CLASS);
     */
    @Override
    public void connect()
    {
        try
        {
            serverConnection = new Connection(this);
        } catch (Exception e)
        {
            Constants.LOGGER.log(Level.SEVERE, "Failed to create backend! " + e.getLocalizedMessage());
        }

        new Thread(serverConnection).start();
    }

    @Override
    public void disconnect()
    {
        serverConnection.disconnect();
        quitRooms();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#toString()
     */
    @Override
    public String toString()
    {
        return this.name;
    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#setName(java.lang.String)
     */
    @Override
    public void setName(String serverName)
    {
        this.name = serverName;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#getName()
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.IRCServerBase#getIRCUser(java.lang.String)
     */
    @Override
    public IRCUser getIRCUser(String userName)
    {
        for (IRCRoomBase tempChannel : createdRooms)
            if (tempChannel.getCreatedUser(userName) != null)
                return tempChannel.getCreatedUser(userName);
        return new IRCUser(this, userName);
    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#getCreatedPrivateRoom(java.lang.String)
     */
    @Override
    public IRCPrivate getCreatedPrivateRoom(String privateRoom)
    {
        IRCRoomBase tempRoom = getCreatedRoom(privateRoom, true);

        if(tempRoom != null)
        {
            return (IRCPrivate) tempRoom;
        }

        return null;
    }

        /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#getCreatedPrivateRoom(java.lang.String)
     */
    @Override
    public IRCChannel getCreatedChannel(String channelName)
    {
        IRCRoomBase tempRoom = getCreatedRoom(channelName, false);

        if(tempRoom != null)
        {
            return (IRCChannel) tempRoom;
        }

        return null;
    }

    @Override
    public void quitRooms()
    {
        Iterator<IRCRoomBase> channelIterator = createdRooms.iterator();
        while (channelIterator.hasNext())
        {
            IRCRoomBase removeChannel = channelIterator.next();
            channelIterator.remove();
            quitRoom(removeChannel);
        }
    }

    @Override
    public void quitRoom(IRCRoomBase ircRoom)
    {
        ircRoom.quitRoom();
        createdRooms.remove(ircRoom);

        boolean tabExists = Arrays.stream(gui.tabbedPane.getComponents()).anyMatch(room -> room.equals(gui.previousSelectedTab));

        if(tabExists)
            gui.tabbedPane.setSelectedComponent(gui.previousSelectedTab);

        gui.tabbedPane.remove(ircRoom);
    }

    @Override
    public IRCRoomBase getCreatedRoom(String roomName, boolean asPrivate)
    {
        for (IRCRoomBase tempChannel : createdRooms)
            if ((tempChannel instanceof IRCPrivate && asPrivate) || tempChannel.getName().equals(roomName))
                return tempChannel;
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#addToCreatedChannels(java.lang.String)
     */
    @Override
    public void addToCreatedRooms(String roomName, boolean asPrivate)
    {
        if (getCreatedRoom(roomName, asPrivate) == null)
        {
            IRCRoomBase tempChannel = asPrivate ? new IRCPrivate(this, getIRCUser(roomName)) : new IRCChannel(this, roomName);
            createdRooms.add(tempChannel);

            gui.tabbedPane.insertTab(roomName, tempChannel.icon, tempChannel, null, gui.tabbedPane.indexOfComponent(gui.currentSelectedTab) + 1);
            //gui.tabbedPane.addTab(roomName, tempChannel.icon, tempChannel);
            Component currentTab = gui.tabbedPane.getSelectedComponent();
            if(currentTab instanceof IRCRoomBase)
            {
                if(!((IRCRoomBase) currentTab).userIsTyping())
                {
                    gui.tabbedPane.setSelectedIndex(gui.tabbedPane.indexOfComponent(tempChannel));
                    tempChannel.getUserTextBox().requestFocus();
                } else {
                    tempChannel.callForAttention();
                }
            } else if(currentTab instanceof IRCServer) {
                if(clientTextBox.getText().isEmpty())
                {
                    gui.tabbedPane.setSelectedIndex(gui.tabbedPane.indexOfComponent(tempChannel));
                    tempChannel.getUserTextBox().requestFocus();
                } else {
                    tempChannel.callForAttention();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#addToPrivateRooms(urChatBasic.frontend.IRCUser)
     */
    @Override
    public IRCPrivate addToPrivateRooms(IRCUser fromUser)
    {
        IRCPrivate privateRoom = getCreatedPrivateRoom(fromUser.getName());
        if (privateRoom == null)
        {
            addToCreatedRooms(fromUser.getName(), true);
            privateRoom = getCreatedPrivateRoom(fromUser.getName());
            // gui.tabbedPane.addTab(privateRoom.getName(), privateRoom.icon, privateRoom);
            // gui.tabbedPane.setSelectedIndex(gui.tabbedPane.indexOfComponent(privateRoom));
            // privateRoom.getUserTextBox().requestFocus();
            return privateRoom;
        }

        return privateRoom;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#printChannelText(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void printChannelText(String channelName, String line, String fromUser)
    {

        IRCRoomBase tempChannel = getCreatedRoom(channelName, false);

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
     * @see urChatBasic.backend.IRCServerBase#printPrivateText(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void printPrivateText(String userName, String line, String fromUser)
    {
        // private messages aren't linked to a channel, so create it - also
        // if they aren't muted
        if (getIRCUser(userName) != null && !getIRCUser(userName).isMuted())
        {
            IRCPrivate privateRoom = addToPrivateRooms(getIRCUser(userName));

            privateRoom.printText(line, fromUser);
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
    public void printServerText(String line)
    {
        printText(line, Constants.EVENT_USER);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#printEventTicker(java.lang.String, java.lang.String)
     */
    @Override
    public void printEventTicker(String channelName, String eventText)
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
    public void addToUsersList(final String channelName, final String[] users)
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
    public void addToUsersList(final String channelName, final String user)
    {
        addToUsersList(channelName, new String[]{user});
    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#removeFromUsersList(java.lang.String, java.lang.String)
     */
    @Override
    public void removeFromUsersList(final String channelName, final String user)
    {
        String thisUser = user;
        if (user.startsWith(":"))
            thisUser = user.substring(1);

        if (channelName.equals(getName()))
        {
            for (IRCRoomBase tempChannel : createdRooms)
            {
                tempChannel.removeFromUsersList(tempChannel.getName(), thisUser);
            }
        } else
        {
            IRCChannel tempChannel = getCreatedChannel(channelName);
            if (tempChannel != null)
                if (thisUser.equals(getNick()))
                    quitRoom(tempChannel);
                else
                    tempChannel.removeFromUsersList(channelName, thisUser);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#setChannelTopic(java.lang.String, java.lang.String)
     */
    @Override
    public void setChannelTopic(String channelName, String channelTopic)
    {
        getCreatedChannel(channelName).setChannelTopic(channelTopic);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#sendClientText(java.lang.String, java.lang.String)
     */
    @Override
    public void sendClientText(String line, String source)
    {
        try
        {
            if (serverConnection.isConnected())
            {
                serverConnection.sendClientText(line, source);
            }
        } catch (IOException e)
        {
            Constants.LOGGER.log(Level.WARNING, "Couldn't send text! " + e.getLocalizedMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#renameUser(java.lang.String, java.lang.String)
     */
    @Override
    public void renameUser(final String oldUserName, final String newUserName)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                for (IRCRoomBase tempChannel : createdRooms)
                {
                    tempChannel.renameUser(oldUserName.replace(":", ""), newUserName);
                }
            }
        });
    }
}
