package frontend;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.Reporter.log;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.text.BadLocationException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import urChatBasic.backend.Connection;
import urChatBasic.backend.MessageHandler;
import urChatBasic.backend.MessageHandler.Message;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.backend.utils.URStyle;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.base.proxy.ProxyTypes;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.IRCChannel;
import urChatBasic.frontend.IRCServer;
import urChatBasic.frontend.IRCUser;
import urChatBasic.frontend.UserGUI;
import urChatBasic.frontend.utils.URColour;
import utils.TestDriverGUI;

public class AppearanceTests
{
    MessageHandler testHandler;
    IRCServer testServer;
    TestDriverGUI testDriver;
    UserGUI testGUI;
    final static int MAX_CHANNEL_NAMES = 10;
    final static String CHANNEL_PREFIX = "#someChannel";
    final List<String> PUB_CHANNEL_NAMES = new ArrayList<>();
    IRCUser testUser;
    Connection testConnection;

    @BeforeClass(alwaysRun = true)
    public void setUp () throws Exception
    {
        testDriver = new TestDriverGUI();
        TestDriverGUI.startTestGUI(DriverGUI.gui);
        testGUI = DriverGUI.gui;
        testServer = new IRCServer("testServer", "testUser", "testUser", "testPassword", "1337", true, "testProxy", "1234", ProxyTypes.NONE.getType(),
                CapabilityTypes.NONE.getType());
        testUser = new IRCUser(testServer, "testUser");

        for (int i = 0; i < MAX_CHANNEL_NAMES; i++)
        {
            PUB_CHANNEL_NAMES.add(CHANNEL_PREFIX + i);
        }

        for (String channelName : PUB_CHANNEL_NAMES)
        {
            testServer.addToCreatedChannels(channelName, false);
        }

        testConnection = new Connection(testServer);
        testHandler = testConnection.getMessageHandler();

        for (String channelName : PUB_CHANNEL_NAMES)
        {
            String rawMessage = ":someuser!~someuser@urchatclient PRIVMSG " + channelName + " :Welcome to " + channelName;

            Message testMessage = testHandler.new Message(rawMessage);
            testHandler.parseMessage(testMessage);
        }

        TestDriverGUI.waitForEverything(testGUI);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown () throws Exception
    {
        // Reporter.log("Deleting testing profile.", true);
        testServer.quitChannels();
        // URProfilesUtil.getActiveProfilePath().sync();
        // URProfilesUtil.getActiveProfilePath().sync();
        URProfilesUtil.deleteProfile(testDriver.getTestProfileName());
        TestDriverGUI.closeWindow();
    }

    /*
     * Tests: - Change Font - Change Font Size - Change Foreground Colour - Change Background Colour
     */

    @Test
    public void changeDefaultFontTest () throws BadLocationException, InterruptedException
    {
        // Get Current Font in Appearance panel
        URStyle guiStyle = testGUI.getStyle();

        // Get Current Font in all rooms
        for (String pubChannelName : PUB_CHANNEL_NAMES)
        {
            IRCChannel pubChannel = testServer.getCreatedChannel(pubChannelName);
            log("Have joined " + pubChannelName + " successfully?", true);
            String welcomeMessage = pubChannel.getLineFormatter().getLineAtPosition(13).split("] ")[1].trim();
            assertEquals("<someuser> Welcome to " + pubChannelName, welcomeMessage);

            log("Check current style in the channel is correct.", true);
            URStyle channelStyle = pubChannel.getLineFormatter().getStyleAtPosition(22, welcomeMessage);

            assertTrue(guiStyle.equals(channelStyle));
        }

        String[] FONT_LIST = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        URStyle newStyle = guiStyle.clone();
        newStyle.setFont(new Font(FONT_LIST[new Random().nextInt(FONT_LIST.length)], Font.PLAIN, 8));
        testGUI.getFontPanel().setDefaultStyle(newStyle);
        TestDriverGUI.waitForEverything(testGUI);
        guiStyle = testGUI.getStyle();
        for (String pubChannelName : PUB_CHANNEL_NAMES)
        {
            IRCChannel pubChannel = testServer.getCreatedChannel(pubChannelName);
            String welcomeMessage = pubChannel.getLineFormatter().getLineAtPosition(13).split("] ")[1].trim();
            log("Check current style has updated.", true);

            URStyle channelStyle = pubChannel.getLineFormatter().getStyleAtPosition(22, welcomeMessage);

            log("Test Style: " + guiStyle, true);
            log("Channel Style: " + channelStyle, true);

            String testStyleFont = guiStyle.getFamily().get();
            String channelStyleFont = channelStyle.getFamily().get();
            assertEquals("Channel font family doesn't match GUI font family.", testStyleFont, channelStyleFont);

            int testStyleSize = guiStyle.getSize().get();
            int channelStyleSize = channelStyle.getSize().get();
            assertEquals("Channel font size doesn't match GUI font size.", testStyleSize, channelStyleSize);

            String testStyleForeground = URColour.hexEncode(guiStyle.getForeground().get());
            String channelStyleForeground = URColour.hexEncode(channelStyle.getForeground().get());
            assertEquals("Channel foreground doesn't match GUI font foreground.", testStyleForeground, channelStyleForeground);

            String testStyleBackground = URColour.hexEncode(guiStyle.getBackground().get());
            String channelStyleBackground = URColour.hexEncode(channelStyle.getBackground().get());
            assertEquals("Channel background doesn't match GUI font background.", testStyleBackground, channelStyleBackground);

        }
    }
}
