package urChatBasic.frontend.dialogs;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import urChatBasic.base.DialogBase;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.components.ColourPanel;

public class ColourDialog extends DialogBase
{
    private ColourPanel colourPanel;

    public ColourDialog(String title, Font defaultFont, Preferences settingsPath)
    {
        super(DriverGUI.frame, title, true);
        initColourDialog(defaultFont, settingsPath);
    }

    public void initColourDialog(Font defaultFont, Preferences settingsPath)
    {
        colourPanel = new ColourPanel(defaultFont, settingsPath);

        add(colourPanel);
        setContentPane(colourPanel);
        pack();

        // setSize(600, 600);
        setResizable(false);
        // setMaximumSize(new Dimension(600, 600));
        setLocationRelativeTo(super.getParent());
    }

    @Override
    public void setVisible(boolean b)
    {
        setLocationRelativeTo(super.getParent());

        super.setVisible(b);
    }

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
