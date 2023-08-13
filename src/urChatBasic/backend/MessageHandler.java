package urChatBasic.backend;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCServerBase;
import urChatBasic.base.MessageBase;
import urChatBasic.base.UserGUIBase;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.base.capabilities.SaslCapSubTypes;
import urChatBasic.frontend.DriverGUI;

/**
 * This class will Handle the message it has received and assign an approriate class that will parse
 * the string and then
 *
 * @author Matt
 *
 */
public class MessageHandler
{
    Set<IDRange> rangeIDs = new HashSet<IDRange>();
    Set<IDSingle> singleIDs = new HashSet<IDSingle>();

    IRCServerBase serverBase;
    UserGUIBase gui = DriverGUI.gui;
    public String creationTime = (new Date()).toString();

    /**
     * Assign the correct
     *
     * @param server
     * @param messageID
     */
    public MessageHandler(IRCServerBase server)
    {
        this.serverBase = server;

        if (rangeIDs.isEmpty())
            addRanges();
        if (singleIDs.isEmpty())
            addSingles();
    }

    public void parseMessage(Message receivedMessage)
    {
        receivedMessage.messageBase.messageExec(receivedMessage);
    }

    private Boolean isBetween(String line, char start, String middle, char end)
    {
        int startIndex = line.indexOf(start);
        int middleIndex = line.indexOf(middle);
        int endIndex = line.substring(startIndex + 1).indexOf(end) + startIndex;

        if (startIndex >= 0 && middleIndex >= 0 && endIndex >= 0)
            if (middleIndex > startIndex && middleIndex < endIndex)
                return true;

        return false;
    }

    public class IDRange
    {
        private int min, max;
        private MessageBase handlerType;
        private MessageIdType type = MessageIdType.NUMBER_ID;

        public IDRange(int min, int max, MessageBase handlerType)
        {
            this.min = min;
            this.max = max;
            this.handlerType = handlerType;
        }

        public boolean inRange(int checkNumber)
        {
            if (checkNumber >= this.min && checkNumber <= this.max)
                return true;
            else
                return false;
        }
    }

    public class IDSingle
    {
        private String id;
        private int[] idArray;
        private MessageBase handlerType;
        private MessageIdType type;

        public IDSingle(int id, MessageBase handlerType)
        {
            this.idArray = new int[] {id};
            this.handlerType = handlerType;
            type = MessageIdType.NUMBER_ID;
        }

        public IDSingle(int[] id, MessageBase handlerType)
        {
            this.idArray = id;
            this.handlerType = handlerType;
            type = MessageIdType.NUMBER_ID;
        }

        public IDSingle(String id, MessageBase handlerType)
        {
            this.id = id;
            this.handlerType = handlerType;
            type = MessageIdType.STRING_ID;
        }

        public boolean isEqual(String testId)
        {
            try
            {
                if (Integer.parseInt(testId) > 0)
                    return isEqual(Integer.parseInt(testId));
            } catch (Exception e)
            {
                return id.equals(testId);
            }
            return false;
        }

        public boolean isEqual(int testId)
        {
            for (int x : idArray)
                if (x == testId)
                {
                    return true;
                }

            return false;
        }
    }

    private void addRanges()
    {
        rangeIDs.add(new IDRange(1, 4, new UserRegistrationMessage()));
        rangeIDs.add(new IDRange(332, 333, new ChannelTopicMessage()));
        rangeIDs.add(new IDRange(412, 415, new BadPrivateMessage()));
        rangeIDs.add(new IDRange(371, 376, new GeneralServerMessage()));
        rangeIDs.add(new IDRange(251, 256, new GeneralServerMessage()));
        rangeIDs.add(new IDRange(471, 475, new JoinFailureMessage()));
    }

