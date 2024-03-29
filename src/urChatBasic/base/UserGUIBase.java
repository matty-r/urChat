package urChatBasic.base;

public interface UserGUIBase
{

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
     * Check to see if there are any Servers at all.
     *
     * @param channelName
     * @return IRCChannel
     */
    public abstract Boolean isCreatedServersEmpty();

    public abstract void addToCreatedServers(IRCServerBase ircServer);

    // TODO: Favourites handling should be done elsewhere.
    // /**
    //  * Adds the favourite as an element to the favourites list - also adds the item to the
    //  * clientSettings.
    //  *
    //  * @param server
    //  * @param channel
    //  */
    // public abstract void addFavourite(String server, String channel);

    // /**
    //  * Used to check if the channel is already a favourite from an IRCChannel
    //  *
    //  * @param channel
    //  * @return
    //  */
    // public abstract Boolean isFavourite(IRCChannelBase channel);

    // public abstract void removeFavourite(String server, String channel);

    // /**
    //  * Used to connect to all the favourites. This gets run from Connection once the socket has
    //  * successfully connected to the initial server.
    //  *
    //  * @param IRCServer
    //  */
    // public abstract void connectFavourites(IRCServerBase server);

    // public abstract void removeFavourite(String favServer, String favChannel);

    public abstract void sendGlobalMessage(String message, String sender);


    /**
     * Loops through all servers and disconnects and deletes the tab
     */
    public abstract void quitServers();

    public abstract void quitServer(IRCServerBase server);

    public abstract void removeClientSetting(String node, String key);

    public abstract void setClientSettings();

    public abstract void run();


}
