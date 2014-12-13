package urChatBasic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class IRCUser implements Comparable<IRCUser>{
	private String name;
	private String userStatus = "";
	public UserPopUp myMenu;
	private Boolean muted = false;
	
	//IRCServer (Owner)
	private IRCServer myServer;
	
	public IRCUser(IRCServer serverName,String name){
		this.name = name;
		this.myServer = serverName;
		this.myMenu = new UserPopUp();
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name.replace(userStatus,"");
	}
	
	@Override
	public String toString(){
		return this.name;
	}
	
	public String getUserStatus(){
		return this.userStatus;
	}
	
	public void setUserStatus(String c){
		userStatus = c;
	}
	
	
   class UserPopUp extends JPopupMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4268923922705929184L;
		JMenuItem nameItem;
		JMenuItem privateMessageItem;
		JMenuItem whoIsItem;
		JMenuItem muteItem;
	    public UserPopUp(){
	    	nameItem = new JMenuItem(IRCUser.this.getName());
	        add(nameItem);
	        addSeparator();
	        privateMessageItem = new JMenuItem("Private Message");
	        privateMessageItem.addActionListener(new StartPrivateMessage());
	        add(privateMessageItem);
	        whoIsItem = new JMenuItem("Whois");
	        whoIsItem.addActionListener(new StartWhoIsQuery());
	        add(whoIsItem);
	        muteItem = new JMenuItem("Toggle Mute");
	        muteItem.addActionListener(new ToggleMute());
	        add(muteItem);
	    }
	}
   
	private class StartPrivateMessage implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(!isMuted())
				myServer.addToPrivateRooms(IRCUser.this.getName());
		}   
   }
	
	private class StartWhoIsQuery implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			myServer.sendClientText("/whois "+IRCUser.this.getName(), "Server");
		}   
   }
	
	public Boolean isMuted(){
		return this.muted;
	}
	
	public void setMuted(Boolean mute){
		this.muted = mute;
	}
	
	private class ToggleMute implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			IRCUser.this.setMuted(!muted);
		}   
	}
	
	/**
	 * Leave this to compare to the name directly (not with getName()). This
	 * will allow channel ops to be sorted to the top correctly.
	 */
	@Override
	public int compareTo(IRCUser comparison) {
		return name.compareTo(comparison.name);
	}

	public String getServer() {
		return myServer.getName();
	}

}