    private void addSingles()
    {
        singleIDs.add(new IDSingle(5, new NoticeMessage()));
        singleIDs.add(new IDSingle(353, new UsersListMessage()));
        singleIDs.add(new IDSingle(322, new CommandResponseMessage()));
        singleIDs.add(new IDSingle((new int[] {311, 319, 312, 317, 318, 301, 671, 330, 338, 378}), new WhoIsMessage()));
        singleIDs.add(new IDSingle((new int[] {265, 266, 250, 328, 477, 331, 900}), new GeneralServerMessage()));
        singleIDs.add(new IDSingle(366, new GeneralChannelMessage()));
        singleIDs.add(new IDSingle((new int[] {432, 433, 451}), new InvalidNickMessage()));
        singleIDs.add(new IDSingle(403, new NoSuchChannelMessage()));
        singleIDs.add(new IDSingle(461, new NotEnoughParametersMesssage()));
        singleIDs.add(new IDSingle(903, new SASLAuthenticateSuccessMessage()));
        singleIDs.add(new IDSingle(904, new SASLAuthenticateFailedMessage()));
        singleIDs.add(new IDSingle("CAP", new CapabilityMessage()));
        singleIDs.add(new IDSingle("AUTHENTICATE", new SASLAuthenticateMessage()));
        singleIDs.add(new IDSingle("MODE", new ModeMessage()));
        singleIDs.add(new IDSingle("NOTICE", new NoticeMessage()));
        singleIDs.add(new IDSingle("PRIVMSG", new PrivateMessage()));
        singleIDs.add(new IDSingle("INVITE", new PrivateMessage()));
        singleIDs.add(new IDSingle("PART", new PartMessage()));
        singleIDs.add(new IDSingle("KICK", new KickMessage()));
        singleIDs.add(new IDSingle("JOIN", new JoinMessage()));
        singleIDs.add(new IDSingle(":Closing", new DisconnectMessage()));
        singleIDs.add(new IDSingle("QUIT", new DisconnectMessage()));
        singleIDs.add(new IDSingle("NICK", new RenameUserMessage()));
    }

    public enum MessageIdType
    {
        NUMBER_ID, STRING_ID
    }

    public class Message
    {
        String prefix = "";
        String idCommand = "";
        int idCommandNumber;
        String channel = "";
        String body = "";
        MessageIdType type;
        String rawMessage = "";
        String nick = "";
        String subType  = "";
        MessageBase messageBase;
        MessageHandler messageHandler;

        public Message(MessageHandler handler, String fullMessage)
        {
            this.rawMessage = fullMessage;
            this.messageHandler = handler;
            setPrefix();
            try{
                setChannel();
                setIdCommand();
                setMessageBody();
                setNick();
                setSubType();
            } catch (Exception e) {
                this.channel = "";
            }

            try
            {
                this.idCommandNumber = Integer.parseInt(this.idCommand);
                this.type = MessageIdType.NUMBER_ID;
            } catch (Exception e)
            {
                this.type = MessageIdType.STRING_ID;

                // requires special handling
                if(channel.isEmpty() && nick.isEmpty() && subType.isEmpty())
                {
                    idCommand = prefix;
                }
            }

            setHandler();
        }

        public String toString()
        {
            return rawMessage;
        }


        private void setPrefix()
        {
            prefix = rawMessage.split(" ")[0];
        }

        private void setNick()
        {
            if (isBetween(rawMessage, ':', "!", '@'))
                this.nick = rawMessage.substring(1, rawMessage.indexOf("!")).trim();
        }

        private void setChannel()
        {
            String withoutPrefix = rawMessage.replace(prefix, "").trim();
            // int messageBegin = posnOfOccurrence(withoutPrefix, Constants.SPACES_AHEAD_DELIMITER, 1);
            int messageBegin = withoutPrefix.indexOf(Constants.SPACES_AHEAD_DELIMITER);

            if(messageBegin < 0)
            {
                messageBegin = withoutPrefix.length();
            }

            int channelBegin = withoutPrefix.indexOf(Constants.CHANNEL_DELIMITER);
            if (channelBegin < messageBegin && channelBegin > -1)
                this.channel = withoutPrefix.substring(channelBegin, messageBegin).split(" ")[0].trim();
            else
                this.channel = withoutPrefix.split(" ")[1];
        }

        private void setMessageBody()
        {
            try
            {
                String withoutPrefixIdChannel = rawMessage.replace(prefix, "").trim();

                if (withoutPrefixIdChannel.indexOf(":") > -1)
                    this.body = withoutPrefixIdChannel.substring(withoutPrefixIdChannel.indexOf(":") + 1).trim();
                else
                    this.body = withoutPrefixIdChannel;
            } catch (IndexOutOfBoundsException e)
            {
                Constants.LOGGER.log(Level.SEVERE,
                        "Failed to extract a message from received text. " + e.getLocalizedMessage());
            }
        }

