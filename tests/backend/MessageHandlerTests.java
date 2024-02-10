package backend;

import static org.testng.AssertJUnit.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import urChatBasic.backend.Connection;
import urChatBasic.backend.MessageHandler;
import urChatBasic.backend.MessageHandler.Message;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCChannelBase;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.base.proxy.ProxyTypes;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.IRCPrivate;
import urChatBasic.frontend.IRCServer;
import urChatBasic.frontend.IRCUser;
import urChatBasic.frontend.UserGUI;
import urChatBasic.frontend.panels.InterfacePanel;
import utils.TestDriverGUI;


public class MessageHandlerTests
{
    MessageHandler testHandler;
    IRCServer testServer;
    TestDriverGUI testDriver;
    UserGUI testGUI;
    IRCChannelBase testPrivChannel;
    final String PUB_CHANNEL_NAME = "#someChannel";
    IRCChannelBase testPubChannel;
    IRCUser testUser;
    Connection testConnection;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception
    {
        testDriver = new TestDriverGUI();
        testGUI = DriverGUI.gui;
        testServer = new IRCServer("testServer", "testUser", "testUser", "testPassword", "1337", true, "testProxy",
                "1234", ProxyTypes.NONE.getType(), CapabilityTypes.NONE.getType());
        testUser = new IRCUser(testServer, "testUser");
        testServer.addToPrivateChannels(testUser);
        testPrivChannel = testServer.getCreatedPrivateChannel(testUser.toString());
        testServer.addToCreatedChannels(PUB_CHANNEL_NAME, false);
        testPubChannel = testServer.getCreatedChannel(PUB_CHANNEL_NAME);
        testConnection = new Connection(testServer);
        testHandler = testConnection.getMessageHandler();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown () throws Exception
    {
        // Reporter.log("Deleting testing profile.", true);
        testServer.quitChannels();
        // URProfilesUtil.getActiveProfilePath().sync();
        // URProfilesUtil.getActiveProfilePath().sync();
        URProfilesUtil.deleteProfile(testDriver.getTestProfileName(), false);
        TestDriverGUI.closeWindow();
    }

    @Test(groups = {"Test #001"})
    public void nickIsHighStyleTest() throws BadLocationException, InterruptedException
    {
        // This should create a someuser private channel
        String rawMessage = ":someuser!~someuser@urchatclient PRIVMSG testUser :hello testUser!";
        Message testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);
        IRCPrivate someUserChannel = testServer.getCreatedPrivateChannel("someuser");
        String testLine = someUserChannel.getLineFormatter().getLatestLine(); // "[0629] <someuser> hello testUser!"

        while (someUserChannel.messageQueueWorking())
        {
            TimeUnit.SECONDS.sleep(1);
        }

        // Should be highStyle because someuser mentioned my nick, testUser
        assertEquals("highStyle",
                someUserChannel.getLineFormatter().getStyleAtPosition(11, testLine).getAttribute("name"));
    }

