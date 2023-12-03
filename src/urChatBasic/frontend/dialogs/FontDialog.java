package urChatBasic.frontend.dialogs;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import urChatBasic.backend.utils.URStyle;
import urChatBasic.base.DialogBase;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.components.FontPanel;

public class FontDialog extends DialogBase
{
    private String title = "Default Font";
    private FontPanel fontPanel;

    public FontDialog(URStyle defaultStyle, Preferences settingsPath, String title)
    {
        super(DriverGUI.frame, title, true);
        this.title = title;
        initFontDialog(defaultStyle, settingsPath);
    }

    public void initFontDialog(URStyle defaultStyle, Preferences settingsPath)
    {
        setSize(600, 100);
        setResizable(false);
        setMaximumSize(new Dimension(600, 100));
        setLocationRelativeTo(super.getParent());

        fontPanel = new FontPanel(defaultStyle, settingsPath, title);

        add(fontPanel);
    }

    public FontPanel getFontPanel()
    {
        return fontPanel;
    }

    @Override
    public void setVisible(boolean b)
    {
        setLocationRelativeTo(super.getParent());
        fontPanel.loadStyle();
        super.setVisible(b);
    }

    public void addSaveListener(ActionListener newActionListener)
    {
        // fontPanel.getSaveButton().addActionListener(newActionListener);
        fontPanel.addActionListener(fontPanel.getSaveButton(), newActionListener);
    }

    public void addResetListener(ActionListener newActionListener)
    {
        fontPanel.getResetButton().addActionListener(newActionListener);
    }

    public class ShowFontDialog implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            fontPanel.loadStyle();
            FontDialog.this.setVisible(true);
        }
    }
}
