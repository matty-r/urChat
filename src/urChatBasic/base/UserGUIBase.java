package urChatBasic.base;

import java.awt.Graphics;
import java.util.prefs.Preferences;
import urChatBasic.base.capabilities.CapTypeBase;

public interface UserGUIBase
{

    public abstract int getLimitServerLinesCount();

    public abstract int getLimitChannelLinesCount();

    /**
     * Sets the tab to the index number
     *
     * @param indexNum
     */
    public abstract void setCurrentTab(int indexNum);

    /**
     * Sets the tab to the name
     *
     * @param indexNum
     */
    public abstract void setCurrentTab(String tabName);

    /**
     * Returns a tabs Index by name
     *
     * @param tabName
     * @return int
     */
    public abstract int getTabIndex(String tabName);

    public abstract Boolean saveChannelHistory();

    public abstract Boolean saveServerHistory();

    /**
     * Return the appropriate created server
     *
     * @param serverName
     * @return IRCServer
     */
    public abstract IRCServerBase getCreatedServer(String serverName);

    /**
     * Called when the connection to the server was a success
     *
     * @param server
     */
    public abstract void setupServerTab(IRCServerBase server);

    /**
     * Deletes empty favourites
     */
    public abstract void cleanUpSettings();

    /**
     * Creates a new server based on name
     *
     * @param serverName
     */
    public abstract void addToCreatedServers(String serverName);

    /**
     * Check to see if there are any Servers at all.
     *
     * @param channelName
     * @return IRCChannel
     */
    public abstract Boolean isCreatedServersEmpty();

    /**
     * Show event ticker?
     *
     * @return Boolean
     */
    public abstract Boolean isShowingEventTicker();

    /**
     * Show users list?
     *
     * @return Boolean
     */
    public abstract Boolean isShowingUsersList();

    /**
     * Show users list?
     *
     * @return Boolean
     */
    public abstract Boolean isClickableLinksEnabled();

    public abstract CapTypeBase authenticationType();

    /**
     * Show joins/quits in the event ticker?
     *
     * @return Boolean
     */
    public abstract Boolean isJoinsQuitsTickerEnabled();

    /**
     * Show joins/quits in the main window?
     *
     * @return Boolean
     */
    public abstract Boolean isJoinsQuitsMainEnabled();

    /**
     * Save channel chat history?
     *
     * @return Boolean
     */
    public abstract Boolean isChannelHistoryEnabled();

    /**
     * Limit the number of lines in the server activity window
     *
     * @return Boolean
     */
    public abstract Boolean isLimitedServerActivity();

    /**
     * Limit the number of lines in the channel history
     *
     * @return Boolean
     */
    public abstract Boolean isLimitedChannelActivity();

    /**
     * Add timestamp to chat text?
     *
     * @return Boolean
     */
    public abstract Boolean isTimeStampsEnabled();

    /**
     * Save text that I type, this allows using the up and down arrows to repeat text.
     *
     * @return
     */
    public abstract Boolean isClientHistoryEnabled();

    /**
     * Adds the favourite as an element to the favourites list - also adds the item to the
     * clientSettings.
     *
     * @param server
     * @param channel
     */
    public abstract void addFavourite(String server, String channel);

    /**
     * Used to check if the channel is already a favourite from an IRCChannel
     *
     * @param channel
     * @return
     */
    public abstract Boolean isFavourite(IRCRoomBase channel);

    // public abstract void removeFavourite(String server, String channel);

    public abstract void sendGlobalMessage(String message, String sender);

    /**
     * Used to connect to all the favourites. This gets run from Connection once the socket has
     * successfully connected to the initial server.
     *
     * @param IRCServer
     */
    public abstract void connectFavourites(IRCServerBase server);

    /**
     * Remove and disconnect all private rooms, channels and servers
     */
    public abstract void shutdownAll();

    public abstract void setProfileName(String newProfileName);

    public abstract String getProfileName();

    /**
     * Loops through all servers and disconnects and deletes the tab
     */
    public abstract void quitServers();

    public abstract void quitServer(IRCServerBase server);

    public abstract void removeClientSetting(String node, String key);

    public abstract int getEventTickerDelay();

    public abstract void setClientSettings();

    public abstract Preferences getProfilePath();

    public abstract void run();

    public abstract void removeFavourite(String favServer, String favChannel);

}