    @Test(groups = {"Test #001"})
    public void recvActionMessage()
    {
        String rawMessage =
                ":" + testUser + "!~" + testUser + "@userHost PRIVMSG " + testUser + " :ACTION claps hands";
        Message testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);
        assertEquals("> claps hands", testMessage.getBody());
    }

    @Test(groups = {"Test #002"})
    @Parameters({"channelName"})
    public void noticeMessageParseTest(@Optional(PUB_CHANNEL_NAME) String channelName)
    {
        Reporter.log("Using @Optional variable channelName: " + channelName);

        String rawMessage = ":ChanServ!ChanServ@services.libera.chat NOTICE userName :[" + channelName
                + "] Welcome to " + channelName + ".";
        Message testMessage = testHandler.new Message(rawMessage);

        assertEquals(channelName, testMessage.getChannel());
        // assertEquals("Welcome to #someChannel.", testMessage.getBody());
    }


    @Test(groups = {"Test #003"})
    public void nickIsNickStyleTest() throws BadLocationException, InterruptedException
    {
        String rawMessage = ":someuser!~someuser@urchatclient PRIVMSG "+PUB_CHANNEL_NAME+" :Welcome to somechannel!";
        Message testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);
        String testLine = testPubChannel.getLineFormatter().getLatestLine(); // "[0629] <someuser> hello world!"

        while (testPubChannel.messageQueueWorking())
        {
            TimeUnit.SECONDS.sleep(1);
        }

        // Should be nickStyle because the user didn't mention testUser and is just a normal message
        assertEquals("nickStyle",
                testPubChannel.getLineFormatter().getStyleAtPosition(11, testLine).getAttribute("name"));
    }

    @Test(groups = {"Test #003"})
    public void sendActionMessageChannel()
    {
        String rawMessage = "/me claps hands";
        try
        {
            testConnection.sendClientText(rawMessage, "#channelname");

        } catch (IOException e)
        {
            Constants.LOGGER.warn(e.getLocalizedMessage(), e);
        }

        Reporter.log("This test won't run unless the dependant method is included in the test and passed.");
    }

    @Test
    public void sendPrivateMessageMessageUser()
    {
        String rawMessage = "/msg otheruser hello, did you get this message?";
        try
        {
            testConnection.sendClientText(rawMessage, "otheruser");
        } catch (IOException e)
        {
            Constants.LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    @Test(groups = {"Test #004"})
    public void sendActionMessageUser()
    {
        Reporter.log("Depends on sendPrivateMessageMessageUser which will be implicitly included in this test group.");
        String rawMessage = "/me claps hands";
        try
        {
            testConnection.sendClientText(rawMessage, "otheruser");
        } catch (IOException e)
        {
            Constants.LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    @Test
    public void noticeMessage2()
    {
        String rawMessage =
                ":channeluser!channelname@channelname/bot/primary NOTICE myUsername :this is just some notice message directed to this user from the ";
        Message testMessage = testHandler.new Message(rawMessage);

        assertEquals("#channelname", testMessage.getChannel());
    }

    @Test
    public void handleChannelNoticeUrl()
    {
        String rawMessage = ":services. 328 userName "+PUB_CHANNEL_NAME+" :https://somechannel.com/url";
        Message testMessage = testHandler.new Message(rawMessage);

        assertEquals(MessageHandler.NoticeMessage.class, testMessage.getMessageBase().getClass());
    }

    @Test
    public void testQuitChannel1()
    {
        String rawMessage = ":userName!~userName@user/userName QUIT :Read error: Connection reset by peer";
        Message testMessage = testHandler.new Message(rawMessage);

        assertEquals(MessageHandler.DisconnectMessage.class, testMessage.getMessageBase().getClass());
    }

    @Test
    public void testQuitChannel2()
    {
        String rawMessage = ":userName!~userName@user/userName QUIT :Remote host closed the connection";
        Message testMessage = testHandler.new Message(rawMessage);

        assertEquals(MessageHandler.DisconnectMessage.class, testMessage.getMessageBase().getClass());
    }

    @Test
    public void testQuitServer()
    {
        String rawMessage = "ERROR :\"Goodbye cruel world\"";
        Message testMessage = testHandler.new Message(rawMessage);

        assertEquals(MessageHandler.DisconnectErrorMessage.class, testMessage.getMessageBase().getClass());
    }

    @Test(groups = {"Test #005"})
    public void testChannelLineLimit() throws BadLocationException, InterruptedException
    {
        ((InterfacePanel) testGUI.interfacePanel).setLimitChannelLinesCount(10);
        ((InterfacePanel) testGUI.interfacePanel).setJoinsQuitsMain(false);
        int channelLinesLimit = ((InterfacePanel) testGUI.interfacePanel).getLimitChannelLinesCount();

        String channelMessage = ":" + testUser + "!~" + testUser + "@urchatclient PRIVMSG "+PUB_CHANNEL_NAME+" :line # ";

        for (int i = 0; i < channelLinesLimit + 10; i++)
        {
            Message testMessage = testHandler.new Message(channelMessage + i);
            testHandler.parseMessage(testMessage);
        }

        while (testPubChannel.messageQueueWorking())
        {
            TimeUnit.SECONDS.sleep(1);
        }

        // for (int i = 0; i < serverLinesLimit+10; i++) {
        // Message testMessage = testHandler.new Message(serverMessage + i);
        // testHandler.parseMessage(testMessage);
        // }

        // int serverLinesCount =
        // testServer.getChannelTextPane().getStyledDocument().getDefaultRootElement().getElementCount();
        int channelLinesCount =
                testPubChannel.getLineFormatter().getDocument().getDefaultRootElement().getElementCount();



        String firstLine = testPubChannel.getLineFormatter().getFirstLine();
        String lastLine = testPubChannel.getLineFormatter().getLatestLine(); // "<testUser> line # 509"

        assertTrue("Last line should line # 19 but it was" + lastLine, lastLine.endsWith("line # 19"));

        assertTrue(
                "First line should be line # 10 but it was "
                        + firstLine,
                firstLine.endsWith("line # 10"));
        assertSame("Channel line count should equal the line limit", channelLinesLimit, channelLinesCount - 1);
    }

    @Test(groups = {"Test #005"}, description = "Test Description")
    public void testServerLineLimit() throws BadLocationException, InterruptedException
    {
        ((InterfacePanel) testGUI.interfacePanel).setLimitServerLinesCount(10);
        ((InterfacePanel) testGUI.interfacePanel).setJoinsQuitsMain(false);
        int serverLinesLimit = ((InterfacePanel) testGUI.interfacePanel).getLimitServerLinesCount();

        String serverMessage = ":" + testServer.getName() + " 001 " + testUser + " :line # ";

        for (int i = 0; i < serverLinesLimit + 10; i++)
        {
            Message testMessage = testHandler.new Message(serverMessage + i);
            testHandler.parseMessage(testMessage);
        }

        while (testServer.messageQueueWorking())
        {
            TimeUnit.SECONDS.sleep(1);
        }

        int serverLinesCount =
                testServer.getChannelTextPane().getStyledDocument().getDefaultRootElement().getElementCount();

        String testLine = testServer.getLineFormatter().getLatestLine(); // "<testUser> line # 19"

        assertTrue("Last line should line # 19 but it was" + testLine, testLine.endsWith("line # 19"));

        assertTrue(
                "First line should be line # 10 but it was " + testServer.getChannelTextPane().getText().split(System.lineSeparator())[0],
                testServer.getChannelTextPane().getText().split(System.lineSeparator())[0].trim().endsWith("line # 10"));
        assertSame("Channel line count should equal the line limit", serverLinesLimit, serverLinesCount - 1);
    }

    @Test
    @Ignore
    public void _emojiMessage()
    {
        // test display of emojis in text
        String rawMessage =
                ":sd!~discord@user/sd PRIVMSG "+PUB_CHANNEL_NAME+" :02<textwithEMOJI 🇦🇺> this should show a flag";
        // TODO create this test
    }

    @Test
    public void urlInMessage() throws BadLocationException, InterruptedException
    {
        // test displaying urls

        String rawMessage = ":"+testUser.getName()+"!~"+testUser.getName()+"@urchatclient PRIVMSG "+testUser.getName()+" :https://google.com";
        // String rawMessage2 = "https://duckduckgo.com/?q=irc+urchat&kp=1&t=h_&ia=web";

        Message testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);

        while (testPrivChannel.messageQueueWorking())
        {
            TimeUnit.SECONDS.sleep(1);
        }

        String testLine = testPrivChannel.getLineFormatter().getLatestLine(); // "[0629] <someuser>
                                                                                 // https://google.com"
        // Should be urlStyle, i.e a clickable link
        assertEquals("urlStyle",
                testPrivChannel.getLineFormatter().getStyleAtPosition(19, testLine).getAttribute("name"));
    }

    @Test
    public void channelRegex()
    {
        // find and match against any URLs that may be in the text
        String line = "join #urchat to test the regex";

        Pattern pattern = Pattern.compile(Constants.CHANNEL_REGEX);
        Matcher matcher = pattern.matcher(line);


        String regexLine = "";
        while (matcher.find())
        {
            regexLine = matcher.group(1);
        }

        assertTrue(regexLine.equalsIgnoreCase("#urchat"));
    }

    @Test(groups = {"Test #005"})
    public void channelInMessage() throws BadLocationException, InterruptedException
    {
        Reporter.log("No dependency, this will pass regardless of the failed testChannelLineLimit method.");

        // test displaying channel
        Message testMessage =
                testHandler.new Message(":"+testUser.getName()+"!~"+testUser.getName()+"@urchatclient PRIVMSG "+testUser.getName()+" :first line");
        testHandler.parseMessage(testMessage);

        String rawMessage =
                ":"+testUser.getName()+"!~"+testUser.getName()+"@urchatclient PRIVMSG "+testUser.getName()+" :Please join #urchat and go to https://github.com/matty-r/urChat then go back to #anotherchannel";

        testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);
        // StyledDocument testDoc = testPrivChannel.getChannelTextPane().getStyledDocument();

        while (testPrivChannel.messageQueueWorking())
        {
            TimeUnit.SECONDS.sleep(1);
        }

        String testLine = testPrivChannel.getLineFormatter().getLatestLine();
        // Should be channel, i.e clickable name which allows you to join the channel
        assertEquals("channelStyle",
                testPrivChannel.getLineFormatter().getStyleAtPosition(33, testLine).getAttribute("name"));
        assertEquals("urlStyle",
                testPrivChannel.getLineFormatter().getStyleAtPosition(58, testLine).getAttribute("name"));
        assertEquals("channelStyle",
                testPrivChannel.getLineFormatter().getStyleAtPosition(110, testLine).getAttribute("name"));
    }
}
