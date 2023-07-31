package urChatBasic.frontend;

public interface IRCActions
{

    // Will be used to ensure other classes contain these methods.
    public String getServer();
    public String getName();
    public void callForAttention();
    public boolean wantsAttention();
}
