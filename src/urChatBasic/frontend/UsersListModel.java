package urChatBasic.frontend;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import javax.swing.AbstractListModel;

@SuppressWarnings("rawtypes")
public class UsersListModel extends AbstractListModel
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // ArrayList<IRCUser> users;
    List<IRCUser> users = new ArrayList<IRCUser>();

    public int getSize()
    {
        return users.size();
    }

    public IRCUser getElementAt(int index)
    {
        return (IRCUser) users.get(index);
    }

    public void addUser(IRCUser newUser)
    {
        users.add(newUser);
    }

    public void removeUser(String userName)
    {
        users.removeIf(n -> n.getName().equalsIgnoreCase(userName));
    }

    public void removeUser(IRCUser targetUser)
    {
        users.remove(targetUser);
    }

    public boolean hasUser(String userName)
    {
        return users.stream().filter(user -> user.getName().equalsIgnoreCase(userName)).findFirst().isPresent();
    }

    public boolean hasUser(IRCUser targetUser)
    {
        return users.stream().filter(user -> user.equals(targetUser)).findFirst().isPresent();
    }

    public List<IRCUser> getUsersList()
    {
        return users;
    }

    public void setList(ArrayList<IRCUser> array)
    {
        this.users = array;
    }

    public void getSortedList(ArrayList<IRCUser> array)
    {
        Collections.sort(array);
        users = array;
    }

    public void sort()
    {
        Collections.sort(users);
        fireContentsChanged(this, 0, users.size());
    }
}
