package urChatBasic.frontend.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import urChatBasic.backend.utils.URStyle;
import urChatBasic.base.DialogBase;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.components.ColourPanel;

public class ColourDialog extends DialogBase
{
    private ColourPanel colourPanel;

    public ColourDialog(URStyle targetStyle, Preferences settingsPath)
    {
        super(DriverGUI.frame, targetStyle.getName(), true);
        initColourDialog(targetStyle, settingsPath);
    }

    public void initColourDialog(URStyle targetStyle, Preferences settingsPath)
    {
        colourPanel = new ColourPanel(targetStyle, settingsPath);

        add(colourPanel);
        setContentPane(colourPanel);
        pack();

        // setSize(600, 600);
        setResizable(false);
        // setMaximumSize(new Dimension(600, 600));
        setLocationRelativeTo(super.getParent());

        colourPanel.addSaveListener(e -> {
            ColourDialog.this.setVisible(false);
            System.out.println("Set visible false");
        });
    }

    public ColourPanel getColourPanel()
    {
        return colourPanel;
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
