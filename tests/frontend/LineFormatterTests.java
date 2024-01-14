package frontend;

import static org.testng.AssertJUnit.*;
import java.awt.IllegalComponentStateException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.TimeUnit;
import javax.swing.text.BadLocationException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import urChatBasic.backend.Connection;
import urChatBasic.backend.MessageHandler;
import urChatBasic.backend.MessageHandler.Message;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.IRCRoomBase;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.IRCServer;
import urChatBasic.frontend.IRCUser;
import urChatBasic.frontend.UserGUI;
import utils.TestDriverGUI;

public class LineFormatterTests
{
    MessageHandler testHandler;
    IRCServer testServer;
    TestDriverGUI testDriver;
    UserGUI testGUI;
    IRCRoomBase testPrivChannel;
    final String PUB_CHANNEL_NAME = "#someChannel";
    IRCRoomBase testPubChannel;
    IRCUser testUser;
    Connection testConnection;

    @BeforeClass(alwaysRun = true)
    public void setUp () throws Exception
    {
        testDriver = new TestDriverGUI();
        TestDriverGUI.startTestGUI(DriverGUI.gui);
        testGUI = DriverGUI.gui;
        testServer = new IRCServer("testServer", "testUser", "testUser", "testPassword", "1337", true, "testProxy", "1234", true, CapabilityTypes.NONE.getType());
        testUser = new IRCUser(testServer, "testUser");
        testServer.addToCreatedRooms(PUB_CHANNEL_NAME, false);
        testPubChannel = testServer.getCreatedChannel(PUB_CHANNEL_NAME);
        testConnection = new Connection(testServer);
        testHandler = testConnection.getMessageHandler();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown () throws Exception
    {
        // Reporter.log("Deleting testing profile.", true);
        testServer.quitRooms();
        // URProfilesUtil.getActiveProfilePath().sync();
        // URProfilesUtil.getActiveProfilePath().sync();
        URProfilesUtil.deleteProfile(testDriver.getTestProfileName());
        TestDriverGUI.closeWindow();
    }

    // Test right-click on a nick and selecting whois
    // Exception is thrown when we attempt to show the right-click menu, but the TestGUI isn't visible
    @Test(expectedExceptions = IllegalComponentStateException.class)
    public void rightClickOnNickTest () throws BadLocationException, InterruptedException
    {
        String rawMessage = ":someuser!~someuser@urchatclient PRIVMSG "+PUB_CHANNEL_NAME+" :Welcome to somechannel!";
        Message testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);

        while (testPubChannel.messageQueueWorking())
        {
            TimeUnit.SECONDS.sleep(1);
        }

        testGUI.tabbedPane.setSelectedComponent(testPubChannel);

        assertEquals("<someuser> Welcome to somechannel!", testPubChannel.getLineFormatter().getLineAtPosition(9).split("] ")[1].trim());
        testPubChannel.getChannelTextPane().setCaretPosition(9);
        // Right click on someuser
        Rectangle2D coords = testPubChannel.getChannelTextPane().modelToView2D(testPubChannel.getChannelTextPane().getCaretPosition());

        // Right-Click mouse event at the x-y coords of the caret in the text pane
        MouseEvent event = new MouseEvent(
            testPubChannel.getChannelTextPane(),
            MouseEvent.BUTTON3,
            System.currentTimeMillis(),
            MouseEvent.BUTTON3_DOWN_MASK,
            (int) coords.getX(),
            (int) coords.getY(),
            1,
            false
        );

        // Here we expect it to throw an exception because the TestGUI isn't visible
        for (MouseListener listener : testPubChannel.getChannelTextPane().getMouseListeners()) {
            if(listener instanceof IRCRoomBase.ChannelClickListener)
            {
                listener.mouseClicked(event);
                break;
            }
        }
    }
}
