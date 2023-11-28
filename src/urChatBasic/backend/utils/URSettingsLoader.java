package urChatBasic.backend.utils;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import urChatBasic.base.Constants;
import urChatBasic.frontend.utils.URColour;

public class URSettingsLoader {

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

    public static Map<String, Color> loadFontColours(Color foreground, Color background, Preferences settingsPath)
    {
        Map<String, Color> colourMap = new HashMap<String, Color>();
        colourMap.put(Constants.KEY_FONT_FOREGROUND, foreground);
        colourMap.put(Constants.KEY_FONT_BACKGROUND, background);

        String loadedForeground = settingsPath.get(Constants.KEY_FONT_FOREGROUND, URColour.hexEncode(foreground));
        String loadedBackground = settingsPath.get(Constants.KEY_FONT_BACKGROUND, URColour.hexEncode(background));

        colourMap.replace(Constants.KEY_FONT_FOREGROUND, URColour.hexDecode(loadedForeground));
        colourMap.replace(Constants.KEY_FONT_BACKGROUND, URColour.hexDecode(loadedBackground));

        return colourMap;
    }
}
