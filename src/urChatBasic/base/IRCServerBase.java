package urChatBasic.base;

import urChatBasic.base.capabilities.CapTypeBase;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.frontend.IRCChannel;
import urChatBasic.frontend.IRCPrivate;
import urChatBasic.frontend.IRCUser;

public interface IRCServerBase
{

    public abstract String getNick ();

    public abstract void setNick (String newNick);

    public abstract String getName ();

    public abstract String getLogin ();

    public abstract String getPassword ();

    public abstract CapTypeBase getAuthentication ();

    public abstract void connect (String[] autoConnectChannels);

    public abstract void reconnectChannels ();

    public abstract void disconnect ();

    public abstract boolean isConnected ();

    public abstract boolean hasConnection ();

    public abstract String toString ();

    public abstract void setName (String serverName);

    public abstract String getPort ();

    public abstract String getProxyHost ();

    public abstract String getProxyPort ();

    public abstract Boolean usingTLS ();

    public abstract Boolean usingSOCKS ();

    /**
     * Get the IRCUser object from the userName - if the IRCUser isn't found, then create it.
     *
     * @param userName
     * @return
     */
    public abstract IRCUser getIRCUser (String userName);

    public abstract void quitRooms();

    public abstract void quitRoom (IRCRoomBase ircRoom);

    /**
     * Closes and removes all private rooms that have been created.
     */


    /**
     * Return the appropriate created channel
     *
     * @param channelName
     * @return IRCChannel
     */
    public abstract IRCChannel getCreatedChannel (String channelName);

    public abstract void setPingReceived();
    /**
     * Return the appropriate created server
     *
     * @param serverName
     * @return IRCServer
     */
    public abstract IRCPrivate getCreatedPrivateRoom (String privateRoom);

    /**
     * Return the appropriate created channel
     *
     * @param roomName
     * @return IRCRoomBase
     */
    public abstract IRCRoomBase getCreatedRoom (String roomName, boolean asPrivate);

    /**
     * Creates a new room based on name
     *
     * @param roomName
     */
    public abstract void addToCreatedRooms (String roomName, boolean asPrivate);

    /**
     * Creates a new Private Room based on IRCUser
     *
     * @param serverName
     * @return
     */
    public abstract IRCPrivate addToPrivateRooms (IRCUser privateRoom);

    /**
     * Used to negotiate a PLAIN SASL connection to the IRC Server.
     */
    public abstract void saslRequestAuthentication ();

    public abstract void saslDoAuthentication ();

    public abstract void saslSendAuthentication ();

    public abstract void saslCompleteAuthentication ();

    public abstract void nickservRequestAuthentication ();

    public abstract void setCapabilities (String capabilityMessage);

    public boolean hasCapability (CapabilityTypes sasl);

    /**
     * Prints the text to the appropriate channels main text window.
     *
     * @param channelName
     * @param line
     */
    public abstract void printChannelText (String channelName, String line, String fromUser);

    /**
     * Prints the text to the appropriate channels main text window. Checks the user exists first and if
     * they are muted else if they don't exist then just create it and print the private text.
     *
     * @param channelName
     * @param line
     */
    public abstract void printPrivateText (String userName, String line, String fromUser);

    public abstract void printServerText (String line);

    public abstract void printEventTicker (String channelName, String eventText);

    // Adds users to the list in the users array[]
    public abstract void addToUsersList (String channelName, String[] users);

    // Adds a single user, good for when a user joins the channel
    public abstract void addToUsersList (String channelName, String user);


    public abstract void setChannelTopic (String channelName, String channelTopic);

    /**
     * Removes a single user from the specified channel. If the call is from "Server" as the channelName
     * it will loop through all createdChannels and remove the user. But only if they were in there to
     * begin with.
     *
     * @param channelName
     * @param user
     */
    public abstract void removeFromUsersList (String channelName, String user);

    public abstract void sendClientText (String line, String source);

    /**
     * This is a forwarding method used to direct the call to the IRCChannel, filters through
     *
     * @param channelName
     * @param user
     * @param newUser
     */
    public abstract void renameUser (String oldUserName, String newUserName);

    public void callForAttention();
}
