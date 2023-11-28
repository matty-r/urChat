package urChatBasic.frontend.components;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.*;
import urChatBasic.backend.utils.URSettingsLoader;
import urChatBasic.base.Constants;

/* ColorChooserDemo.java requires no other files. */
public class ColourPanel extends JPanel implements ChangeListener {
    protected JColorChooser tcc;
    public Color selectedColor;
    private Color defaultForeground;
    private Color defaultBackground;
    private Font displayFont;
    private Preferences settingsPath;

    public ColourPanel(Font displayFont, Preferences settingsPath) {
        super(new BorderLayout());
        this.settingsPath = settingsPath;
        this.displayFont = displayFont;
        defaultForeground = getForeground();
        defaultBackground = getBackground();

        //Set up color chooser for setting text color
        tcc = new JColorChooser(defaultForeground);
        tcc.getPreviewPanel().setFont(this.displayFont);
        tcc.getSelectionModel().addChangeListener(this);

        add(tcc, BorderLayout.PAGE_END);
    }

    public void loadColours()
    {
        Map<String, Color> colourMap = URSettingsLoader.loadFontColours(defaultForeground, defaultBackground, settingsPath);

        defaultBackground = colourMap.get(Constants.KEY_FONT_BACKGROUND);
        defaultForeground = colourMap.get(Constants.KEY_FONT_FOREGROUND);
        // int savedFontBoldItalic = 0;

        // if (settingsPath.getBoolean(Constants.KEY_FONT_BOLD, defaultFont.isBold()))
        //     savedFontBoldItalic = Font.BOLD;
        // if (settingsPath.getBoolean(Constants.KEY_FONT_ITALIC, defaultFont.isItalic()))
        //     savedFontBoldItalic |= Font.ITALIC;

        // savedFont = new Font(settingsPath.get(Constants.KEY_FONT_FAMILY, defaultFont.getFamily()),
        //         savedFontBoldItalic, settingsPath.getInt(Constants.KEY_FONT_SIZE, defaultFont.getSize()));

        // setFont(savedFont, false);
    }

    public void stateChanged(ChangeEvent e) {
        selectedColor = tcc.getColor();
    }
}
