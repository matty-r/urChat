package urChatBasic.frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import urChatBasic.backend.Connection;
import urChatBasic.base.ConnectionBase;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCServerBase;

public class IRCServer extends JPanel implements IRCServerBase
{
    /**
     *
     */
    private static final long serialVersionUID = -4685985875752613136L;
    ////////////////
    // GUI ELEMENTS//
    ////////////////
    protected UserGUI gui = DriverGUI.gui;

    // Icons
    public ImageIcon icon;

    private FontPanel fontPanel;


    // Connection Properties
    // TODO: Should remove the connection stuff from here into Connection instead of being in IRCServer?
    // Should also probably be called IRCNetwork?
    private ConnectionBase serverConnection = null;

    // Server Text Area
    private JTextPane serverTextArea = new JTextPane();
    private JScrollPane serverTextScroll = new JScrollPane(serverTextArea);
    public JTextField serverTextBox = new JTextField(); //userTextBox
    private String name;
    private String port;
    private String nick;
    private String login;

    private Boolean isTLS;

    private String proxyHost;
    private String proxyPort;
    private Boolean useSOCKS;

    public ServerPopUp myMenu = new ServerPopUp();

    // Created Private Rooms/Tabs
    private List<IRCPrivate> createdPrivateRooms = new ArrayList<IRCPrivate>();
    // Created channels/tabs
    private List<IRCChannel> createdChannels = new ArrayList<IRCChannel>();


