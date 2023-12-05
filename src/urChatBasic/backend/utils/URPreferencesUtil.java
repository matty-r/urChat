package urChatBasic.backend.utils;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import javax.swing.text.StyleConstants;
import urChatBasic.base.Constants;
import urChatBasic.frontend.utils.URColour;

public class URPreferencesUtil {

    /**
     * Uses the defaultFont for the returned font if there is no font saved.
     * @param defaultFont
     * @param settingsPath
     * @return
     */
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

    /**
     * Uses the defaultStyle for the returned colours if there aren't any colours saved.
     * @param defaultStyle
     * @param settingsPath
     * @return
     */
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

    public static URStyle loadStyle(URStyle targetStyle, Preferences baseSettingsPath)
    {
        Preferences stylePrefPath = baseSettingsPath.node(targetStyle.getAttribute("name").toString());
        System.out.println("Load Style Path: " + stylePrefPath.toString());
        Font loadedFont = loadStyleFont(targetStyle.getFont(), stylePrefPath);
        // LineFormatter.getStyleAsFont(defaultStyle);
        Map<String, Color> loadedColours = loadStyleColours(targetStyle, stylePrefPath);
        // LineFormatter.getStyleColours(defaultStyle);

        StyleConstants.setFontFamily(targetStyle,
                loadedFont.getFamily());

        StyleConstants.setFontSize(targetStyle,
                loadedFont.getSize());

        StyleConstants.setBold(targetStyle,
                loadedFont.isBold());

        StyleConstants.setItalic(targetStyle,
                loadedFont.isItalic());

        StyleConstants.setForeground(targetStyle, loadedColours.get(Constants.KEY_FONT_FOREGROUND));

        StyleConstants.setBackground(targetStyle, loadedColours.get(Constants.KEY_FONT_BACKGROUND));

        // System.out.println("Loaded: "+targetStyle.getAttribute("name") + ". Font: "+loadedFont.getFamily() +
        //     " Colours - fg: "+URColour.hexEncode(targetStyle.getForeground()) + " bg: " + URColour.hexEncode(targetStyle.getBackground()));

        return targetStyle;
    }

    public static void deleteStyleFont(URStyle targetStyle, Preferences baseSettingsPath)
    {
        Preferences settingsPath =  baseSettingsPath.node(targetStyle.getName());
        try
        {
            System.out.println("Removing font keys: " + settingsPath.absolutePath());
            settingsPath.remove(Constants.KEY_FONT_BOLD);
            settingsPath.remove(Constants.KEY_FONT_ITALIC);
            settingsPath.remove(Constants.KEY_FONT_FAMILY);
            settingsPath.remove(Constants.KEY_FONT_SIZE);
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        // TODO: Don't safe if it's the default font
        settingsPath.putBoolean(Constants.KEY_FONT_BOLD, newFont.isBold());
        settingsPath.putBoolean(Constants.KEY_FONT_ITALIC, newFont.isItalic());
        settingsPath.put(Constants.KEY_FONT_FAMILY, newFont.getFamily());
        settingsPath.putInt(Constants.KEY_FONT_SIZE, newFont.getSize());
    }

    /**
     * Removes the saved colours if they've been set to the default, this ensures we're not accidentally
     * overriding the default theme colours when the theme is changed.
     * @param foreground
     * @param background
     * @param settingsPath
     */
    private static void saveStyleColours(Color foreground, Color background, Preferences settingsPath)
    {
        // Don't save if it's the default colours
        Color defaultForeground = UIManager.getColor("Label.foreground");
        Color defaultBackground = UIManager.getColor("Panel.background");

        if(URColour.hexEncode(defaultForeground).equals(URColour.hexEncode(foreground)))
        {
            settingsPath.remove(Constants.KEY_FONT_FOREGROUND);
        } else {
            settingsPath.put(Constants.KEY_FONT_FOREGROUND, URColour.hexEncode(foreground));
        }

        if(URColour.hexEncode(defaultBackground).equals(URColour.hexEncode(background)))
        {
            settingsPath.remove(Constants.KEY_FONT_BACKGROUND);
        } else {
            settingsPath.put(Constants.KEY_FONT_BACKGROUND, URColour.hexEncode(background));
        }
    }
}
