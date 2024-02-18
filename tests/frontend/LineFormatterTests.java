package frontend;

import static org.testng.AssertJUnit.*;
import java.awt.IllegalComponentStateException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import urChatBasic.backend.Connection;
import urChatBasic.backend.MessageHandler;
import urChatBasic.backend.MessageHandler.Message;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.IRCChannelBase;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.base.proxy.ProxyTypes;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.IRCServer;
import urChatBasic.frontend.IRCUser;
import urChatBasic.frontend.UserGUI;
import utils.TestDriverGUI;
import static org.testng.Reporter.log;

public class LineFormatterTests
{
    MessageHandler testHandler;
    IRCServer testServer;
    TestDriverGUI testDriver;
    UserGUI testGUI;
    // IRCChannelBase testPrivChannel;
    final String PUB_CHANNEL_NAME = "#someChannel";
    IRCChannelBase testPubChannel;
    IRCUser testUser;
    Connection testConnection;

    @BeforeClass(alwaysRun = true)
    public void setUp () throws Exception
    {
        testDriver = new TestDriverGUI();
        TestDriverGUI.startTestGUI(DriverGUI.gui);
        testGUI = DriverGUI.gui;
        testServer = new IRCServer("testServer", "testUser", "testUser", "testPassword", "1337", true, "testProxy", "1234", ProxyTypes.NONE.getType(), CapabilityTypes.NONE.getType());
        testUser = new IRCUser(testServer, "testUser");
        testServer.addToCreatedChannels(PUB_CHANNEL_NAME, false);
        testPubChannel = testServer.getCreatedChannel(PUB_CHANNEL_NAME);
        testConnection = new Connection(testServer);
        testHandler = testConnection.getMessageHandler();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown () throws Exception
    {
        log("Quit channels", true);
        testServer.quitChannels();
        log("Delete test profile", true);
        URProfilesUtil.deleteProfile(testDriver.getTestProfileName(), false);
        log("Close test window", true);
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

        testGUI.tabbedPane.setSelectedIndex(1);

        assertEquals("<someuser> Welcome to somechannel!", testPubChannel.getLineFormatter().getLineAtPosition(9).split("] ")[1].trim());
        testPubChannel.getChannelTextPane().setCaretPosition(9);
        // Right click on someuser
        final Rectangle2D[] coords = new Rectangle2D[1];
        AtomicBoolean canContinue = new AtomicBoolean(false);

        SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
                try
                {
                    coords[0] = testPubChannel.getChannelTextPane().modelToView2D(testPubChannel.getChannelTextPane().getCaretPosition());
                    canContinue.set(true);
                } catch (BadLocationException e)
                {
                    fail();
                }
			}
		});


        while(!canContinue.get())
        {
            TimeUnit.SECONDS.sleep(1);
        }

        // Right-Click mouse event at the x-y coords of the caret in the text pane
        MouseEvent event = new MouseEvent(
            testPubChannel.getChannelTextPane(),
            MouseEvent.BUTTON3,
            System.currentTimeMillis(),
            MouseEvent.BUTTON3_DOWN_MASK,
            (int) coords[0].getX(),
            (int) coords[0].getY(),
            1,
            false
        );

        // Here we expect it to throw an exception because the TestGUI isn't visible
        for (MouseListener listener : testPubChannel.getChannelTextPane().getMouseListeners()) {
            if(listener instanceof IRCChannelBase.ChannelClickListener)
            {
                listener.mouseClicked(event);
                break;
            }
        }
    }
}
