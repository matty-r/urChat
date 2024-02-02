package urChatBasic.frontend;

import java.io.IOException;
import java.net.URL;
import javax.swing.ImageIcon;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCChannelBase;

public class IRCChannel extends IRCChannelBase
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

        URL imgPath = null;
        try
        {
            imgPath =  new URL(Constants.IMAGES_DIR + "Channel.png");
            icon = new ImageIcon(imgPath);
        } catch (IOException e)
        {
            Constants.LOGGER.warn( "COULD NOT LOAD Channel.png " + e.getLocalizedMessage());
        }
    }
}
