package urChatBasic.frontend;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.Timer;
import urChatBasic.base.IRCActionsBase;

public class IRCActions implements IRCActionsBase {

    // IRCActions stuff
    private boolean wantsAttention = false;
    private Timer wantsAttentionTimer = new Timer(0, new FlashTab());
    private Color originalColor;
    protected UserGUI gui = DriverGUI.gui;
    protected JPanel ircPanel;

    public IRCActions(JPanel ircPanel)
    {
        this.ircPanel = ircPanel;
    }

    private class FlashTab implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            Component selectedComponent = gui.tabbedPane.getSelectedComponent();
            int tabIndex = gui.tabbedPane.indexOfComponent(ircPanel);

            if (ircPanel.wantsAttention() && selectedComponent != ircPanel)
            {
                clientTextBox.requestFocus();

                if (gui.tabbedPane.getBackgroundAt(tabIndex) == Color.red)
                {

                    gui.tabbedPane.setBackgroundAt(tabIndex, IRCChannel.this.originalColor);
                } else
                {
                    gui.tabbedPane.setBackgroundAt(tabIndex, Color.red);
                }

                repaint();
            } else
            {
                gui.tabbedPane.setBackgroundAt(tabIndex, IRCChannel.this.originalColor);
                wantsAttentionTimer.stop();
            }
        }
    }
}
