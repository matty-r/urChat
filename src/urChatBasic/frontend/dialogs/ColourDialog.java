package urChatBasic.frontend.dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import urChatBasic.base.DialogBase;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.components.ColourPanel;

public class ColourDialog extends DialogBase
{
    private ColourPanel colourPanel;

    public ColourDialog(String title, Preferences settingsPath)
    {
        super(DriverGUI.frame, title, true);
        initColourDialog(settingsPath);
    }

    public void initColourDialog(Preferences settingsPath)
    {
        setSize(600, 600);
        setResizable(false);
        setMaximumSize(new Dimension(600, 600));
        setLocationRelativeTo(super.getParent());

        colourPanel = new ColourPanel();

        add(colourPanel);
    }

    // @Override
    // public void setVisible(boolean b)
    // {
    //     fontPanel.loadFont();
    //     super.setVisible(b);
    // }

    // public void addSaveListener(ActionListener newActionListener)
    // {
    //     // fontPanel.getSaveButton().addActionListener(newActionListener);
    //     fontPanel.addActionListener(fontPanel.getSaveButton(), newActionListener);
    // }

    // public void addResetListener(ActionListener newActionListener)
    // {
    //     fontPanel.getResetButton().addActionListener(newActionListener);
    // }

    public class ShowColourDialog implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            // fontPanel.loadFont();
            ColourDialog.this.setVisible(true);
        }
    }
}
