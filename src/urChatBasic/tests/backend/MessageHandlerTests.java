package urChatBasic.tests.backend;

import org.junit.Before;
import org.junit.Test;
import urChatBasic.backend.Connection;
import urChatBasic.backend.MessageHandler;
import urChatBasic.backend.MessageHandler.Message;
import urChatBasic.base.IRCRoomBase;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.IRCServer;
import urChatBasic.frontend.IRCUser;
import urChatBasic.frontend.UserGUI;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

public class MessageHandlerTests {
    MessageHandler testHandler;
    IRCServer testServer;
    UserGUI testGUI;
    IRCRoomBase testChannel;
    IRCUser testUser;
    Connection testConnection;

    // helper functions
    private String getLatestLine(StyledDocument doc)
    {
        int length = doc.getLength();

        if (length == 0) {
            return ""; // Return an empty string if the document is empty
        }

        try {
            int start = doc.getText(0, length).trim().lastIndexOf('\n') + 1;
            int end = length;

            if (end == -1) {
                end = length;
            }

            return doc.getText(start, end - start).trim();
        } catch (BadLocationException e) {
            e.printStackTrace();
            return "";
        }
    }


    @Before
    public void setUp() throws Exception {
        DriverGUI.createGUI();
        testGUI = DriverGUI.gui;
        testServer = new IRCServer("testServer", "testUser", "testUser", "testPassword", "1337", true, "testProxy", "1234", true);
        testUser = new IRCUser(testServer, "testUser");
        testServer.addToPrivateRooms(testUser);
        testChannel = testServer.getCreatedPrivateRoom(testUser.toString());
        testHandler = new MessageHandler(testServer);
        testConnection = new Connection(testServer);
    }

    @Test
    public void noticeMessageParseTest()
    {
        String rawMessage = ":ChanServ!ChanServ@services.libera.chat NOTICE userName :[#somechannel] Welcome to #someChannel.";
        Message testMessage = testHandler.new Message(rawMessage);

        assertEquals("#somechannel", testMessage.getChannel());
        // assertEquals("Welcome to #somechannel.", testMessage.getBody());
    }

    @Test
    public void recvActionMessage()
    {
        String rawMessage = ":"+testUser+"!~"+testUser+"@userHost PRIVMSG "+testUser+" :ACTION claps hands";
        Message testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);
        assertEquals("> claps hands", testMessage.getBody());
    }

    @Test
    public void nickIsHighStyleTest() throws BadLocationException
    {
        String rawMessage = ":someuser!~someuser@urchatclient PRIVMSG testUser :hello testUser!";
        Message testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);
        StyledDocument testDoc = testChannel.getChannelTextPane().getStyledDocument();
        String testLine = testChannel.getLineFormatter().getLatestLine(testDoc); // "[0629] <someuser> hello testUser!"

        // Should be highStyle because someuser mentioned my nick, testUser
        assertEquals("highStyle", testChannel.getLineFormatter().getStyleAtPosition(testDoc, 11, testLine).getAttribute("name"));
    }

    @Test
    public void nickIsDefaultStyleTest() throws BadLocationException
    {
        String rawMessage = ":someuser!~someuser@urchatclient PRIVMSG #somechannel :Welcome to somechannel!";
        Message testMessage = testHandler.new Message(rawMessage);
        testHandler.parseMessage(testMessage);
        StyledDocument testDoc = testChannel.getChannelTextPane().getStyledDocument();
        String testLine = testChannel.getLineFormatter().getLatestLine(testDoc); // "[0629] <someuser> hello world!"

        // Should be defaultStyle because the user didn't mention testUser and is just a normal message
        assertEquals("defaultStyle", testChannel.getLineFormatter().getStyleAtPosition(testDoc, 11, testLine).getAttribute("name"));
    }

    @Test
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
    }

    @Test
    public void sendActionMessageUser()
    {
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

    @Test
    public void noticeMessage2()
    {
        String rawMessage = ":channeluser!channelname@channelname/bot/primary NOTICE myUsername :this is just some notice message directed to this user from the ";
        Message testMessage = testHandler.new Message(rawMessage);

        assertEquals("#channelname", testMessage.getChannel());
        // TODO create this test
    }

    @Test
    public void handleChannelUrl()
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

    @Test
    public void emojiMessage()
    {
        // test display of emojis in text
        String rawMessage = ":sd!~discord@user/sd PRIVMSG #somechannel :02<textwithEMOJI 🇦🇺> this should show a flag";
        // TODO create this test
    }

    @Test
    public void urlMessage()
    {
        // test displaying urls
        String rawMessage = "https://i.imgur.com/somepicture.png";
        String rawMessage2 = "https://duckduckgo.com/?q=irc+urchat&kp=1&t=h_&ia=web";
        // TODO create this test
    }
}
