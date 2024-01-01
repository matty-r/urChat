package urChatBasic.backend.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.List;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import urChatBasic.base.Constants;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.utils.URColour;

public class URPreferencesUtil {

    /**
     * Uses the defaultFont for the returned font if there is no font saved.
     * TODO: This should really go through an enum of font keys and apply them all instead?
     * @param defaultFont
     * @param settingsPath
     * @return
     */
    public static Font loadStyleFont(Font defaultFont, Preferences settingsPath)
    {
        // Font savedFont = defaultFont;
        // int savedFontBoldItalic = 0;

        // if (settingsPath.getBoolean(Constants.KEY_FONT_BOLD, defaultFont.isBold()))
        //     savedFontBoldItalic = Font.BOLD;
        // if (settingsPath.getBoolean(Constants.KEY_FONT_ITALIC, defaultFont.isItalic()))
        //     savedFontBoldItalic |= Font.ITALIC;

        // savedFont = new Font(settingsPath.get(Constants.KEY_FONT_FAMILY, defaultFont.getFamily()),
        //         savedFontBoldItalic, settingsPath.getInt(Constants.KEY_FONT_SIZE, defaultFont.getSize()));

        // return savedFont;

        try
        {
            if(settingsPath.keys().length == 0)
                return defaultFont;
        } catch (BackingStoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return defaultFont;
        }

        Font savedFont = defaultFont;

        Map<TextAttribute, Object> fontMap = new Hashtable<TextAttribute, Object>();

        fontMap.put(TextAttribute.WEIGHT, settingsPath.getBoolean(Constants.KEY_FONT_BOLD, defaultFont.isBold()) ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
        fontMap.put(TextAttribute.POSTURE, settingsPath.getBoolean(Constants.KEY_FONT_ITALIC, defaultFont.isItalic()) ? TextAttribute.POSTURE_OBLIQUE : TextAttribute.POSTURE_REGULAR);
        fontMap.put(TextAttribute.UNDERLINE, settingsPath.getBoolean(Constants.KEY_FONT_UNDERLINE, URStyle.isUnderline(defaultFont)) ? TextAttribute.UNDERLINE_ON : -1);

        savedFont = new Font(settingsPath.get(Constants.KEY_FONT_FAMILY, defaultFont.getFamily()), Font.PLAIN, settingsPath.getInt(Constants.KEY_FONT_SIZE, defaultFont.getSize()))
                .deriveFont(fontMap);

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

        defaultStyle.getForeground().ifPresent(fg -> colourMap.put(Constants.KEY_FONT_FOREGROUND, fg));
        defaultStyle.getBackground().ifPresent(bg -> colourMap.put(Constants.KEY_FONT_BACKGROUND, bg));

        try
        {
            if(settingsPath.keys().length == 0)
                return colourMap;
        } catch (BackingStoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return colourMap;
        }

        String loadedForeground;
        if(defaultStyle.getForeground().isPresent())
        {
            loadedForeground = settingsPath.get(Constants.KEY_FONT_FOREGROUND, URColour.hexEncode(defaultStyle.getForeground().get()));
            colourMap.put(Constants.KEY_FONT_FOREGROUND, URColour.hexDecode(loadedForeground));
        }

        String loadedBackground;
        if(defaultStyle.getBackground().isPresent())
        {
            loadedBackground = settingsPath.get(Constants.KEY_FONT_BACKGROUND, URColour.hexEncode(defaultStyle.getBackground().get()));
            colourMap.put(Constants.KEY_FONT_BACKGROUND, URColour.hexDecode(loadedBackground));
        }

        return colourMap;
    }

    /**
     * TODO: Add a fallbackSettingsPath in order to have somewhere else to load the targetStyle from if the baseSettingsPath doesn't exist.
     * @param targetStyle
     * @param baseSettingsPath
     * @return
     */
    public static URStyle loadStyle(final URStyle targetStyle, Preferences baseSettingsPath)
    {
        URStyle loadedStyle = targetStyle.clone();
        // Default to the profile path node
        Preferences stylePrefPath = baseSettingsPath;
        if(loadedStyle.getAttribute("name") == null)
            loadedStyle.addAttribute("name", "");

        String styleName = loadedStyle.getAttribute("name").toString();


        try
        {

            if(baseSettingsPath.nodeExists(styleName))
                stylePrefPath = baseSettingsPath.node(styleName);
            else if (DriverGUI.gui != null && URProfilesUtil.getActiveProfilePath().nodeExists(styleName))
                stylePrefPath = URProfilesUtil.getActiveProfilePath().node(styleName);
            else
                return targetStyle;

        } catch (Exception e)
        {
            System.err.println("Active Profile: ["+URProfilesUtil.getActiveProfileName()+"] Unable to load ["+loadedStyle.getAttribute("name")+"]"+ " attempted with path: " + stylePrefPath);
            // TODO Auto-generated catch block
            // e.printStackTrace();
            return targetStyle;
        }

        Constants.LOGGER.log(Level.FINE, "Load Style Path: " + stylePrefPath.toString());
        Font loadedFont = loadStyleFont(loadedStyle.getFont(), stylePrefPath);
        Map<String, Color> loadedColours = loadStyleColours(loadedStyle, stylePrefPath);

        loadedStyle.setFont(loadedFont);
        loadedStyle.setForeground(loadedColours.get(Constants.KEY_FONT_FOREGROUND));
        loadedStyle.setBackground(loadedColours.get(Constants.KEY_FONT_BACKGROUND));

        return loadedStyle;
    }

    public static void deleteStyleFont(URStyle targetStyle, Preferences baseSettingsPath)
    {
        Preferences settingsPath =  baseSettingsPath.node(targetStyle.getName());
        try
        {
            Constants.LOGGER.log(Level.FINE, "Removing font keys: " + settingsPath.absolutePath());
            settingsPath.remove(Constants.KEY_FONT_BOLD);
            settingsPath.remove(Constants.KEY_FONT_ITALIC);
            settingsPath.remove(Constants.KEY_FONT_FAMILY);
            settingsPath.remove(Constants.KEY_FONT_SIZE);
            settingsPath.remove(Constants.KEY_FONT_UNDERLINE);
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void deleteStyleColours(URStyle targetStyle, Preferences baseSettingsPath)
    {
        Preferences settingsPath =  baseSettingsPath.node(targetStyle.getName());
        try
        {
            Constants.LOGGER.log(Level.INFO, "Removing font colours: " + settingsPath.absolutePath());
            settingsPath.remove(Constants.KEY_FONT_FOREGROUND);
            settingsPath.remove(Constants.KEY_FONT_BACKGROUND);
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void saveStyle(URStyle oldStyle, URStyle newStyle, Preferences baseSettingsPath)
    {
        Preferences stylePrefPath = baseSettingsPath.node(newStyle.getAttribute("name").toString());
        Constants.LOGGER.log(Level.INFO, "Save Style Path: " + stylePrefPath.toString());

        URStyle diffStyle = oldStyle.clone();

        Enumeration<?> oldAttributes = oldStyle.getAttributeNames();
        while (oldAttributes.hasMoreElements()) {
            Object attributeName = oldAttributes.nextElement();
            Object oldValue = oldStyle.getAttribute(attributeName);
            Object newValue = newStyle.getAttribute(attributeName);

            if (newValue == null || !newValue.equals(oldValue)) {
                diffStyle.addAttribute(attributeName, newValue);
            } else {
                // If same, remove from diffStyle
                diffStyle.removeAttribute(attributeName);
                try
                {
                    // Remove it from the current saved style if what is saved doesn't match what the oldStyle
                    // oldStyle is the default, so if it doesn't exist then it will use the oldStyle, therefore
                    // it doesn't need to exist in the preferences.
                    if(Arrays.asList(stylePrefPath.keys()).contains(URStyle.getKeymap(attributeName)))
                    {
                        String savedValue = stylePrefPath.get(URStyle.getKeymap(attributeName), null);
                        if(savedValue != null && !savedValue.equals(oldValue))
                            stylePrefPath.remove(URStyle.getKeymap(attributeName));
                    }
                } catch (BackingStoreException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        // Check attributes in newStyle that are not in oldStyle
        Enumeration<?> newAttributes = newStyle.getAttributeNames();
        while (newAttributes.hasMoreElements()) {
            Object attributeName = newAttributes.nextElement();
            if (!oldStyle.containsAttribute(attributeName, newStyle.getAttribute(attributeName))) {
                diffStyle.addAttribute(attributeName, newStyle.getAttribute(attributeName));
            }
        }

        saveStyleFont(diffStyle, stylePrefPath);
        saveStyleColours(diffStyle, stylePrefPath);
    }

    public static void putPref(String name, Object value, Preferences path) {
        putPref(name, Optional.of(value), path);
    }

    public static void putPref(String name, Optional<?> optionalValue, Preferences path) {
        System.out.println("Saving name:" + name);
        if(optionalValue != null && optionalValue.isPresent())
        {
            Object value = optionalValue.get();
            if (value instanceof String) {
                path.put(name, (String) value);
            } else if (value instanceof Integer) {
                path.putInt(name, (int) value);
            } else if (value instanceof Boolean) {
                path.putBoolean(name, (boolean) value);
            } else {
                System.err.println("Unsupported data type for preference: " + value.getClass().getSimpleName());
            }
        }
    }

    public static Object getPref (String name, Object defaultValue, Preferences path)
    {

        // TODO: Exception handling
        // if (!(defaultValue instanceof String || defaultValue instanceof Integer || defaultValue instanceof Boolean)) {
        //     throw new IllegalArgumentException("Invalid defaultValue type. Allowed types: String, Integer, Boolean");
        // }
        Object stringValue = path.get(name, null);
        if (stringValue != null && defaultValue != null) {
            // Check if the value exists in preferences
            if (defaultValue instanceof String) {
                return path.get(name, (String) defaultValue);
            } else if (defaultValue instanceof Integer) {
                return path.getInt(name, (int) defaultValue);
            } else if (defaultValue instanceof Boolean) {
                return path.getBoolean(name, (boolean) defaultValue);
            }
        } else if (stringValue != null && defaultValue == null)
        {
            try {
                if (stringValue != null) {
                    // Attempt to parse the stored value to int
                    return Integer.parseInt((String) stringValue);
                }
            } catch (NumberFormatException e) {
                // If parsing to int fails, attempt to get the boolean value
                if(((String) stringValue).equalsIgnoreCase("true") || ((String) stringValue).equalsIgnoreCase("false"))
                {
                    return Boolean.parseBoolean((String) stringValue);
                }
                // If parsing to boolean fails, return the string value
                return stringValue;
            }
        }

        return defaultValue;
    }

    public static ArrayList<Preferences> getAllNodes (Preferences rootPath)
    {
        ArrayList<Preferences> prefPaths = new ArrayList<Preferences>();

        try
        {
            prefPaths.add(rootPath);

            for (String nodeName : rootPath.childrenNames()) {
                prefPaths.addAll(getAllNodes(rootPath.node(nodeName)));
            }
        } catch (BackingStoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return prefPaths;
    }

    /**
     *
     * @param saveStyle
     * @param settingsPath
     */
    private static void saveStyleFont(URStyle saveStyle, Preferences settingsPath) {
        putPref(Constants.KEY_FONT_BOLD, saveStyle.isBold(), settingsPath);
        putPref(Constants.KEY_FONT_ITALIC, saveStyle.isItalic(), settingsPath);
        putPref(Constants.KEY_FONT_UNDERLINE, saveStyle.isUnderline(), settingsPath);
        putPref(Constants.KEY_FONT_FAMILY, saveStyle.getFamily(), settingsPath);
        putPref(Constants.KEY_FONT_SIZE, saveStyle.getSize(), settingsPath);
    }

    /**
     * Removes the saved colours if they've been set to the default, this ensures we're not accidentally
     * overriding the default theme colours when the theme is changed.
     * @param newForeground
     * @param newBackground
     * @param settingsPath
     */
    private static void saveStyleColours(URStyle saveStyle, Preferences settingsPath)
    {

        // Don't save if it's the default colours
        Color defaultForeground = UIManager.getColor(Constants.DEFAULT_FOREGROUND_STRING);
        Color defaultBackground = UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING);

        if(saveStyle.getForeground().isPresent())
        {
            Color newForeground = saveStyle.getForeground().get();
            if(URColour.hexEncode(defaultForeground).equals(URColour.hexEncode(newForeground)))
            {
                settingsPath.remove(Constants.KEY_FONT_FOREGROUND);
            } else {
                settingsPath.put(Constants.KEY_FONT_FOREGROUND, URColour.hexEncode(newForeground));
            }
        }

        if(saveStyle.getBackground().isPresent())
        {
            Color newBackground = saveStyle.getBackground().get();
            if(URColour.hexEncode(defaultBackground).equals(URColour.hexEncode(newBackground)))
            {
                settingsPath.remove(Constants.KEY_FONT_BACKGROUND);
            } else {
                settingsPath.put(Constants.KEY_FONT_BACKGROUND, URColour.hexEncode(newBackground));
            }
        }
    }
}
