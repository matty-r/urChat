package urChatBasic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class IRCUser implements Comparable<IRCUser>{
	private String name;
	private String userStatus = "";
	public ListPopUp myMenu;
	
	public IRCUser(String name){
		this.name = name;
		this.myMenu = new ListPopUp();
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
	
	
   class ListPopUp extends JPopupMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4268923922705929184L;
		JMenuItem nameItem;
		JMenuItem sendMessageItem;
	    public ListPopUp(){
	    	nameItem = new JMenuItem(IRCUser.this.getName());
	        add(nameItem);
	        addSeparator();
	        sendMessageItem = new JMenuItem("Private Message");
	        sendMessageItem.addActionListener(new ClickPopUpItem());
	        add(sendMessageItem);
	    }
	}
   
	private class ClickPopUpItem implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			DriverGUI.gui.addPrivateRooms(IRCUser.this);
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

}
