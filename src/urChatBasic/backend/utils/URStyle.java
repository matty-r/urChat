package urChatBasic.backend.utils;

import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import urChatBasic.base.Constants;

public class URStyle extends SimpleAttributeSet {

    /**
     * Create a URStyle with defaults
     */
    public URStyle (String name)
    {
        this(name, Constants.DEFAULT_FONT_GENERAL);
    }

    /**
     * Create a URStyle based on defaultFont
     * @param defaultFont
     */
    public URStyle (String name, Font defaultFont)
    {
        super();
        this.addAttribute("name", name);
        setFont(defaultFont);
        setForeground(UIManager.getColor("Label.foreground"));
        setBackground(UIManager.getColor("Label.background"));
    }

    public Font getFont()
    {
        int savedFontBoldItalic = 0;

        if (StyleConstants.isBold(this))
            savedFontBoldItalic = Font.BOLD;
        if (StyleConstants.isItalic(this))
            savedFontBoldItalic |= Font.ITALIC;

        Font styleFont = new Font(StyleConstants.getFontFamily(this), savedFontBoldItalic,
                StyleConstants.getFontSize(this));

        return styleFont;
    }

    public void setFont(Font newFont)
    {
        StyleConstants.setFontFamily(this, newFont.getFamily());
        StyleConstants.setBold(this, newFont.isBold());
        StyleConstants.setItalic(this, newFont.isItalic());
        StyleConstants.setFontSize(this, newFont.getSize());
    }

    public Color getForeground()
    {
        return StyleConstants.getForeground(this);
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
}
