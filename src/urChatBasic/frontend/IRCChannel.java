package urChatBasic.frontend;

import urChatBasic.base.IRCRoomBase;

public class IRCChannel extends IRCRoomBase
{
    /**
     *
     */
    private static final long serialVersionUID = 1358231872908927052L;

    /**
     * Constructor
     *
     * @param server
     * @param channelName
     */
    public IRCChannel(IRCServer server, String channelName)
    {
        super(server, channelName);
    }
}
