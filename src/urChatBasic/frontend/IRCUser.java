package urChatBasic.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import urChatBasic.base.IRCServerBase;

public class IRCUser implements Comparable<IRCUser>
{
    private String name;
    private String userStatus = "";
    public UserPopUp myMenu;
    private Boolean muted = false;
    private IRCServerBase myServer;

    public IRCUser(IRCServerBase server, String name)
    {
        this.name = name;
        this.myServer = server;

        if (name.startsWith("@"))
            setUserStatus("@");
        else if (name.startsWith("+"))
            setUserStatus("+");
        else if (name.startsWith("&"))
            setUserStatus("&");
        else if (name.startsWith("%"))
            setUserStatus("%");
    }

    public void createPopUp()
    {
        this.myMenu = new UserPopUp();
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name.replace(userStatus, "");
    }

    @Override
    public String toString()
    {
        return getName();
    }

    public String getUserStatus()
    {
        return this.userStatus;
    }

    public void setUserStatus(String c)
    {
        userStatus = c;
    }


    public class UserPopUp extends JPopupMenu
    {
        /**
         *
         */
        private static final long serialVersionUID = -4268923922705929184L;
        JMenuItem nameItem;
        JMenuItem privateMessageItem;
        JMenuItem whoIsItem;
        JMenuItem muteItem;

        public UserPopUp()
        {
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
        public void actionPerformed(ActionEvent arg0)
        {
            if (!isMuted())
                myServer.addToPrivateChannels(IRCUser.this);
        }
    }

    private class StartWhoIsQuery implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            myServer.sendClientText("/whois " + IRCUser.this.getName(), "Server");
        }
    }

    public Boolean isMuted()
    {
        return this.muted;
    }

    public void setMuted(Boolean mute)
    {
        this.muted = mute;
    }

    private class ToggleMute implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            IRCUser.this.setMuted(!muted);
        }
    }

    /**
     * Leave this to compare to the name directly (not with getName()). This will allow channel ops to
     * be sorted to the top correctly.
     */
    @Override
    public int compareTo(IRCUser comparison)
    {
        return name.compareTo(comparison.name);
    }

    public String getServer()
    {
        return myServer.getName();
    }

}
