package urChatBasic.base;

import java.io.IOException;


public interface ConnectionBase extends Runnable {

	public abstract Boolean isConnected();

	public abstract IRCServerBase getServer();

	public abstract void setNick(String newNick);

	public abstract String getNick();
	
	public abstract String getPortNumber();
	
	public abstract String getLogin();

	public abstract void sendClientText(String clientText, String fromChannel)
			throws IOException;

	/** Write a line to the server log file - checks to make sure
	 * the client has enabled saving of the server history.
	 * */
	
	public abstract void run();

}