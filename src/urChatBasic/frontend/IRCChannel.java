package urChatBasic.frontend;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import urChatBasic.base.Constants;
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

        URL imgPath = null;
        try
        {
            imgPath =  new URL(Constants.IMAGES_DIR + "Room.png");
            icon = new ImageIcon(imgPath);
        } catch (IOException e)
        {
            Constants.LOGGER.warn( "COULD NOT LOAD Room.png " + e.getLocalizedMessage());
        }
    }
}
