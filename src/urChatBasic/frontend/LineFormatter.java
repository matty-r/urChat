package urChatBasic.frontend;

import java.awt.Color;
import java.awt.Font;
import java.util.logging.Level;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import urChatBasic.base.Constants;

public class LineFormatter
{
    private String myNick;
    private Font myFont;
    public SimpleAttributeSet timeStyle;
    public SimpleAttributeSet nameStyle;
    public SimpleAttributeSet lineStyle;

    public LineFormatter(Font myFont, String myNick)
    {
        this.myNick = myNick;
        this.myFont = myFont;
    }

    public SimpleAttributeSet standardStyle()
    {

        SimpleAttributeSet tempStyle = new SimpleAttributeSet();
        tempStyle.addAttribute("name", "standardStyle");
        StyleConstants.setForeground(tempStyle, Color.BLACK);
        StyleConstants.setFontFamily(tempStyle, myFont.getFamily());
        StyleConstants.setFontSize(tempStyle, myFont.getSize());

        return tempStyle;
    }

    public SimpleAttributeSet lowStyle()
    {

        SimpleAttributeSet tempStyle = new SimpleAttributeSet();
        tempStyle.addAttribute("name", "lowStyle");
        StyleConstants.setForeground(tempStyle, Color.LIGHT_GRAY);
        StyleConstants.setFontFamily(tempStyle, myFont.getFamily());
        StyleConstants.setFontSize(tempStyle, myFont.getSize());

        return tempStyle;
    }

    public SimpleAttributeSet mediumStyle()
    {

        SimpleAttributeSet tempStyle = new SimpleAttributeSet();
        tempStyle.addAttribute("name", "mediumStyle");
        StyleConstants.setBackground(tempStyle, Color.YELLOW);
        StyleConstants.setFontFamily(tempStyle, myFont.getFamily());
        StyleConstants.setFontSize(tempStyle, myFont.getSize());

        return tempStyle;
    }

    public SimpleAttributeSet highStyle()
    {

        SimpleAttributeSet tempStyle = new SimpleAttributeSet();
        tempStyle.addAttribute("name", "highStyle");
        StyleConstants.setBold(tempStyle, true);
        StyleConstants.setBackground(tempStyle, Color.RED);
        StyleConstants.setForeground(tempStyle, Color.WHITE);
        StyleConstants.setItalic(tempStyle, true);
        StyleConstants.setFontFamily(tempStyle, myFont.getFamily());
        StyleConstants.setFontSize(tempStyle, myFont.getSize());

        return tempStyle;
    }

    public SimpleAttributeSet myStyle()
    {
        SimpleAttributeSet tempStyle = new SimpleAttributeSet();
        tempStyle.addAttribute("name", "myStyle");
        StyleConstants.setForeground(tempStyle, Color.GREEN);
        StyleConstants.setBold(tempStyle, true);
        StyleConstants.setFontFamily(tempStyle, myFont.getFamily());
        StyleConstants.setFontSize(tempStyle, myFont.getSize());

        return tempStyle;
    }

    public void formattedDocument(StyledDocument doc, String timeLine, String fromUser, String line)
    {
        nameStyle = standardStyle();

        if (myNick.equals(fromUser))
        {
            nameStyle = this.myStyle();
        } else
        {
            if (line.indexOf(myNick) > -1)
                nameStyle = highStyle();
        }

        if (fromUser.equals(IRCChannel.EVENT_USER))
        {
            nameStyle = lowStyle();
            lineStyle = lowStyle();
        }

        try
        {
            doc.insertString(doc.getLength(), timeLine, timeStyle);
            doc.insertString(doc.getLength(), " <", lineStyle);
            doc.insertString(doc.getLength(), fromUser, nameStyle);
            doc.insertString(doc.getLength(), "> ", lineStyle);
            doc.insertString(doc.getLength(), line + "\n", lineStyle);
        } catch (BadLocationException e)
        {
            Constants.LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }

}
