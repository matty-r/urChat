package urChatBasic.base;

import java.io.IOException;


public interface ConnectionBase {

	public abstract Boolean isConnected();

	public abstract IRCServerBase getServer();

	public abstract void setNick(String newNick);

	public abstract String getNick();

	public abstract void sendClientText(String clientText, String fromChannel)
			throws IOException;

	/** Write a line to the server log file - checks to make sure
	 * the client has enabled saving of the server history.
	 * */
	public abstract void writeDebugFile(String message) throws IOException;

	public abstract void run();

}