package urChatBasic.frontend;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCRoomBase;

public class IRCPrivate extends IRCRoomBase
{
    /**
     *
     */
    private static final long serialVersionUID = -7861645386733494089L;

    public IRCPrivate(IRCServer server, IRCUser user)
    {
        super(server, user.getName());
        hideUsersList();
        hideEventTicker();
        clientTextBox.addActionListener(new SendTextListener());

        URL imgPath = null;
        try
        {
            imgPath =  new URL(Constants.IMAGES_DIR + "User.png");
            icon = new ImageIcon(imgPath);
        } catch (IOException e)
        {
            Constants.LOGGER.warn( "COULD NOT LOAD User.png " + e.getLocalizedMessage());
        }
    }

    /**
     * Private channel doesn't need the User List, so don't set it up.
     */
    @Override
    protected void setupMainPanel()
    {
        mainPanel.setLayout(new BorderLayout());
        setupMainTextArea();
        mainPanel.add(channelScroll, BorderLayout.CENTER);
        setupBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private class SendTextListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            if (!clientTextBox.getText().trim().isEmpty())
            {
                if (!clientTextBox.getText().startsWith("/"))
                {
                    String messagePrefix = "/msg " + getName() + " ";
                    clientTextBox.setText(messagePrefix + clientTextBox.getText());
                }
            }
        }
    }
}
