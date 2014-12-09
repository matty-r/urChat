package urChatBasic;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import javax.swing.DefaultListModel;

@SuppressWarnings("rawtypes")
public class UsersListModel extends DefaultListModel{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//ArrayList<IRCUser> users;
	List users = new ArrayList<IRCUser>();

    public UsersListModel(List<IRCUser> array){
    	users = array;
    }

    public int getSize(){
        return users.size();
    }

    public IRCUser getElementAt(int index){
        return (IRCUser) users.get(index);
    }

    public List<IRCUser> getUsersList(){
        return users;
    }

    public void setList(ArrayList<IRCUser> array){
        this.users = array;
    }

    public void getSortedList(ArrayList<IRCUser> array){
        Collections.sort(array);
        users = array;
    }
    
    public void sort(){
        Collections.sort(users);
        fireContentsChanged(this, 0, users.size());
    }
}
