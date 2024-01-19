package urChatBasic.backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.logging.Level;
import urChatBasic.backend.MessageHandler.Message;
import urChatBasic.base.ConnectionBase;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCServerBase;
import urChatBasic.base.proxy.ProxyTypes;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.dialogs.MessageDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Connection implements ConnectionBase
{
    private BufferedWriter writer;
    private BufferedReader reader;
    private boolean shutdown = false;
    // private String creationTime = (new Date()).toString();

    private IRCServerBase server;

    private Socket mySocket;

    private MessageHandler messageHandler;

    // Connection keep alive stuff
    // Starts the timer with an start delay
    private Timer keepAliveTimer = new Timer((int) Duration.ofSeconds(30).toMillis(), new PingKeepalive());
    // Rate in which to send a PING to the server
    private final int PING_RATE_MS = (int) Duration.ofMinutes(5).toMillis();
    private boolean pingReceived = true;
    private final int MAX_RESPONSE_FAILURES = 2;
    private int currentFailures = 0;
    // only try to reconnect once
    private final int MAX_RECONNECT_ATTEMPTS = 1;
    private boolean reconnect = false;
    private int reconnectAttempts = 0;

    public Connection (IRCServerBase server)
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
    public Boolean isConnected ()
    {
        return !shutdown;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.ConnectionBase#getServer()
     */
    @Override
    public IRCServerBase getServer ()
    {
        return this.server;
    }

    private void startUp () throws IOException
    {
        shutdown = false;

        localMessage("Attempting to connect to " + server);

        // Determine the socket type to be used
        InetSocketAddress endPointAddress = new InetSocketAddress(server.getName(), Integer.parseInt(getServer().getPort()));

        if (!getServer().usingProxy().equals(ProxyTypes.NONE.getType()))
        {
            getServer().usingProxy().createProxy(getServer().getProxyHost(), Integer.parseInt(getServer().getProxyPort()));
            Socket proxySocket = getServer().usingProxy().connectThroughProxy(endPointAddress);

            if (getServer().usingTLS())
            {
                SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                mySocket = sslsocketfactory.createSocket(proxySocket, endPointAddress.getHostName(), endPointAddress.getPort(), true);
                mySocket.setKeepAlive(true);
            } else
            {
                mySocket = proxySocket;
            }
        } else
        {
            if (getServer().usingTLS())
            {
                SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                mySocket = sslsocketfactory.createSocket();
            } else
            {
                mySocket = new Socket();
            }
            mySocket.connect(endPointAddress, 500);
        }

        writer = new BufferedWriter(new OutputStreamWriter(mySocket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(mySocket.getInputStream(), StandardCharsets.UTF_8));

        // if we got this far, we established a connection to the server
        DriverGUI.gui.setupServerTab(server);

        localMessage("Initiating authentication...");

        String line = null;

        try
        {
            // TODO: Not great.. sleep for half a second before continuing on. Maybe we could start reading lines then initiate the rest if we receive a line?
            Thread.sleep(500);
        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Initiate connection to the server.
        writer.write("CAP LS 302\r\n");
        writer.write("NICK " + getServer().getNick() + "\r\n");
        writer.write("USER " + getServer().getLogin() + " 8 * : " + getServer().getLogin() + "\r\n");
        localMessage("Connecting with nick " + getServer().getNick());
        writer.flush();

        keepAliveTimer.setDelay(PING_RATE_MS);
        keepAliveTimer.start();

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

    private class PingKeepalive implements ActionListener
    {
        public void actionPerformed (ActionEvent event)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run ()
                {
                    try
                    {
                        if (shutdown)
                        {
                            keepAliveTimer.stop();
                        } else
                        {
                            if (!pingReceived)
                            {
                                currentFailures++;
                                localMessage("Haven't received the last " + currentFailures
                                        + " pings, something is wrong. Will reconnect after " + MAX_RESPONSE_FAILURES
                                        + " fails");
                                if (currentFailures >= MAX_RESPONSE_FAILURES)
                                {
                                    // This will cause stuff to close, but then also reconnect.. maybe?
                                    shutdown = true;
                                    reconnect = true;
                                }
                            } else
                            {
                                pingReceived = false;

                                writer.write("PING " + new Date().toInstant().toEpochMilli() + "\r\n");
                                writer.flush();
                            }
                        }
                    } catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void setPingReceived ()
    {
        // Reset the fails
        currentFailures = 0;
        pingReceived = true;
    }

    public MessageHandler getMessageHandler ()
    {
        return messageHandler;
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.base.ConnectionBase#sendClientText(java.lang.String, java.lang.String)
     */
    @Override
    public void sendClientText (String clientText, String fromChannel) throws IOException
    {
        String[] tempTextArray = clientText.split(" ");
        String outText = "";
        Message clientMessage = null;

        if (!clientText.equals(""))
        {
            if (clientText.toLowerCase().startsWith("/info"))
            {
                outText = "INFO\r\n";
            }
            if (clientText.toLowerCase().startsWith("/join"))
            {
                outText = "JOIN " + clientText.replace("/join ", "").toLowerCase() + "\r\n";
            } else if (clientText.toLowerCase().startsWith("/nick"))
            {
                outText = "NICK " + clientText.replace("/nick ", "") + "\r\n";
                getServer().setNick(clientText.replace("/nick ", ""));
            } else if (clientText.toLowerCase().startsWith("/msg"))
            {
                tempTextArray = clientText.split(" ");
                outText = "PRIVMSG " + tempTextArray[1] + " :"
                        + clientText.replace("/msg " + tempTextArray[1] + " ", "") + "\r\n";



                String msgPrefix = ":" + getServer().getNick() + "!~" + getServer().getNick() + "@"+Constants.APP_NAME;

                if (!tempTextArray[1].equalsIgnoreCase(getServer().getNick()))
                {
                    clientMessage = messageHandler.new Message(msgPrefix + " " + outText);
                }

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

                String msgPrefix = ":" + getServer().getNick() + "!~" + getServer().getNick() + "@"+Constants.APP_NAME;
                clientMessage = messageHandler.new Message(msgPrefix + " " + outText);
            } else if (clientText.startsWith("CAP") || clientText.startsWith("AUTHENTICATE"))
            {
                outText = clientText + "\r\n";
            } else
            {
                if (null != getServer().getCreatedChannel(fromChannel))
                {
                    outText = "PRIVMSG " + fromChannel + " :" + clientText + "\r\n";

                    String msgPrefix = ":" + getServer().getNick() + "!~" + getServer().getNick() + "@urchatclient";
                    clientMessage = messageHandler.new Message(msgPrefix + " " + outText);

                    // server.printChannelText(fromChannel, clientText, getServer().getNick());
                }
            }

            if (isConnected())
            {
                try
                {
                    writer.write(outText);
                    writer.flush();
                } catch (Exception e)
                {
                    Constants.LOGGER.log(Level.SEVERE, "Problem writing to socket: " + e.toString() + outText);
                }

                try
                {
                    if (null != clientMessage)
                    {
                        clientMessage.exec();
                    }
                } catch (Exception e)
                {
                    Constants.LOGGER.log(Level.SEVERE,
                            "Problem writing out client message: " + e.toString() + clientMessage.getRawMessage());
                }

                Constants.LOGGER.log(Level.FINE, "Client Text:- " + fromChannel + " " + outText);
            } else
            {

                Constants.LOGGER.log(Level.WARNING,
                        "Not connected. Unable to send text:- " + fromChannel + " " + clientMessage.getRawMessage());
            }
        }
    }

    private void localMessage (String message)
    {
        server.printServerText(message);
        Constants.LOGGER.log(Level.INFO, "Local Text:-" + message);
    }

    private void serverMessage (Message newMessage)
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
    public void run ()
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

            if (reconnect && reconnectAttempts < MAX_RECONNECT_ATTEMPTS)
            {
                reconnect = false;
                reconnectAttempts++;
                reader.close();
                writer.close();
                mySocket.close();
                run();
            } else if (shutdown)
            {
                Constants.LOGGER.log(Level.INFO, "Disconnected safely!");
            } else
            {
                shutdown = true;
                reader.close();
                writer.close();
                mySocket.close();
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
    public void disconnect ()
    {
        shutdown = true;
    }
}
