package urChatBasic;

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
		JMenuItem anItem;
	    public ListPopUp(){
	        anItem = new JMenuItem(IRCUser.this.getName());
	        add(anItem);
	    }
	}
	

	/**
	 * Leave this to compare to the name directly. This
	 * will allow channel ops to be sorted to the top 
	 * correctly.
	 */
	@Override
	public int compareTo(IRCUser comparison) {
		return name.compareTo(comparison.name);
	}

}
