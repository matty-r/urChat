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
    private String styleName = "Default Font";
    private ColourPanel colourPanel;

    public ColourDialog(String styleName, URStyle defaultStyle, Preferences settingsPath)
    {
        super(DriverGUI.frame, defaultStyle.getName(), true);
        this.styleName = styleName;
        initColourDialog(defaultStyle, settingsPath);
    }

    public void initColourDialog(URStyle defaultStyle, Preferences settingsPath)
    {
        colourPanel = new ColourPanel(styleName, defaultStyle, settingsPath);

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
