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
import urChatBasic.base.UserGUIBase;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.dialogs.MessageDialog;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;

public class Connection implements ConnectionBase
{
    private BufferedWriter writer;

    private boolean shutdown = false;
    // private String creationTime = (new Date()).toString();

    private IRCServerBase server;

    private Socket mySocket;

    private MessageHandler messageHandler;
    public UserGUIBase gui = DriverGUI.gui;

    // Used for Logging messages received by the server
    // Debug Mode
    // Currently deprecated
    // private DateFormat debugDateFormat = new SimpleDateFormat("ddMMyyyy");
    // private DateFormat debugTimeFormat = new SimpleDateFormat("HHmm");
    // private Date todayDate = new Date();
    // private String debugFile;

    public Connection(IRCServerBase server)
    {
        messageHandler = new MessageHandler(server);
        this.server = server;
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

    private void startUp() throws IOException
    {
        BufferedReader reader;

        localMessage("Attempting to connect to " + server);

        // Determine the socket type to be used
        if (getServer().usingSOCKS())
        {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS,
                    new InetSocketAddress(getServer().getProxyHost(), Integer.parseInt(getServer().getProxyPort())));
            Socket proxySocket = new Socket(proxy);
            InetSocketAddress address =
                    new InetSocketAddress(server.getName(), Integer.parseInt(getServer().getPort()));

            if (getServer().usingTLS())
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
            if (getServer().usingTLS())
            {
                SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                mySocket = sslsocketfactory.createSocket(server.getName(), Integer.parseInt(getServer().getPort()));
            } else
            {
                mySocket = new Socket(server.getName(), Integer.parseInt(getServer().getPort()));
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
        writer.write("NICK " + getServer().getNick() + "\r\n");
        writer.write("USER " + getServer().getLogin() + " 8 * : " + getServer().getLogin() + "\r\n");
        localMessage("Connect with nick " + getServer().getNick());
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
                serverMessage(messageHandler.new Message(line));
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
     * @see urChatBasic.base.ConnectionBase#sendClientText(java.lang.String, java.lang.String)
     */
    @Override
    public void sendClientText(String clientText, String fromChannel) throws IOException
    {
        String[] tempTextArray = clientText.split(" ");
        String outText = "";
        Message clientMessage = null;

        if (!clientText.equals(""))
        {
            if (clientText.toLowerCase().startsWith("/join"))
            {
                outText = "JOIN " + clientText.replace("/join ", "") + "\r\n";
            } else if (clientText.toLowerCase().startsWith("/nick"))
            {
                outText = "NICK " + clientText.replace("/nick ", "") + "\r\n";
                getServer().setNick(clientText.replace("/nick ", ""));
            } else if (clientText.toLowerCase().startsWith("/msg"))
            {
                tempTextArray = clientText.split(" ");
                outText = "PRIVMSG " + tempTextArray[1] + " :"
                        + clientText.replace("/msg " + tempTextArray[1] + " ", "") + "\r\n";

                // if (clientText.toLowerCase().startsWith("/msg nickserv identify"))
                // {
                //     clientText = "*** HIDDEN NICKSERV IDENTIFY ***";
                // }

                // server.printPrivateText(tempTextArray[1], clientText.replace("/msg " + tempTextArray[1] + " ", ""),getServer().getNick());

                String msgPrefix = ":"+ getServer().getNick()+"!~"+ getServer().getNick()+"@urchatclient";
                clientMessage = messageHandler.new Message(msgPrefix + " " +outText);

                // TODO: Set current tab to this new priv tab
                // gui.setCurrentTab(tempTextArray[1]);
            } else if (clientText.toLowerCase().startsWith("/whois"))
            {
                outText = "WHOIS " + tempTextArray[1] + "\r\n";
            } else if (clientText.toLowerCase().startsWith("/quit"))
            {
                outText = "QUIT :" + clientText.replace("/quit ", "") + "\r\n";
            } else if (clientText.toLowerCase().startsWith("/part"))
            {
                outText = "PART " + fromChannel + " :" + clientText.replace("/part  ", "") + "\r\n";
            } else if (clientText.toLowerCase().startsWith("/me") || clientText.toLowerCase().startsWith("/action"))
            {
                String tempText = clientText.replace("/me ", "").replace("/action ", "");
                outText = "PRIVMSG " + fromChannel + " :" + Constants.CTCP_DELIMITER + "ACTION " + tempText
                        + Constants.CTCP_DELIMITER + "\r\n";

                String msgPrefix = ":"+ getServer().getNick()+"!~"+ getServer().getNick()+"@urchatclient";
                clientMessage = messageHandler.new Message(msgPrefix + " " +outText);
            } else if (clientText.startsWith("CAP") || clientText.startsWith("AUTHENTICATE"))
            {
                outText = clientText + "\r\n";
            } else
            {
                if(null != getServer().getCreatedChannel(fromChannel))
                {
                    outText = "PRIVMSG " + fromChannel + " :" + clientText + "\r\n";

                    String msgPrefix = ":"+ getServer().getNick()+"!~"+ getServer().getNick()+"@urchatclient";
                    clientMessage = messageHandler.new Message(msgPrefix + " " +outText);

                    // server.printChannelText(fromChannel, clientText, getServer().getNick());
                }
            }

            if (isConnected())
            {
                try {
                    writer.write(outText);
                    writer.flush();
                } catch (Exception e) {
                    Constants.LOGGER.log(Level.SEVERE, "Problem writing to socket: " + e.toString() + outText);
                }

                try {
                    if(null != clientMessage)
                    {
                        clientMessage.exec();
                    }
                } catch (Exception e) {
                    Constants.LOGGER.log(Level.SEVERE, "Problem writing out client message: " + e.toString() + clientMessage.getRawMessage());
                }

                Constants.LOGGER.log(Level.FINE, "Client Text:- " + fromChannel + " " + outText);
            } else {

                Constants.LOGGER.log(Level.WARNING, "Not connected. Unable to send text:- " + fromChannel + " " + clientMessage.getRawMessage());
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
        {
            try
            {
                messageHandler.parseMessage(newMessage);
                Constants.LOGGER.log(Level.FINE, newMessage.toString());
            } catch (Exception e)
            {
                Constants.LOGGER.log(Level.WARNING, e.toString() + newMessage);
            }
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
            if (getServer().getPort() != null && getServer() != null && getServer().getNick() != null)
            {
                startUp();
            } else
            {
                Constants.LOGGER.log(Level.SEVERE, "Incomplete settings: (Port " + getServer().getPort() + ") (Server "
                        + getServer() + ") (Nick " + getServer().getNick() + ") ");
            }

            if (shutdown)
            {
                Constants.LOGGER.log(Level.INFO, "Disconnected safely!");
            } else
            {
                Constants.LOGGER.log(Level.WARNING, "Disconnected unsafely!");
            }

        } catch (IOException e)
        {
            Constants.LOGGER.log(Level.SEVERE, "startUp() failed! " + e.getLocalizedMessage());
            MessageDialog dialog = new MessageDialog("startUp() failed! " + e.getLocalizedMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            dialog.setVisible(true);
        }
    }

    @Override
    public void disconnect()
    {
        shutdown = true;
    }
}
