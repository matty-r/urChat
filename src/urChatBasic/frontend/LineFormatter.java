package urChatBasic.frontend;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
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
    protected UserGUI gui = DriverGUI.gui;

    public LineFormatter(Font myFont, String myNick)
    {
        this.myNick = myNick;
        this.myFont = myFont;
        timeStyle = standardStyle();
        nameStyle = standardStyle();
        lineStyle = standardStyle();
    }

    public SimpleAttributeSet standardStyle()
    {

        SimpleAttributeSet tempStyle = new SimpleAttributeSet();
        tempStyle.addAttribute("name", "standardStyle");
        StyleConstants.setForeground(tempStyle, Color.BLACK);
        StyleConstants.setBold(tempStyle, myFont.isBold());
        StyleConstants.setItalic(tempStyle, myFont.isItalic());
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

    public SimpleAttributeSet urlStyle()
    {
        SimpleAttributeSet tempStyle = new SimpleAttributeSet();
        tempStyle.addAttribute("name", "urlStyle");
        StyleConstants.setForeground(tempStyle, Color.BLUE);
        StyleConstants.setUnderline(tempStyle, true);
        StyleConstants.setFontFamily(tempStyle, myFont.getFamily());
        StyleConstants.setFontSize(tempStyle, myFont.getSize());

        return tempStyle;
    }

    public SimpleAttributeSet myStyle()
    {
        SimpleAttributeSet tempStyle = new SimpleAttributeSet();
        tempStyle.addAttribute("name", "myStyle");
        StyleConstants.setForeground(tempStyle, Color.GREEN);
        StyleConstants.setUnderline(tempStyle, true);
        StyleConstants.setFontFamily(tempStyle, myFont.getFamily());
        StyleConstants.setFontSize(tempStyle, myFont.getSize());

        return tempStyle;
    }

    public class ClickableText extends AbstractAction
    {
        private String textLink;

        ClickableText(String textLink)
        {
            this.textLink = textLink;
        }

        public void execute()
        {
            if (!textLink.isEmpty() && gui.isClickableLinksEnabled())
            {
                // TODO: This should really pop up a dialog to confirm you want to open the link
                try {
                    Desktop.getDesktop().browse(new URL(textLink).toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            execute();
        }
    }

    /**
     * Inserts a string onto the end of the doc.
     *
     * @param doc
     * @param timeLine
     * @param fromUser
     * @param line
     */
    public void formattedDocument(StyledDocument doc, String timeLine, String fromUser, String line)
    {

        if (myNick.equals(fromUser))
        {
            nameStyle = this.myStyle();
        } else
        {
            if (line.indexOf(myNick) > -1)
                nameStyle = highStyle();
        }

        if (fromUser.equals(Constants.EVENT_USER))
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

            // find and match against any URLs that may be in the text
            Pattern pattern = Pattern.compile(Constants.URL_REGEX);
            Matcher matcher = pattern.matcher(line);
            SimpleAttributeSet linkStyle = urlStyle();

            while (matcher.find()) {
                // pre http
                doc.insertString(doc.getLength(), line.substring(0, matcher.start()), lineStyle);

                // http "clickableText"
                String httpLine = line.substring(matcher.start(), matcher.end());
                linkStyle.addAttribute("clickableText", new ClickableText(httpLine));
                doc.insertString(doc.getLength(), httpLine, linkStyle);

                // post http
                line = line.substring(matcher.end());

                // search again
                matcher = pattern.matcher(line);
            }

            // print the remaining text
            doc.insertString(doc.getLength(), line, lineStyle);

            doc.insertString(doc.getLength(), System.getProperty("line.separator"), lineStyle);
        } catch (BadLocationException e)
        {
            Constants.LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }

}
