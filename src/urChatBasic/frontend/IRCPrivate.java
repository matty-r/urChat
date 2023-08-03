package urChatBasic.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import urChatBasic.base.IRCRoomBase;

public class IRCPrivate extends IRCRoomBase
{
    /**
     *
     */
    private static final long serialVersionUID = -7861645386733494089L;
    ////////////////
    // GUI ELEMENTS//
    ////////////////


    public IRCPrivate(IRCServer server, IRCUser user)
    {
        super(server, user.getName());
    }


    private class SendTextListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            if (!clientTextBox.getText().trim().isEmpty())
            {
                String messagePrefix = "";
                if (!clientTextBox.getText().startsWith("/"))
                    messagePrefix = "/msg " + getName() + " ";
                server.sendClientText(messagePrefix + clientTextBox.getText(), getName());
            }
            clientTextBox.setText("");
        }
    }
}