        // TODO: This should be improved. Just flatten all the ids into a single list.
        private void setHandler()
        {
            boolean handled = false;

            if (!handled)
                for (IDSingle testSingle : singleIDs)
                    if (testSingle.type.equals(MessageIdType.NUMBER_ID) && testSingle.isEqual(this.idCommandNumber))
                    {
                        this.messageBase = testSingle.handlerType;
                        handled = true;
                        break;
                    }

            if (!handled)
                for (IDSingle testSingle : singleIDs)
                    if (testSingle.type.equals(MessageIdType.STRING_ID) && testSingle.isEqual(this.idCommand))
                    {
                        this.messageBase = testSingle.handlerType;
                        handled = true;
                        break;
                    }

            if (!handled)
                for (IDRange testRange : rangeIDs)
                    if (testRange.type.equals(MessageIdType.NUMBER_ID) && testRange.inRange(this.idCommandNumber))
                    {
                        this.messageBase = testRange.handlerType;
                        handled = true;
                        break;
                    }

            if (!handled)
                this.messageBase = new MessageHandler.DefaultMesssage();
        }

        private void setIdCommand()
        {
            idCommand = rawMessage.split(" ")[1];
        }

        private void setSubType ()
        {
            String subType = this.rawMessage.replaceFirst(Pattern.quote(this.prefix), "");
            subType = subType.replaceFirst(Pattern.quote(this.idCommand), "");
            subType = subType.replaceFirst(Pattern.quote(this.channel), "");
            subType = subType.replaceFirst(Pattern.quote(this.body),"");
            subType = subType.trim();
            this.subType = subType.split(" ")[0];
        }
    }

