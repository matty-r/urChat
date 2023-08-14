package urChatBasic.frontend.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import urChatBasic.base.DialogBase;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.components.FontPanel;

public class FontDialog extends DialogBase {
    private FontPanel fontPanel;

    public FontDialog(String title)
    {
        super(DriverGUI.frame, title, true);
        setSize(600, 100);
        setResizable(false);
        setMaximumSize(new Dimension(600, 100));
        setLocationRelativeTo(super.getParent());


        dialogPanel = new JPanel(new BorderLayout());
        fontPanel = new FontPanel(dialogPanel);
        dialogPanel.add(fontPanel);

        add(dialogPanel);
    }

}
