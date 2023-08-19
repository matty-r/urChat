package urChatBasic.tests.backend;

import org.junit.Before;
import org.junit.Test;
import urChatBasic.backend.MessageHandler;
import urChatBasic.backend.MessageHandler.Message;

import static org.junit.Assert.assertEquals;

public class MessageHandlerTests {
    MessageHandler testHandler;


    @Before
    public void setUp() throws Exception {
        testHandler = new MessageHandler(null);
    }

    @Test
    public void noticeMessageParseTest()
    {
        String rawMessage = ":ChanServ!ChanServ@services.libera.chat NOTICE userName :[#somechannel] Welcome to #someChannel.";
        Message testMessage = testHandler.new Message(testHandler, rawMessage);

        assertEquals("#somechannel", testMessage.getChannel());
        // assertEquals("Welcome to #somechannel.", testMessage.getBody());
    }

    @Test
    public void noticeMessage2()
    {
        String rawMessage = ":channeluser!channelname@channelname/bot/primary NOTICE myUsername :this is just some notice message directed to this user from the ";
        Message testMessage = testHandler.new Message(testHandler, rawMessage);

        assertEquals("#channelname", testMessage.getChannel());
    }

    @Test
    public void handleChannelUrl()
    {
        String rawMessage = ":services. 328 userName #somechannel :https://somechannel.com/url";
        Message testMessage = testHandler.new Message(testHandler, rawMessage);

        assertEquals(MessageHandler.NoticeMessage.class, testMessage.getMessageBase().getClass());
    }

    @Test
    public void testQuitChannel1()
    {
        String rawMessage = ":userName!~userName@user/userName QUIT :Read error: Connection reset by peer";
        Message testMessage = testHandler.new Message(testHandler, rawMessage);

        assertEquals(MessageHandler.DisconnectMessage.class, testMessage.getMessageBase().getClass());
    }

    @Test
    public void testQuitChannel2()
    {
        String rawMessage = ":userName!~userName@user/userName QUIT :Remote host closed the connection";
        Message testMessage = testHandler.new Message(testHandler, rawMessage);

        assertEquals(MessageHandler.DisconnectMessage.class, testMessage.getMessageBase().getClass());
    }

    @Test
    public void emojiMessage()
    {
        String rawMessage = ":sd!~discord@user/sd PRIVMSG #reddit-sysadmin :02<username 🇦🇺> I know they were not new then, lol";
    }

    @Test
    public void urlMessage()
    {
        String rawMessage = "https://i.imgur.com/OcAYX2l.png";
        String rawMessage2 = "https://www.google.com/search?q=where%27s%20the%20mariadb%20socket";
    }
}