    public class UserRegistrationMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            printServerText(myMessage.body);
        }

    }

    public class CommandResponseMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            printServerText(myMessage.body);
        }
    }

    public class GeneralServerMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            printServerText(myMessage.body);
        }
    }

    public class GeneralChannelMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            myMessage.messageHandler.serverBase.printChannelText(myMessage.channel, myMessage.body, myMessage.nick);
        }
    }

    public class JoinMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            if (myMessage.nick.equals(myMessage.messageHandler.serverBase.getNick()))
            {
                myMessage.messageHandler.serverBase.addToCreatedChannels(myMessage.channel);
                myMessage.messageHandler.serverBase.printEventTicker(myMessage.channel, "You have joined " + myMessage.channel);
            } else
                myMessage.messageHandler.serverBase.addToUsersList(myMessage.channel, myMessage.nick);
        }

    }

    public class ChannelTopicMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            myMessage.messageHandler.serverBase.setChannelTopic(myMessage.channel, myMessage.body);
        }
    }

    public class UsersListMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            myMessage.messageHandler.serverBase.addToUsersList(myMessage.channel, myMessage.body.split(" "));
        }
    }

    public class RenameUserMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            if (!myMessage.nick.equals(myMessage.messageHandler.serverBase.getNick()))
                myMessage.messageHandler.serverBase.renameUser(myMessage.nick, myMessage.body);
        }
    }

    public class JoinFailureMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            printServerText(myMessage.body);
        }

    }

    public class ModeMessage implements MessageBase
    {


        @Override
        public void messageExec(Message myMessage)
        {
            // printServerText(myMessage.body);
            if (myMessage.channel.equals(myMessage.messageHandler.serverBase.getNick()))
                printServerText(myMessage.body);
            else
                myMessage.messageHandler.serverBase.getCreatedChannel(myMessage.channel).createEvent(myMessage.body);
        }

    }

    public class WhoIsMessage implements MessageBase
    {


        @Override
        public void messageExec(Message myMessage)
        {
            printPrivateText(myMessage.rawMessage.split(" ")[3], myMessage.body, myMessage.rawMessage.split(" ")[3]);
        }

    }

    public class ServerChangeMessage implements MessageBase
    {


        @Override
        public void messageExec(Message myMessage)
        {
            // TODO
        }

    }

    public class BadPrivateMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            // TODO Auto-generated method stub
        }
    }

    public class SASLAuthenticateFailedMessage implements MessageBase
    {
        @Override
        public void messageExec(Message myMessage)
        {
            printServerText(myMessage.body);
        }
    }

    public class SASLAuthenticateSuccessMessage implements MessageBase
    {
        @Override
        public void messageExec(Message myMessage)
        {
            printServerText(myMessage.body);
            serverBase.saslCompleteAuthentication();
        }
    }

    public class SASLAuthenticateMessage implements MessageBase
    {
        @Override
        public void messageExec(Message myMessage)
        {
            if(myMessage.rawMessage.equals("AUTHENTICATE +"))
            {
                printServerText(myMessage.rawMessage);
                serverBase.saslSendAuthentication();
            }
        }
    }

    public class CapabilityMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            // CAP ACK or CAP LS?
            switch (myMessage.subType) {
                case "LS":
                        printServerText(myMessage.body);
                        // TODO: If client enabled SASL authentication, check the myMessage for sasl='op'
                        serverBase.setCapabilities(myMessage.body);

                        if(serverBase.hasCapability(CapabilityTypes.SASL))
                        {
                            // Did the client have a sasl type selected?
                            if(gui.authenticationType().equals(SaslCapSubTypes.PLAIN))
                            {
                                serverBase.saslRequestAuthentication();
                                break;
                            }
                        }

                        // end capability message
                        serverBase.saslCompleteAuthentication();

                        if(gui.authenticationType().equals(CapabilityTypes.NICKSERV.getType()))
                        {
                            serverBase.nickservRequestAuthentication();
                        }
                    break;
                case "ACK":
                        printServerText("Begin SASL Authentication");
                        serverBase.saslDoAuthentication();
                    break;
                default:
                    printServerText(myMessage.body);
            }
        }
    }


    public class InvalidNickMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            printServerText(myMessage.body);
        }
    }

    public class NoticeMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            if (myMessage.nick != null && myMessage.nick.toLowerCase().equals("NickServ".toLowerCase()))
            {
                printPrivateText(myMessage.nick, myMessage.body, myMessage.nick);
                gui.connectFavourites(myMessage.messageHandler.serverBase);
            } else
            {
                printServerText(myMessage.body);
            }
        }



    }

    public class PrivateMessage implements MessageBase
    {

        @Override
        public void messageExec(Message myMessage)
        {
            if (!myMessage.channel.equals(myMessage.messageHandler.serverBase.getNick()))
            {
                if (isBetween(myMessage.body, Constants.CTCP_DELIMITER, "ACTION", Constants.CTCP_DELIMITER))
                    myMessage.body = myMessage.body.replace(Constants.CTCP_DELIMITER + "ACTION", ">");
                myMessage.messageHandler.serverBase.printChannelText(myMessage.channel, myMessage.body, myMessage.nick);
            } else
            {
                // channel is myNick, so instead use the nick of the sender for the channel
                myMessage.messageHandler.serverBase.printChannelText(myMessage.nick, myMessage.body, myMessage.nick);
            }
        }


    }

    public class PartMessage implements MessageBase
    {
        @Override
        public void messageExec(Message myMessage)
        {
            if (!(myMessage.nick.equals(myMessage.messageHandler.serverBase.getNick())))
            {
                for (String tempChannel : myMessage.channel.split(","))
                    myMessage.messageHandler.serverBase.removeFromUsersList(tempChannel, myMessage.nick);
            } else
            {
                for (String tempChannel : myMessage.channel.split(","))
                {
                    myMessage.messageHandler.serverBase.removeFromUsersList(tempChannel, myMessage.messageHandler.serverBase.getNick());
                    printServerText("You quit " + tempChannel);
                }
            }
        }
    }

    public class KickMessage implements MessageBase
    {
        @Override
        public void messageExec(Message myMessage)
        {
            myMessage.messageHandler.serverBase.removeFromUsersList(myMessage.channel, myMessage.body);
        }
    }

    public class DisconnectMessage implements MessageBase
    {
        @Override
        public void messageExec(Message myMessage)
        {
            if (myMessage.messageHandler.serverBase.getNick().equals(myMessage.nick))
            {
                gui.quitServer(myMessage.messageHandler.serverBase);
            } else
                myMessage.messageHandler.serverBase.removeFromUsersList(myMessage.messageHandler.serverBase.getName(), myMessage.nick);
        }
    }

    public class NoSuchChannelMessage implements MessageBase
    {
        @Override
        public void messageExec(Message myMessage)
        {
            printServerText(myMessage.body);
        }
    }

    public class NotEnoughParametersMesssage implements MessageBase
    {
        @Override
        public void messageExec(Message myMessage)
        {
            printServerText(myMessage.body);
        }
    }

    public class DefaultMesssage implements MessageBase
    {
        @Override
        public void messageExec(Message myMessage)
        {
            printServerText(myMessage.body);
            Constants.LOGGER.log(Level.WARNING, "NOT HANDLED: " + myMessage.rawMessage);
        }
    }

    private void printPrivateText(String userName, String line, String fromUser)
    {
        serverBase.printPrivateText(userName, line, fromUser);
    }

    private void printServerText(String message)
    {
        serverBase.printServerText(message);
    }
}
