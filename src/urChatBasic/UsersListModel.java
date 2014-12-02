package urChatBasic;

import java.util.ArrayList;
import java.util.Collections;
import javax.swing.AbstractListModel;

public class UsersListModel extends AbstractListModel{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrayList<IRCUser> users;

    public UsersListModel(ArrayList<IRCUser> array){
    	users = array;
    }

    public int getSize(){
        return users.size();
    }

    public Object getElementAt(int index){
        return (IRCUser)users.get(index);
    }

    public ArrayList<IRCUser> getSongList(){
        return users;
    }

    public void setList(ArrayList<IRCUser> array){
        this.users = array;
    }

    public void getSortedList(ArrayList<IRCUser> array){
        Collections.sort(array);
        users = array;
    }
}
