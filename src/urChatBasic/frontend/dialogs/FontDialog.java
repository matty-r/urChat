package urChatBasic.frontend.dialogs;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import urChatBasic.base.DialogBase;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.components.FontPanel;

public class FontDialog extends DialogBase
{
    private FontPanel fontPanel;

    public FontDialog(String title, Font defaultFont, Preferences settingsPath)
    {
        super(DriverGUI.frame, title, true);
        initFontDialog(defaultFont, settingsPath);
    }

    public void initFontDialog(Font defaultFont, Preferences settingsPath)
    {
        setSize(600, 100);
        setResizable(false);
        setMaximumSize(new Dimension(600, 100));
        setLocationRelativeTo(super.getParent());

        fontPanel = new FontPanel(defaultFont, settingsPath, "Default Font:");

        add(fontPanel);
    }

    public FontPanel getFontPanel()
    {
        return fontPanel;
    }

    @Override
    public void setVisible(boolean b)
    {
        fontPanel.loadFont();
        super.setVisible(b);
    }

    public void addSaveListener(ActionListener newActionListener)
    {
        fontPanel.getSaveButton().addActionListener(newActionListener);
    }
}
