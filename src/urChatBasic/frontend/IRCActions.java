package urChatBasic.frontend;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.UIManager;
import urChatBasic.base.IRCActionsBase;
import urChatBasic.base.IRCChannelBase;

public class IRCActions implements IRCActionsBase {

    // IRCActions stuff
    private boolean wantsAttention = false;
    private Timer wantsAttentionTimer = new Timer(0, new FlashTab());
    private Color originalColour;
    protected UserGUI gui = DriverGUI.gui;
    protected IRCChannelBase ircChannel;

    public IRCActions(IRCChannelBase ircChannel)
    {
        this.ircChannel = ircChannel;
        // originalColour = ircChannel.getBackground();

        for(int i = 0; i < gui.tabbedPane.getTabCount(); i++)
        {
            if(gui.tabbedPane.getComponentAt(i) == ircChannel)
            {
                originalColour = gui.tabbedPane.getBackgroundAt(i);
                break;
            }
        }
    }

    private class FlashTab implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            Component selectedComponent = gui.tabbedPane.getSelectedComponent();
            int tabIndex = gui.tabbedPane.indexOfComponent(ircChannel);

            if (tabIndex >= 0 && wantsAttention && selectedComponent != ircChannel)
            {
                ircChannel.getUserTextBox().requestFocus();

                if (gui.tabbedPane.getBackgroundAt(tabIndex) == UIManager.getColor("CheckBoxMenuItem.selectionBackground"))
                {
                    gui.tabbedPane.setBackgroundAt(tabIndex, originalColour);
                } else
                {
                    gui.tabbedPane.setBackgroundAt(tabIndex, UIManager.getColor("CheckBoxMenuItem.selectionBackground"));
                }

                // repaint();
            } else
            {
                if(null != selectedComponent && tabIndex >= 0)
                {
                    gui.tabbedPane.setBackgroundAt(tabIndex, originalColour);
                }
                wantsAttentionTimer.stop();
            }
        }
    }

    @Override
    public void callForAttention()
    {
        wantsAttentionTimer.setDelay(1000);
        wantsAttention = true;

        if (!(wantsAttentionTimer.isRunning()))
            wantsAttentionTimer.start();
    }
}
