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

    public static Font loadStyleFont(Font defaultFont, Preferences settingsPath)
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

    public static Map<String, Color> loadStyleColours(URStyle defaultStyle, Preferences settingsPath)
    {
        Map<String, Color> colourMap = new HashMap<String, Color>();
        colourMap.put(Constants.KEY_FONT_FOREGROUND, defaultStyle.getForeground());
        colourMap.put(Constants.KEY_FONT_BACKGROUND, defaultStyle.getBackground());

        String loadedForeground = settingsPath.get(Constants.KEY_FONT_FOREGROUND, URColour.hexEncode(defaultStyle.getForeground()));
        String loadedBackground = settingsPath.get(Constants.KEY_FONT_BACKGROUND, URColour.hexEncode(defaultStyle.getBackground()));

        colourMap.replace(Constants.KEY_FONT_FOREGROUND, URColour.hexDecode(loadedForeground));
        colourMap.replace(Constants.KEY_FONT_BACKGROUND, URColour.hexDecode(loadedBackground));

        return colourMap;
    }

    public static URStyle loadStyle(URStyle defaultStyle, Preferences baseSettingsPath)
    {
        Preferences stylePrefPath = baseSettingsPath.node(defaultStyle.getAttribute("name").toString());
        System.out.println("Load Style Path: " + stylePrefPath.toString());
        Font loadedFont = loadStyleFont(defaultStyle.getFont(), stylePrefPath);
        // LineFormatter.getStyleAsFont(defaultStyle);
        Map<String, Color> loadedColours = loadStyleColours(defaultStyle, stylePrefPath);
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

        System.out.println("Loaded: "+defaultStyle.getAttribute("name") + ". Font: "+loadedFont.getFamily() +
            " Colours - fg: "+URColour.hexEncode(defaultStyle.getForeground()) + " bg: " + URColour.hexEncode(defaultStyle.getBackground()));

        return defaultStyle;
    }

    public static void saveStyle(URStyle targetStyle, Preferences baseSettingsPath)
    {
        Preferences stylePrefPath = baseSettingsPath.node(targetStyle.getAttribute("name").toString());
        System.out.println("Save Style Path: " + stylePrefPath.toString());
        saveStyleFont(targetStyle.getFont(), stylePrefPath);
        saveStyleColours(targetStyle.getForeground(), targetStyle.getBackground(), stylePrefPath);
    }

    /**
     * TODO: This should be deprecated and just use a saveStyle method.
     * @param newFont
     * @param settingsPath
     */
    private static void saveStyleFont(Font newFont, Preferences settingsPath)
    {
        settingsPath.putBoolean(Constants.KEY_FONT_BOLD, newFont.isBold());
        settingsPath.putBoolean(Constants.KEY_FONT_ITALIC, newFont.isItalic());
        settingsPath.put(Constants.KEY_FONT_FAMILY, newFont.getFamily());
        settingsPath.putInt(Constants.KEY_FONT_SIZE, newFont.getSize());
    }

    private static void saveStyleColours(Color newForeground, Color newBackground, Preferences settingsPath)
    {
        settingsPath.put(Constants.KEY_FONT_FOREGROUND, URColour.hexEncode(newForeground));
        settingsPath.put(Constants.KEY_FONT_BACKGROUND, URColour.hexEncode(newBackground));
    }
}
