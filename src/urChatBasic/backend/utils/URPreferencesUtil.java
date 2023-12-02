package urChatBasic.backend.utils;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.text.StyleConstants;
import urChatBasic.base.Constants;
import urChatBasic.frontend.utils.URColour;

public class URPreferencesUtil {

    public static Font loadFont(Font defaultFont, Preferences settingsPath)
    {
        Font savedFont = defaultFont;
        int savedFontBoldItalic = 0;

        if (settingsPath.getBoolean(Constants.KEY_FONT_BOLD, defaultFont.isBold()))
            savedFontBoldItalic = Font.BOLD;
        if (settingsPath.getBoolean(Constants.KEY_FONT_ITALIC, defaultFont.isItalic()))
            savedFontBoldItalic |= Font.ITALIC;

        savedFont = new Font(settingsPath.get(Constants.KEY_FONT_FAMILY, defaultFont.getFamily()),
                savedFontBoldItalic, settingsPath.getInt(Constants.KEY_FONT_SIZE, defaultFont.getSize()));

        return savedFont;
    }

    public static Map<String, Color> loadFontColours(Color defaultForeground, Color defaultBackground, Preferences settingsPath)
    {
        Map<String, Color> colourMap = new HashMap<String, Color>();
        colourMap.put(Constants.KEY_FONT_FOREGROUND, defaultForeground);
        colourMap.put(Constants.KEY_FONT_BACKGROUND, defaultBackground);

        String loadedForeground = settingsPath.get(Constants.KEY_FONT_FOREGROUND, URColour.hexEncode(defaultForeground));
        String loadedBackground = settingsPath.get(Constants.KEY_FONT_BACKGROUND, URColour.hexEncode(defaultBackground));

        colourMap.replace(Constants.KEY_FONT_FOREGROUND, URColour.hexDecode(loadedForeground));
        colourMap.replace(Constants.KEY_FONT_BACKGROUND, URColour.hexDecode(loadedBackground));

        return colourMap;
    }

    public static URStyle loadStyle(URStyle defaultStyle, Preferences settingsPath)
    {
        Preferences stylePrefPath = settingsPath.node(defaultStyle.getAttribute("name").toString());

        Font loadedFont = loadFont(defaultStyle.getFont(), stylePrefPath);
        // LineFormatter.getStyleAsFont(defaultStyle);
        Map<String, Color> loadedColours = loadFontColours(defaultStyle.getForeground(), defaultStyle.getBackground(), stylePrefPath);
        // LineFormatter.getStyleColours(defaultStyle);

        StyleConstants.setFontFamily(defaultStyle,
                loadedFont.getFamily());

        StyleConstants.setFontSize(defaultStyle,
                loadedFont.getSize());

        StyleConstants.setBold(defaultStyle,
                loadedFont.isBold());

        StyleConstants.setItalic(defaultStyle,
                loadedFont.isItalic());

        StyleConstants.setForeground(defaultStyle, loadedColours.get(Constants.KEY_FONT_FOREGROUND));

        StyleConstants.setBackground(defaultStyle, loadedColours.get(Constants.KEY_FONT_BACKGROUND));

        return defaultStyle;
    }

    public static void saveFontColours(Color newForeground, Color newBackground, Preferences settingsPath)
    {
        settingsPath.put(Constants.KEY_FONT_FOREGROUND, URColour.hexEncode(newForeground));
        settingsPath.put(Constants.KEY_FONT_BACKGROUND, URColour.hexEncode(newBackground));
    }
}
