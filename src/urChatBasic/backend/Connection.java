package urChatBasic.backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import urChatBasic.backend.MessageHandler.Message;
import urChatBasic.base.ConnectionBase;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCServerBase;
import urChatBasic.base.DialogBase;
import urChatBasic.base.UserGUIBase;
import urChatBasic.frontend.dialogs.MessageDialog;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;

public class Connection implements ConnectionBase
{
    private BufferedWriter writer;

    private boolean shutdown = false;
    // private String creationTime = (new Date()).toString();

    private IRCServerBase server;
    private String myNick;
    private String login;
    private boolean isTLS;
    private String proxyHost;
    private String proxyPort;
    private boolean useSOCKS;
    private String portNumber;
    private Socket mySocket;

    private MessageHandler messageHandler;
    public UserGUIBase gui;

    // Used for Logging messages received by the server
    // Debug Mode
    // Currently deprecated
    // private DateFormat debugDateFormat = new SimpleDateFormat("ddMMyyyy");
    // private DateFormat debugTimeFormat = new SimpleDateFormat("HHmm");
    // private Date todayDate = new Date();
    // private String debugFile;

    public Connection(IRCServerBase server, String nick, String login, String portNumber, Boolean isTLS,
            String proxyHost, String proxyPort, Boolean useSOCKS, UserGUIBase ugb)
    {
        this.gui = ugb;
        this.server = server;
        this.myNick = nick;
        this.isTLS = isTLS;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.useSOCKS = useSOCKS;
        if (portNumber.trim().equals(""))
            this.portNumber = Constants.DEFAULT_FIRST_PORT;
        this.portNumber = portNumber;
        this.login = login;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.ConnectionBase#isConnected()
     */
    @Override
    public Boolean isConnected()
    {
        return (mySocket != null) && (server != null) && (mySocket.isConnected())
                && (gui.getTabIndex(this.server.getName()) > -1);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.ConnectionBase#getServer()
     */
    @Override
    public IRCServerBase getServer()
    {
        return this.server;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.ConnectionBase#getServer()
     */
    @Override
    public String getPortNumber()
    {
        return this.portNumber;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.ConnectionBase#getServer()
     */
    @Override
    public String getLogin()
    {
        return this.login;
    }

    @Override
    public boolean usingTLS()
    {
        return isTLS;
    }

    @Override
    public boolean usingSOCKS()
    {
        return useSOCKS;
    }

    private void startUp() throws IOException
    {
        BufferedReader reader;

        messageHandler = new MessageHandler(server);
        localMessage("Attempting to connect to " + server);

        // Determine the socket type to be used
        if (usingSOCKS())
        {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
            Socket proxySocket = new Socket(proxy);
            InetSocketAddress address = new InetSocketAddress(server.getName(), Integer.parseInt(getPortNumber()));

            if (usingTLS())
            {
                proxySocket.connect(address, 5000);

                SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                mySocket = sslsocketfactory.createSocket(proxySocket, address.getHostName(), address.getPort(), true);
            } else
            {
                proxySocket.connect(address, 5000);
                mySocket = proxySocket;
            }
        } else
        {
            if (usingTLS())
            {
                SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                mySocket = sslsocketfactory.createSocket(server.getName(), Integer.parseInt(getPortNumber()));
            } else
            {
                mySocket = new Socket(server.getName(), Integer.parseInt(getPortNumber()));
            }
        }

        writer = new BufferedWriter(new OutputStreamWriter(mySocket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(mySocket.getInputStream(), StandardCharsets.UTF_8));

        // if we got this far, we established a connection to the server
        gui.setupServerTab(server);

        localMessage("Connected to " + getServer());

        String line = null;

        // Initiate connection to the server.
        writer.write("CAP LS 302\r\n");
        writer.write("NICK " + getNick() + "\r\n");
        writer.write("USER " + login + " 8 * : " + getLogin() + "\r\n");
        localMessage("Connect with nick " + getNick());
        writer.flush();

        while ((line = reader.readLine()) != null && !shutdown)
        {
            if (line.toLowerCase().startsWith("ping"))
            {
                // We must respond to PINGs to avoid being disconnected.
                writer.write("PONG " + line.substring(line.indexOf(':') + 1) + "\r\n");
                writer.flush();
            } else
            {
                serverMessage(messageHandler.new Message(messageHandler, line));
            }
        }

        if (shutdown)
        {
            try
            {
                writer.close();
                reader.close();
                mySocket.close();
            } catch (IOException e)
            {
                Constants.LOGGER.log(Level.SEVERE, "Error stopping connected.. " + e.getLocalizedMessage());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.ConnectionBase#setNick(java.lang.String)
     */
    @Override
    public void setNick(String newNick)
    {
        myNick = newNick;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.ConnectionBase#getNick()
     */
    @Override
    public String getNick()
    {
        return myNick;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.ConnectionBase#sendClientText(java.lang.String, java.lang.String)
     */
    @Override
    public void sendClientText(String clientText, String fromChannel) throws IOException
    {
        if (isConnected())
        {

            String[] tempTextArray = clientText.split(" ");

            if (!clientText.equals(""))
            {
                if (clientText.startsWith("/join"))
                {
                    writer.write("JOIN " + clientText.replace("/join ", "") + "\r\n");
                } else if (clientText.startsWith("/nick"))
                {
                    writer.write("NICK " + clientText.replace("/nick ", "") + "\r\n");
                    myNick = clientText.replace("/nick ", "");
                } else if (clientText.startsWith("/msg"))
                {
                    tempTextArray = clientText.split(" ");
                    writer.write("PRIVMSG " + tempTextArray[1] + " :"
                            + clientText.replace("/msg " + tempTextArray[1] + " ", "") + "\r\n");

                    if (clientText.toLowerCase().startsWith("/msg nickserv identify"))
                    {
                        clientText = "*** HIDDEN NICKSERV IDENTIFY ***";
                    }

                    server.printPrivateText(tempTextArray[1], clientText.replace("/msg " + tempTextArray[1] + " ", ""),
                            myNick);
                    gui.setCurrentTab(tempTextArray[1]);
                } else if (clientText.startsWith("/whois"))
                {
                    writer.write("WHOIS " + tempTextArray[1] + "\r\n");
                } else if (clientText.startsWith("/quit"))
                {
                    writer.write("QUIT :" + clientText.replace("/quit ", "") + "\r\n");
                } else if (clientText.startsWith("/part"))
                {
                    writer.write("PART " + fromChannel + " :" + clientText.replace("/part  ", "") + "\r\n");
                } else if (clientText.startsWith("/me"))
                {
                    writer.write("PRIVMSG " + fromChannel + " :" + '\001' + "ACTION" + '\001'
                            + clientText.replace("/me ", "") + "\r\n");
                } else if (clientText.startsWith("CAP") || clientText.startsWith("AUTHENTICATE"))
                {
                    writer.write(clientText + "\r\n");
                } else
                {
                    writer.write("PRIVMSG " + fromChannel + " :" + clientText + "\r\n");
                    server.printChannelText(fromChannel, clientText, myNick);
                }
                writer.flush();

                Constants.LOGGER.log(Level.FINE, "Client Text:- " + fromChannel + " " + clientText);
            }
        }
    }

    private void localMessage(String message)
    {
        server.printServerText(message);
        Constants.LOGGER.log(Level.FINE, "Local Text:-" + message);
    }

    private void serverMessage(Message newMessage)
    {
        if (isConnected())
            try
            {
                messageHandler.parseMessage(newMessage);
                Constants.LOGGER.log(Level.FINE, newMessage.toString());
            } catch (Exception e)
            {
                Constants.LOGGER.log(Level.WARNING, e.toString() + newMessage);
            }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.ConnectionBase#run()
     */
    @Override
    public void run()
    {
        try
        {
            if (getPortNumber() != null && getServer() != null && getNick() != null)
            {
                startUp();
            } else
            {
                Constants.LOGGER.log(Level.SEVERE, "Incomplete settings: (Port " + getPortNumber() + ") (Server "
                        + getServer() + ") (Nick " + getNick() + ") ");
            }

            Constants.LOGGER.log(Level.INFO, "Disconnected safely!");
        } catch (IOException e)
        {
            Constants.LOGGER.log(Level.SEVERE, "startUp() failed! " + e.getLocalizedMessage());
            MessageDialog dialog = new MessageDialog("startUp() failed! " + e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    dialog.setVisible(true);
        }
    }

    @Override
    public void disconnect()
    {
        shutdown = true;
    }
}
