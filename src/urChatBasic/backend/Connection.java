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

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class Connection implements ConnectionBase{

	private BufferedWriter writer;
	private BufferedReader reader;

	private IRCServerBase server;
	private String myNick;
	private String login;
	private boolean isTLS;
	private boolean useSOCKS;
	private String portNumber;
	private Socket mySocket;
	private MessageHandler messageHandler;
	public UserGUIBase gui;

	//Used for Logging messages received by the server
	//Debug Mode
	//Currently deprecated
	//private DateFormat debugDateFormat = new SimpleDateFormat("ddMMyyyy");
	//private DateFormat debugTimeFormat = new SimpleDateFormat("HHmm");
	//private Date todayDate = new Date();
	//private String debugFile;

    public Connection(IRCServerBase server,String nick,String login,String portNumber,Boolean isTLS, Boolean useSOCKS, UserGUIBase ugb){
    	this.gui = ugb;
    	this.server =  server;
    	this.myNick = nick;
		this.isTLS = isTLS;
		this.useSOCKS = useSOCKS;
    	if(portNumber.trim().equals(""))
    		this.portNumber = Constants.DEFAULT_FIRST_PORT;
    	this.portNumber = portNumber;
    	this.login = login;
    	this.messageHandler = new MessageHandler(this.server);
	}

    /* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#isConnected()
	 */
    @Override
	public Boolean isConnected(){
    	return (mySocket != null) && (server != null) && (mySocket.isConnected()) && (gui.getTabIndex(this.server.getName()) > -1);
    }

    /* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#getServer()
	 */
    @Override
	public IRCServerBase getServer(){
    	return this.server;
    }

    /* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#getServer()
	 */
    @Override
	public String getPortNumber(){
    	return this.portNumber;
    }

    /* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#getServer()
	 */
    @Override
	public String getLogin(){
    	return this.login;
    }

	@Override
	public boolean usingTLS() {
		return this.isTLS;
	}

	@Override
	public boolean usingSOCKS() {
		return this.useSOCKS;
	}

	private void startUp() throws IOException{

		localMessage("Attempting to connect to "+server);

		// Determine the socket type to be used
		if (usingSOCKS()) {
			Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 1080));
			Socket proxySocket = new Socket(proxy);
			InetSocketAddress address = new InetSocketAddress(server.getName(), Integer.parseInt(getPortNumber()));

			if (usingTLS()) {
				proxySocket.connect(address);

				SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				mySocket = sslsocketfactory.createSocket(proxySocket, address.getHostName(), address.getPort(), true);
			} else {
				proxySocket.connect(address);
				mySocket = proxySocket;
			}
		} else {
			if (usingTLS()) {
				SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				mySocket = sslsocketfactory.createSocket(server.getName(), Integer.parseInt(getPortNumber()));
			} else {
				mySocket = new Socket(server.getName(), Integer.parseInt(getPortNumber()));
			}
		}



		writer = new BufferedWriter(
                new OutputStreamWriter(mySocket.getOutputStream( )));
        reader = new BufferedReader(
                new InputStreamReader(mySocket.getInputStream( ),StandardCharsets.UTF_8));

        localMessage("Connected to "+getServer());


        String line = null;

        // Log on to the server.
        writer.write("NICK " + getNick() + "\r\n");
        writer.write("USER " + login + " 8 * : "+getLogin()+"\r\n");
        localMessage("Connect with nick "+getNick());
        writer.flush( );

        // Read lines from the server until it tells us we have connected.

        while ((line = reader.readLine()) != null) {
        	if (line.indexOf("004") >= 0) {
                //Logged in successfully
        		localMessage("Logged in successfully.");
        		gui.connectFavourites(server);
                break;
            } else
            	serverMessage(messageHandler.new Message(line));
        }

        // Keep reading lines from the server.
        while ((line = reader.readLine()) != null) {
           	if (line.toLowerCase( ).startsWith("ping")) {
                // We must respond to PINGs to avoid being disconnected.
                writer.write("PONG " + line.substring(line.indexOf(':')+1) + "\r\n");
                writer.flush();
            } else
            	serverMessage(messageHandler.new Message(line));
        }

		//writer.close();
		//reader.close();
		//mySocket.close();
    }

	/* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#setNick(java.lang.String)
	 */
	@Override
	public void setNick(String newNick){
		myNick = newNick;
	}

	/* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#getNick()
	 */
	@Override
	public String getNick(){
		return myNick;
	}

	/* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#sendClientText(java.lang.String, java.lang.String)
	 */
	@Override
	public void sendClientText(String clientText,String fromChannel) throws IOException{
		if(isConnected()){

			String[] tempTextArray = clientText.split(" ");

			if(clientText != ""){
				if(clientText.startsWith("/join")){
					writer.write("JOIN " + clientText.replace("/join ","") +"\r\n");
				} else if(clientText.startsWith("/nick")){
						writer.write("NICK " + clientText.replace("/nick ","") +"\r\n");
						myNick = clientText.replace("/nick ","");
				} else if(clientText.startsWith("/msg")){
					tempTextArray = clientText.split(" ");
					if(tempTextArray.length > -1){
						writer.write("PRIVMSG " + tempTextArray[1] + " :"+clientText.replace("/msg "+tempTextArray[1]+" ", "") +"\r\n");
						server.printPrivateText(tempTextArray[1], clientText.replace("/msg "+tempTextArray[1]+" ", ""), myNick);
						gui.setCurrentTab(tempTextArray[1]);
					}
				} else if(clientText.startsWith("/whois")){
					if(tempTextArray.length > -1)
						writer.write("WHOIS " + tempTextArray[1] +"\r\n");
				} else if(clientText.startsWith("/quit")){
						writer.write("QUIT :" + clientText.replace("/quit ","") +"\r\n");
				} else if(clientText.startsWith("/part")){
					writer.write("PART " + fromChannel + " :" + clientText.replace("/part  ","") +"\r\n");
				} else if(clientText.startsWith("/me")){
					writer.write("PRIVMSG " + fromChannel + " :"+'\001'+"ACTION"+'\001' + clientText.replace("/me ","") +"\r\n");
				} else {
					writer.write("PRIVMSG " + fromChannel + " :"+clientText+"\r\n");
					server.printChannelText(fromChannel, clientText, myNick);
				}
			writer.flush();

			if(clientText.toLowerCase().startsWith("/msg nickserv"))
				clientText = "*** HIDDEN NICKSERV IDENTIFY ***";
			Constants.LOGGER.log(Level.FINE, "Client Text:- "+fromChannel+" "+clientText);
			}
		}
	}

	private void localMessage(String message){
		server.printServerText(message);
		Constants.LOGGER.log(Level.FINE, "Local Text:-"+message);
	}

	private void serverMessage(Message newMessage){
		if(isConnected())
			try{
				messageHandler.parseMessage(newMessage);
			Constants.LOGGER.log(Level.FINE, newMessage.rawMessage);
			} catch(Exception e){
				Constants.LOGGER.log(Level.WARNING, newMessage.rawMessage);
			}
	}
	/* (non-Javadoc)
	 * @see urChatBasic.base.ConnectionBase#run()
	 */
	@Override
	public void run() {
		try {
			if(getPortNumber() != null && getServer() != null && getNick() != null)
				startUp();
			else
				Constants.LOGGER.log(Level.SEVERE, "Incomplete settings: (Port "+getPortNumber()+") (Server "+getServer()+") (Nick "+getNick()+") ");
		} catch (IOException e) {
			Constants.LOGGER.log(Level.SEVERE, "startUp() failed! " + e.getLocalizedMessage());
		}
	}


}