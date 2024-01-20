package urChatBasic.base;

import java.io.IOException;


public interface ConnectionBase extends Runnable
{

    public abstract Boolean isConnected ();

    public abstract IRCServerBase getServer ();

    public abstract void sendClientText (String clientText, String fromChannel) throws IOException;

    public abstract void run ();

    public abstract void disconnect ();

    public abstract void reconnect ();

    public abstract void setPingReceived ();
}
