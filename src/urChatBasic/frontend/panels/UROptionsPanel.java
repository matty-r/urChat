package urChatBasic.frontend.panels;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import urChatBasic.frontend.components.MainOptionsPanel;

public class UROptionsPanel extends JPanel
{
    public String panelDisplayName;
    private JScrollPane panelScroller;

    public UROptionsPanel (String displayName, MainOptionsPanel optionsPanel)
    {
        panelDisplayName = displayName;
        panelScroller = new JScrollPane(this);
        optionsPanel.addToOptions(displayName, panelScroller);
    }
}
