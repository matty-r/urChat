package urChatBasic.base;

import urChatBasic.backend.MessageHandler.Message;

public interface MessageHandlerBase
{
    void messageExec(Message myMessage);
}
