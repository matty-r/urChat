package backend;

import urChatBasic.backend.Connection;
import urChatBasic.backend.MessageHandler;
import urChatBasic.backend.MessageHandler.Message;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCRoomBase;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.IRCServer;
import urChatBasic.frontend.IRCUser;
import urChatBasic.frontend.UserGUI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class MessageHandlerTests
{
    MessageHandler testHandler;
    IRCServer testServer;
    UserGUI testGUI;
    IRCRoomBase testChannel;
    IRCUser testUser;
    Connection testConnection;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception
    {
        DriverGUI.createGUI();
        testGUI = DriverGUI.gui;
        testServer = new IRCServer("testServer", "testUser", "testUser", "testPassword", "1337", true, "testProxy",
                "1234", true);
        testUser = new IRCUser(testServer, "testUser");
        testServer.addToPrivateRooms(testUser);
        testChannel = testServer.getCreatedPrivateRoom(testUser.toString());
        testHandler = new MessageHandler(testServer);
        testConnection = new Connection(testServer);
    }

    @Test(groups = {"Test #001"})
    public void nickIsHighStyleTest() throws BadLocationException, InterruptedException
    {
        String rawMessage = ":someuser!~someuser@urchatclient PRIVMSG testUser :hello testUser!";
        Message testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);
        StyledDocument testDoc = testChannel.getChannelTextPane().getStyledDocument();
        String testLine = testChannel.getLineFormatter().getLatestLine(testDoc); // "[0629] <someuser> hello testUser!"

        while (testChannel.messageQueueWorking())
        {
            TimeUnit.SECONDS.sleep(1);
        }

        // Should be highStyle because someuser mentioned my nick, testUser
        assertEquals("highStyle",
                testChannel.getLineFormatter().getStyleAtPosition(testDoc, 11, testLine).getAttribute("name"));
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
    public void noticeMessageParseTest(@Optional("someChannel") String channelName)
    {
        Reporter.log("Using @Optional variable channelName: " + channelName);

        String rawMessage = ":ChanServ!ChanServ@services.libera.chat NOTICE userName :[#" + channelName
                + "] Welcome to #" + channelName + ".";
        Message testMessage = testHandler.new Message(rawMessage);

        assertEquals("#" + channelName, testMessage.getChannel());
        // assertEquals("Welcome to #somechannel.", testMessage.getBody());
    }


    @Test(groups = {"Test #003"}, timeOut = 5000)
    public void nickIsDefaultStyleTest() throws BadLocationException, InterruptedException
    {
        String rawMessage = ":someuser!~someuser@urchatclient PRIVMSG #somechannel :Welcome to somechannel!";
        Message testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);
        StyledDocument testDoc = testChannel.getChannelTextPane().getStyledDocument();
        String testLine = testChannel.getLineFormatter().getLatestLine(testDoc); // "[0629] <someuser> hello world!"

        while (testChannel.messageQueueWorking())
        {
            TimeUnit.SECONDS.sleep(1);
        }

        // Should be defaultStyle because the user didn't mention testUser and is just a normal message
        assertEquals("defaultStyle",
                testChannel.getLineFormatter().getStyleAtPosition(testDoc, 11, testLine).getAttribute("name"));
    }

    @Test(groups = {"Test #003"}, dependsOnMethods = {"backend.MessageHandlerTests.nickIsDefaultStyleTest"})
    public void sendActionMessageChannel()
    {
        String rawMessage = "/me claps hands";
        try
        {
            testConnection.sendClientText(rawMessage, "#channelname");

        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test(groups = {"Test #004"}, dependsOnMethods = {"backend.MessageHandlerTests.sendPrivateMessageMessageUser"})
    public void sendActionMessageUser()
    {
        Reporter.log("Depends on sendPrivateMessageMessageUser which will be implicitly included in this test group.");
        String rawMessage = "/me claps hands";
        try
        {
            testConnection.sendClientText(rawMessage, "otheruser");
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        String rawMessage = ":services. 328 userName #somechannel :https://somechannel.com/url";
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

        assertEquals(MessageHandler.DisconnectMessage.class, testMessage.getMessageBase().getClass());
    }

    @Test(groups = {"Test #005"}, timeOut = 1000)
    public void testChannelLineLimit() throws BadLocationException, InterruptedException
    {
        testGUI.setLimitChannelLines(10);
        testGUI.setJoinsQuitsMain(false);
        int channelLinesLimit = testGUI.getLimitChannelLinesCount();

        String channelMessage = ":" + testUser + "!~" + testUser + "@urchatclient PRIVMSG #somechannel :line # ";

        for (int i = 0; i < channelLinesLimit + 10; i++)
        {
            Message testMessage = testHandler.new Message(channelMessage + i);
            testHandler.parseMessage(testMessage);
        }

        while (testChannel.messageQueueWorking())
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
                testChannel.getChannelTextPane().getStyledDocument().getDefaultRootElement().getElementCount();

        StyledDocument testDoc = testChannel.getChannelTextPane().getStyledDocument();
        String testLine = testChannel.getLineFormatter().getLatestLine(testDoc); // "<testUser> line # 509"

        assertTrue("Last line should line # 19 but it was" + testLine, testLine.endsWith("line # 19"));

        assertTrue(
                "First line should be line # 10 but it was "
                        + testChannel.getChannelTextPane().getText().split("\n")[0],
                testChannel.getChannelTextPane().getText().split("\n")[0].endsWith("line # 10"));
        assertSame("Channel line count should equal the line limit", channelLinesLimit, channelLinesCount - 1);
    }

    @Test(groups = {"Test #005"}, dependsOnMethods = {"backend.MessageHandlerTests.testChannelLineLimit"}
        , description = "This test depends on testChannelLineLimit which should fail due to hitting the timeout")
    public void testServerLineLimit() throws BadLocationException, InterruptedException
    {
        testGUI.setLimitServerLines(10);
        testGUI.setJoinsQuitsMain(false);
        int serverLinesLimit = testGUI.getLimitServerLinesCount();

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

        StyledDocument testDoc = testServer.getChannelTextPane().getStyledDocument();
        String testLine = testServer.getLineFormatter().getLatestLine(testDoc); // "<testUser> line # 19"

        assertTrue("Last line should line # 19 but it was" + testLine, testLine.endsWith("line # 19"));

        assertTrue(
                "First line should be line # 10 but it was " + testServer.getChannelTextPane().getText().split("\n")[0],
                testServer.getChannelTextPane().getText().split("\n")[0].endsWith("line # 10"));
        assertSame("Channel line count should equal the line limit", serverLinesLimit, serverLinesCount - 1);
    }

    @Test
    public void _emojiMessage()
    {
        // test display of emojis in text
        String rawMessage =
                ":sd!~discord@user/sd PRIVMSG #somechannel :02<textwithEMOJI 🇦🇺> this should show a flag";
        // TODO create this test
    }

    @Test
    public void urlInMessage() throws BadLocationException, InterruptedException
    {
        // test displaying urls

        String rawMessage = ":someuser!~someuser@urchatclient PRIVMSG #somechannel :https://google.com";
        // String rawMessage2 = "https://duckduckgo.com/?q=irc+urchat&kp=1&t=h_&ia=web";

        Message testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);
        StyledDocument testDoc = testChannel.getChannelTextPane().getStyledDocument();

        while (testChannel.messageQueueWorking())
        {
            TimeUnit.SECONDS.sleep(1);
        }

        String testLine = testChannel.getLineFormatter().getLatestLine(testDoc); // "[0629] <someuser>
                                                                                 // https://google.com"
        // Should be urlStyle, i.e a clickable link
        assertEquals("urlStyle",
                testChannel.getLineFormatter().getStyleAtPosition(testDoc, 19, testLine).getAttribute("name"));
    }

    @Test
    public void channelRegex()
    {
        // find and match against any URLs that may be in the text
        String line = "join #urchatclient to test the regex";

        Pattern pattern = Pattern.compile(Constants.CHANNEL_REGEX);
        Matcher matcher = pattern.matcher(line);


        String regexLine = "";
        while (matcher.find())
        {
            regexLine = matcher.group(1);
        }

        assertTrue(regexLine.equalsIgnoreCase("#urchatclient"));
    }

    @Test(groups = {"Test #005"})
    public void channelInMessage() throws BadLocationException, InterruptedException
    {
        Reporter.log("No dependency, this will pass regardless of the failed testChannelLineLimit method.");

        // test displaying channel
        Message testMessage =
                testHandler.new Message(":someuser!~someuser@urchatclient PRIVMSG #somechannel :first line");
        testHandler.parseMessage(testMessage);

        String rawMessage =
                ":someuser!~someuser@urchatclient PRIVMSG #somechannel :Please join #urchatclient and go to https://github.com/matty-r/urChat then go back to #anotherchannel";

        testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);
        StyledDocument testDoc = testChannel.getChannelTextPane().getStyledDocument();

        while (testChannel.messageQueueWorking())
        {
            TimeUnit.SECONDS.sleep(1);
        }

        String testLine = testChannel.getLineFormatter().getLatestLine(testDoc);
        // Should be channel, i.e clickable name which allows you to join the channel
        assertEquals("channelStyle",
                testChannel.getLineFormatter().getStyleAtPosition(testDoc, 33, testLine).getAttribute("name"));
        assertEquals("urlStyle",
                testChannel.getLineFormatter().getStyleAtPosition(testDoc, 58, testLine).getAttribute("name"));
        assertEquals("channelStyle",
                testChannel.getLineFormatter().getStyleAtPosition(testDoc, 110, testLine).getAttribute("name"));
    }
}
