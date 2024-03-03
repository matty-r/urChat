package frontend;

import static org.junit.Assert.assertEquals;
import static org.testng.Reporter.log;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.swing.text.BadLocationException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import urChatBasic.backend.Connection;
import urChatBasic.backend.MessageHandler;
import urChatBasic.backend.MessageHandler.Message;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.backend.utils.URStyle;
import urChatBasic.base.IRCChannelBase;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.base.proxy.ProxyTypes;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.IRCChannel;
import urChatBasic.frontend.IRCServer;
import urChatBasic.frontend.IRCUser;
import urChatBasic.frontend.UserGUI;
import urChatBasic.frontend.utils.URColour;
import utils.TestDriverGUI;
import utils.TestUtils;

public class UpdateStylesBenchmarkTests {
    MessageHandler testHandler;
    IRCServer testServer;
    TestDriverGUI testDriver;
    UserGUI testGUI;
    final static int MAX_CHANNEL_NAMES = 1;
    final static String CHANNEL_PREFIX = "#someChannel";
    final List<String> PUB_CHANNEL_NAMES = new ArrayList<>();
    IRCUser testUser;
    Connection testConnection;

    LinkedList<String> importantInfo = new LinkedList<>();

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

        Instant startBenchmark = Instant.now();
        for (IRCChannelBase channel : testServer.createdChannels)
        {
            String rawMessage = ":someuser!~someuser@urchatclient PRIVMSG " + channel.getName() + " :Welcome to " + channel.getName();

            Message testMessage = testHandler.new Message(rawMessage);
            testHandler.parseMessage(testMessage);

            testDriver.loadTestLogs(channel);
        }

        TestDriverGUI.waitForEverything(testGUI);
        logImportantInfo("Took " + Duration.between(startBenchmark, Instant.now()).toMillis() +  "ms to load test logs.");
    }

    private void logImportantInfo (String message)
    {
        importantInfo.add(message);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown () throws Exception
    {
        for (String message : importantInfo) {
            System.out.println(message);
        }
        // Reporter.log("Deleting testing profile.", true);
        testServer.quitChannels();
        // URProfilesUtil.getActiveProfilePath().sync();
        // URProfilesUtil.getActiveProfilePath().sync();
        TestDriverGUI.cleanupTestProfiles();
        TestDriverGUI.closeWindow();
    }

    @Test
    public void changeFontBenchmark () throws InterruptedException
    {
        Instant startBenchmark = Instant.now();
        // Get Current Font in Appearance panel
        URStyle guiStyle = testGUI.getStyle();
        URStyle newStyle = guiStyle.clone();

        String[] FONT_LIST = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        newStyle.setFont(new Font(FONT_LIST[new Random().nextInt(FONT_LIST.length)], Font.PLAIN, 8));
        testGUI.getFontPanel().setDefaultStyle(newStyle);
        TestDriverGUI.waitForEverything(testGUI);
        guiStyle = testGUI.getStyle();
        logImportantInfo( "Took " + Duration.between(startBenchmark, Instant.now()).toMillis() +  "ms to update font.");

    }

    @Test(timeOut = 5000)
    public void changeColoursBenchmark () throws InterruptedException, BadLocationException
    {
        Instant startBenchmark = Instant.now();
        // Get Current Font in Appearance panel
        URStyle guiStyle = testGUI.getStyle();
        URStyle newStyle = guiStyle.clone();

        newStyle.setForeground(TestUtils.getRandomColour());
        log("Set foreground to " +URColour.hexEncode(newStyle.getForeground().get()), true);

        testGUI.getFontPanel().setDefaultStyle(newStyle);

        guiStyle = testGUI.getStyle();
        logImportantInfo( "Took " + Duration.between(startBenchmark, Instant.now()).toMillis() +  "ms to update colours.");

        assertEquals(newStyle, guiStyle);

        for (String pubChannelName : PUB_CHANNEL_NAMES)
        {
            TestDriverGUI.waitForEverything(testGUI);

            IRCChannel pubChannel = testServer.getCreatedChannel(pubChannelName);
            // String welcomeMessage = pubChannel.getLineFormatter().getLineAtPosition(13).split("] ")[1].trim(;
            String welcomeMessage = String.join("",testGUI.getNickFormatString("someuser")) + " Welcome to " + pubChannelName;
            log("Check current style has updated.", true);

            URStyle channelStyle = null;

            while (channelStyle == null || !channelStyle.equals(newStyle))
            {
                TimeUnit.MILLISECONDS.sleep(10);
                channelStyle = pubChannel.getLineFormatter().getStyleAtPosition(22, welcomeMessage);
            }

            log("Test Style: " + guiStyle, true);
            log("Channel Style: " + channelStyle, true);

            String testStyleFont = guiStyle.getFamily().get();
            String channelStyleFont = channelStyle.getFamily().get();
            log("Checking "+pubChannelName+" formatting...", true);

            assertEquals(pubChannelName + " font family doesn't match GUI font family.", testStyleFont, channelStyleFont);

            int testStyleSize = guiStyle.getSize().get();
            int channelStyleSize = channelStyle.getSize().get();
            assertEquals(pubChannelName + " font size doesn't match GUI font size.", testStyleSize, channelStyleSize);

            String testStyleForeground = URColour.hexEncode(guiStyle.getForeground().get());
            String channelStyleForeground = URColour.hexEncode(channelStyle.getForeground().get());
            assertEquals(pubChannelName + " foreground doesn't match GUI font foreground.", testStyleForeground, channelStyleForeground);

            String testStyleBackground = URColour.hexEncode(guiStyle.getBackground().get());
            String channelStyleBackground = URColour.hexEncode(channelStyle.getBackground().get());
            assertEquals(pubChannelName + " background doesn't match GUI font background.", testStyleBackground, channelStyleBackground);

        }
    }

    @Test
    public void changeStylesBenchmark () throws InterruptedException
    {
        Instant startBenchmark = Instant.now();
        // Get Current Font in Appearance panel
        URStyle guiStyle = testGUI.getStyle();
        URStyle newStyle = guiStyle.clone();

        newStyle.setForeground(TestUtils.getRandomColour());
        log("Set foreground to " +URColour.hexEncode(newStyle.getForeground().get()), true);

        testGUI.getFontPanel().setDefaultStyle(newStyle);

        // String styleName = styleLabel.getText();
        // FontDialog styleFontDialog = new FontDialog(styleName, previewLineFormatter.getStyleDefault(styleName), URProfilesUtil.getActiveProfilePath());


        TestDriverGUI.waitForEverything(testGUI);
        guiStyle = testGUI.getStyle();
        logImportantInfo( "Took " + Duration.between(startBenchmark, Instant.now()).toMillis() +  "ms to update colours.");
    }
}
