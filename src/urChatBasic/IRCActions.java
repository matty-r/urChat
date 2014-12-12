package urChatBasic;

public interface IRCActions {
	
	public void renameUser(String oldUserName,String newUserName);
	
	public String getServer();
	
	public String getName();
	
}