    public IRCServer(String serverName, String nick, String login, String portNumber, Boolean isTLS, String proxyHost,
            String proxyPort, Boolean useSOCKS)
    {
        this.setLayout(new BorderLayout());
        port = portNumber;
        this.isTLS = isTLS;

        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.useSOCKS = useSOCKS;
        this.name = serverName;
        this.login = login;
        this.nick = nick;

        Image tempIcon = null;
        try
        {
            tempIcon = ImageIO.read(new File(Constants.RESOURCES_DIR + "Server.png"));
            icon = new ImageIcon(tempIcon);
        } catch (IOException e)
        {
            Constants.LOGGER.log(Level.SEVERE, "COULD NOT LOAD Server.png " + e.getLocalizedMessage());
        }

        this.add(serverTextScroll, BorderLayout.CENTER);
        this.add(serverTextBox, BorderLayout.PAGE_END);
        serverTextArea.setEditable(false);
        serverTextBox.addActionListener(new SendServerText());
        serverTextArea.setFont(gui.getFont());
        fontPanel = new FontPanel(this);
        this.add(fontPanel, BorderLayout.NORTH);
        fontPanel.setVisible(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#getNick()
     */
    @Override
    public String getNick()
    {
        return serverConnection.getNick();
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
            chooseFont = new JMenuItem("Toggle Font chooser");
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
                // Send the /quit message, which disconnects and remove the gui elements
                sendClientText("/quit Goodbye cruel world", IRCServer.this.getName());
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
            fontPanel.setVisible(!fontPanel.isVisible());
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
            serverConnection = new Connection(this, nick, login, port, isTLS, proxyHost, proxyPort, useSOCKS, gui);
        } catch (Exception e)
        {
            Constants.LOGGER.log(Level.SEVERE, "Failed to create backend! " + e.getLocalizedMessage());
        }

        new Thread(serverConnection).start();
    }

    @Override
    public void disconnect()
    {
        quitChannels();
        quitPrivateRooms();
        serverConnection.disconnect();
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
     * @see urChatBasic.backend.IRCServerBase#isCreatedChannelsEmpty()
     */
    @Override
    public Boolean isCreatedChannelsEmpty()
    {
        return createdChannels.isEmpty();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.IRCServerBase#getIRCUser(java.lang.String)
     */
    @Override
    public IRCUser getIRCUser(String userName)
    {
        for (IRCChannel tempChannel : createdChannels)
            if (tempChannel.getCreatedUsers(userName) != null)
                return tempChannel.getCreatedUsers(userName);
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
        for (IRCPrivate tempPrivate : createdPrivateRooms)
        {
            if (tempPrivate.getName().equalsIgnoreCase(privateRoom))
                return tempPrivate;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#quitChannels()
     */
    @Override
    public void quitChannels()
    {
        while (createdChannels.iterator().hasNext())
        {
            IRCChannel tempChannel = createdChannels.iterator().next();
            createdChannels.remove(tempChannel);
            gui.tabbedPane.remove(tempChannel);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#quitChannel(java.lang.String)
     */
    @Override
    public void quitChannel(String channelName)
    {
        if (getCreatedChannel(channelName) != null)
        {
            createdChannels.remove(getCreatedChannel(channelName));
            gui.tabbedPane.remove(gui.getTabIndex(channelName));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#quitPrivateRooms()
     */
    @Override
    public void quitPrivateRooms()
    {
        while (createdPrivateRooms.iterator().hasNext())
        {
            IRCPrivate tempPrivateRoom = createdPrivateRooms.iterator().next();
            gui.tabbedPane.remove(tempPrivateRoom);
            createdPrivateRooms.remove(tempPrivateRoom);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#quitPrivateRooms(java.lang.String)
     */
    @Override
    public void quitPrivateRooms(String roomName)
    {
        if (getCreatedPrivateRoom(roomName) != null)
        {
            createdPrivateRooms.remove(getCreatedPrivateRoom(roomName));
            gui.tabbedPane.remove(gui.getTabIndex(roomName));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#getCreatedChannel(java.lang.String)
     */
    @Override
    public IRCChannel getCreatedChannel(String channelName)
    {
        for (IRCChannel tempChannel : createdChannels)
            if (tempChannel.getName().equals(channelName))
                return tempChannel;
        return null;
    }


    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#addToCreatedChannels(java.lang.String)
     */
    @Override
    public void addToCreatedChannels(String channelName)
    {
        if (getCreatedChannel(channelName) == null)
        {
            IRCChannel tempChannel = new IRCChannel(this, channelName);
            createdChannels.add(tempChannel);
            gui.tabbedPane.addTab(channelName, tempChannel.icon, tempChannel);
            gui.tabbedPane.setSelectedIndex(gui.tabbedPane.indexOfComponent(tempChannel));
            tempChannel.getUserTextBox().requestFocus();
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
            privateRoom = new IRCPrivate(this, fromUser);
            createdPrivateRooms.add(privateRoom);
            gui.tabbedPane.addTab(privateRoom.getName(), privateRoom.icon, privateRoom);
            gui.tabbedPane.setSelectedIndex(gui.tabbedPane.indexOfComponent(privateRoom));
            privateRoom.getUserTextBox().requestFocus();
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
        if (channelName.equals(fromUser))
        {
            printPrivateText(channelName, line, fromUser);
        } else
        {
            getCreatedChannel(channelName).printText(line, fromUser);
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
        this.printText(line);
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
                tempChannel.addToUsersList(tempChannel.getName(), users);
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
        String thisUser = user;
        if (user.startsWith(":"))
            thisUser = user.substring(1);

        IRCChannel tempChannel = getCreatedChannel(channelName);
        if (tempChannel != null)
            tempChannel.addToUsersList(tempChannel.getName(), thisUser);
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

        if (channelName.equals("Server"))
        {
            for (IRCChannel tempChannel : createdChannels)
            {
                tempChannel.removeFromUsersList(tempChannel.getName(), thisUser);
            }
        } else
        {
            IRCChannel tempChannel = getCreatedChannel(channelName);
            if (tempChannel != null)
                if (thisUser.equals(serverConnection.getNick()))
                    quitChannel(channelName);
                else
                    tempChannel.removeFromUsersList(channelName, thisUser);
        }
    }


    private class SendServerText implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            try
            {
                if (!serverTextBox.getText().trim().isEmpty())
                    serverConnection.sendClientText(serverTextBox.getText(), getName());
            } catch (IOException e)
            {
                Constants.LOGGER.log(Level.WARNING, "Failed to send server text! " + e.getMessage());
            }
            serverTextBox.setText("");
        }
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
     * @see urChatBasic.backend.IRCServerBase#doLimitLines()
     */
    @Override
    public void doLimitLines()
    {
        if (gui.isLimitedServerActivity())
        {
            String[] tempText = serverTextArea.getText().split("\n");
            int linesCount = tempText.length;

            if (linesCount >= gui.getLimitServerLinesCount())
            {
                String newText = serverTextArea.getText().replace(tempText[0] + "\n", "");
                serverTextArea.setText(newText);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#printText(java.lang.Boolean, java.lang.String)
     */
    @Override
    public void printText(String line)
    {
        doLimitLines();

        StyledDocument doc = (StyledDocument) serverTextArea.getDocument();

        DateFormat chatDateFormat = new SimpleDateFormat("HHmm");
        Date chatDate = new Date();

        String timeLine = "";
        if (gui.isTimeStampsEnabled())
            timeLine = "[" + chatDateFormat.format(chatDate) + "]";


        new LineFormatter(gui.getFont(), getNick()).formattedDocument(doc, timeLine, "", line);

        serverTextArea.setCaretPosition(serverTextArea.getDocument().getLength());
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.backend.IRCServerBase#getChannelTopic(java.lang.String)
     */
    @Override
    public String getChannelTopic(String channelName)
    {
        return getCreatedChannel(channelName).getChannelTopic();
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
     * @see urChatBasic.backend.IRCServerBase#renameUser(java.lang.String, java.lang.String)
     */
    @Override
    public void renameUser(final String oldUserName, final String newUserName)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                for (IRCChannel tempChannel : createdChannels)
                {
                    tempChannel.renameUser(oldUserName.replace(":", ""), newUserName);
                }
            }
        });
    }

    @Override
    public String getServer()
    {
        return this.name;
    }
}
