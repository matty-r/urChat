package urChatBasic.backend.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Hashtable;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import urChatBasic.base.Constants;

public class URStyle extends SimpleAttributeSet
{

    // /**
    // * Create a URStyle with defaults
    // */
    // public URStyle (String name)
    // {
    // this(name, Constants.DEFAULT_FONT_GENERAL);
    // }

    /**
     * Create a URStyle based on defaultFont
     *
     * @param defaultFont
     */
    public URStyle(String name, Font defaultFont)
    {
        super();
        this.addAttribute("name", name);
        setFont(defaultFont);
        setForeground(UIManager.getColor(Constants.DEFAULT_FOREGROUND_STRING));
        setBackground(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING));
    }

    /**
     * Create a URStyle based on defaultFont
     *
     * @param defaultFont
     */
    public URStyle(String name, Font defaultFont, Color defaultForeground, Color defaultBackground)
    {
        super();
        this.addAttribute("name", name);
        setFont(defaultFont);
        setForeground(defaultForeground);
        setBackground(defaultBackground);
    }

    public String getName()
    {
        return getAttribute("name").toString();
    }

    public Font getFont()
    {
        // int savedFontBoldItalic = 0;

        // if (StyleConstants.isBold(this))
        // savedFontBoldItalic = Font.BOLD;
        // if (StyleConstants.isItalic(this))
        // savedFontBoldItalic |= Font.ITALIC;

        Map<TextAttribute, Object> fontMap = new Hashtable<TextAttribute, Object>();

        fontMap.put(TextAttribute.WEIGHT, StyleConstants.isBold(this) ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
        fontMap.put(TextAttribute.POSTURE, StyleConstants.isItalic(this) ? TextAttribute.POSTURE_OBLIQUE : TextAttribute.POSTURE_REGULAR);
        fontMap.put(TextAttribute.UNDERLINE, StyleConstants.isUnderline(this) ? TextAttribute.UNDERLINE_ON : -1);

        Font styleFont = new Font(StyleConstants.getFontFamily(this), Font.PLAIN, StyleConstants.getFontSize(this))
                .deriveFont(fontMap);

        return styleFont;
        // Font styleFont = new Font(StyleConstants.getFontFamily(this), savedFontBoldItalic,
        // StyleConstants.getFontSize(this));

        // return styleFont;
    }

    public void setFont(Font newFont)
    {
        StyleConstants.setFontFamily(this, newFont.getFamily());
        StyleConstants.setBold(this, newFont.isBold());
        StyleConstants.setItalic(this, newFont.isItalic());
        StyleConstants.setFontSize(this, newFont.getSize());
        StyleConstants.setUnderline(this, isUnderline(newFont));
    }

    public Color getForeground()
    {
        return StyleConstants.getForeground(this);
    }


    public boolean isUnderline()
    {
        return StyleConstants.isUnderline(this);
    }

    // https://docs.oracle.com/javase/6/docs/api/java/awt/font/TextAttribute.html#UNDERLINE
    public static boolean isUnderline(Font targetFont)
    {
        if (targetFont != null && targetFont.getAttributes().get(TextAttribute.UNDERLINE) != null)
            return (int) targetFont.getAttributes().get(TextAttribute.UNDERLINE) == TextAttribute.UNDERLINE_ON;

        return false;
    }

    public Color getBackground()
    {
        return StyleConstants.getBackground(this);
    }

    public void setForeground(Color newColour)
    {
        StyleConstants.setForeground(this, newColour);
    }

    public void setBackground(Color newColour)
    {
        StyleConstants.setBackground(this, newColour);
    }

    public void load(Preferences prefPath)
    {
        URStyle loadedStyle = URPreferencesUtil.loadStyle(this, prefPath);
        setFont(loadedStyle.getFont());
        setForeground(loadedStyle.getForeground());
        setBackground(loadedStyle.getBackground());
    }

    public void save(Preferences prefPath)
    {
        URPreferencesUtil.saveStyle(this, prefPath);
    }

    public boolean equals(URStyle otherStyle)
    {
        return getFont().equals(otherStyle.getFont()) && getForeground().equals(otherStyle.getForeground())
                && getBackground().equals(otherStyle.getBackground());
    }

    @Override
    public URStyle clone()
    {
        return (URStyle) super.clone();
    }
}
