package frontend;

import static org.testng.Reporter.log;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
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
    final static int MAX_CHANNEL_NAMES = 10;
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
        log(message, true);
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
        URProfilesUtil.deleteProfile(testDriver.getTestProfileName(), false);
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

    @Test
    public void changeColoursBenchmark () throws InterruptedException
    {
        Instant startBenchmark = Instant.now();
        // Get Current Font in Appearance panel
        URStyle guiStyle = testGUI.getStyle();
        URStyle newStyle = guiStyle.clone();

        newStyle.setForeground(TestUtils.getRandomColour());
        log("Set foreground to " +URColour.hexEncode(newStyle.getForeground().get()), true);

        testGUI.getFontPanel().setDefaultStyle(newStyle);
        TestDriverGUI.waitForEverything(testGUI);
        guiStyle = testGUI.getStyle();
        logImportantInfo( "Took " + Duration.between(startBenchmark, Instant.now()).toMillis() +  "ms to update colours.");
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
