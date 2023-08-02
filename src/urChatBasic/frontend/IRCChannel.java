package urChatBasic.frontend;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.*;

import urChatBasic.base.Constants;
import urChatBasic.base.IRCRoomBase;

public class IRCChannel extends IRCRoomBase
{
    /**
     *
     */
    private static final long serialVersionUID = 1358231872908927052L;

    ////////////////
    // GUI ELEMENTS//
    ////////////////

    // wantsAttentionStuff

    /**
     * Constructor
     *
     * @param server
     * @param channelName
     */
    public IRCChannel(IRCServer server, String channelName)
    {
        super(server);
        // Create the initial size of the panel
        // Set size of the overall panel
        setPreferredSize(new Dimension(Constants.MAIN_WIDTH, Constants.MAIN_HEIGHT));
        setBackground(Color.gray);
        setupMainPanel();
        setRoomName(channelName);
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
        historyFileName = historyDateFormat.format(todayDate) + " " + getRoomName() + ".log";
        this.myMenu = new ChannelPopUp();
        this.setFont(gui.getFont());
        fontPanel = new FontPanel(this);
        mainPanel.add(fontPanel, BorderLayout.NORTH);
        fontPanel.setVisible(false);
        Image tempIcon = null;
        try
        {
            tempIcon = ImageIO.read(new File(Constants.RESOURCES_DIR + "Room.png"));
        } catch (IOException e)
        {
            Constants.LOGGER.log(Level.SEVERE, "FAILED TO LOAD Room.png: " + e.getLocalizedMessage());
        }
        icon = new ImageIcon(tempIcon);

    }
}
