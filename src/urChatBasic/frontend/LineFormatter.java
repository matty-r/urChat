package urChatBasic.frontend;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import urChatBasic.base.Constants;
import urChatBasic.frontend.utils.URColour;

public class LineFormatter
{
    private String myNick;
    private Font myFont;
    public SimpleAttributeSet defaultStyle;
    public SimpleAttributeSet timeStyle;
    public SimpleAttributeSet nameStyle;
    public SimpleAttributeSet lineStyle;
    protected UserGUI gui = DriverGUI.gui;

    public LineFormatter(Font myFont, String myNick)
    {
        this.myNick = myNick;
        this.myFont = myFont;
        defaultStyle = defaultStyle();
        timeStyle = defaultStyle();
        nameStyle = defaultStyle();
        lineStyle = defaultStyle();
    }

    public void setFont(StyledDocument doc, Font newFont)
    {
        myFont = newFont;
        if(doc.getLength() > 0)
            updateStyles(doc, 0);
    }

    public SimpleAttributeSet defaultStyle()
    {
        SimpleAttributeSet defaultStyle = new SimpleAttributeSet();
        defaultStyle.addAttribute("name", "defaultStyle");

        // get the contrasting colour of the background colour
        StyleConstants.setForeground(defaultStyle, URColour.getContrastColour(UIManager.getColor("Panel.background")));
        StyleConstants.setFontFamily(defaultStyle, myFont.getFamily());
        StyleConstants.setFontSize(defaultStyle, myFont.getSize());
        StyleConstants.setBold(defaultStyle, myFont.isBold());
        StyleConstants.setItalic(defaultStyle, myFont.isItalic());

        return defaultStyle;
    }

    public SimpleAttributeSet lowStyle()
    {
        SimpleAttributeSet tempStyle = defaultStyle();
        tempStyle.addAttribute("name", "lowStyle");
        StyleConstants.setForeground(tempStyle, Color.LIGHT_GRAY);

        return tempStyle;
    }

    public SimpleAttributeSet mediumStyle()
    {

        SimpleAttributeSet tempStyle = defaultStyle();
        tempStyle.addAttribute("name", "mediumStyle");
        // StyleConstants.setBackground(tempStyle, Color.YELLOW);

        return tempStyle;
    }

    public SimpleAttributeSet highStyle()
    {
        SimpleAttributeSet tempStyle = defaultStyle();
        tempStyle.addAttribute("name", "highStyle");

        StyleConstants.setBackground(tempStyle, UIManager.getColor("CheckBoxMenuItem.selectionBackground")); // TODO: Get highlight colour?
        StyleConstants.setForeground(tempStyle, URColour.getContrastColour(UIManager.getColor("CheckBoxMenuItem.selectionBackground")));
        StyleConstants.setBold(tempStyle, true);
        StyleConstants.setItalic(tempStyle, true);

        return tempStyle;
    }

    public SimpleAttributeSet urlStyle()
    {
        SimpleAttributeSet tempStyle = defaultStyle();

        tempStyle.addAttribute("name", "urlStyle");
        tempStyle.addAttribute("type", "url");
        StyleConstants.setForeground(tempStyle, UIManager.getColor("CheckBoxMenuItem.selectionBackground"));

        StyleConstants.setUnderline(tempStyle, true);

        return tempStyle;
    }

    public SimpleAttributeSet myStyle()
    {
        SimpleAttributeSet tempStyle = defaultStyle();
        tempStyle.addAttribute("name", "myStyle");
        // StyleConstants.setForeground(tempStyle, Color.GREEN);
        StyleConstants.setForeground(tempStyle, URColour.getInvertedColour(UIManager.getColor("CheckBoxMenuItem.selectionBackground")));

        StyleConstants.setBold(tempStyle, true);
        StyleConstants.setUnderline(tempStyle, true);

        return tempStyle;
    }

    public class ClickableText extends AbstractAction
    {
        private String textLink;
        private SimpleAttributeSet attributeSet;
        private IRCUser fromUser;

        ClickableText(String textLink, SimpleAttributeSet attributeSet, IRCUser fromUser)
        {
            this.textLink = textLink;
            this.attributeSet = attributeSet;

            if(fromUser != null)
            {
                this.fromUser = fromUser;
            }
        }

        public void execute()
        {
            if (!textLink.isEmpty() && gui.isClickableLinksEnabled() && attributeSet.getAttribute("type").equals("url"))
            {
                // TODO: This should really pop up a dialog to confirm you want to open the link
                try {
                    Desktop.getDesktop().browse(new URL(textLink).toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public JPopupMenu rightClickMenu()
        {
            if(attributeSet.getAttribute("type").equals("IRCUser"))
            {
                return fromUser.myMenu;
            }

            // TODO: Build the right-click menu for other types, i.e URLs

            return null;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            execute();
        }
    }

    private void appendString(StyledDocument doc, String insertedString, SimpleAttributeSet style) throws BadLocationException
    {
        int position = doc.getLength();
        // add an attribute so we know when the style is expected to start and end.
        style.addAttribute("styleStart", position);
        style.addAttribute("styleLength", insertedString.length());
        style.addAttribute("docLength", doc.getLength());
        doc.insertString(position, insertedString, style);
    }

    private SimpleAttributeSet getStyle(String styleName)
    {
        switch (styleName) {
            case "mediumStyle":
                return mediumStyle();
            case "highStyle":
                return highStyle();
            case "myStyle":
                return myStyle();
            case "lowStyle":
                return lowStyle();
            default:
                return defaultStyle();
        }
    }


    public void updateStyles(StyledDocument doc, int startPosition)
    {
        AttributeSet textStyle = doc.getCharacterElement(startPosition).getAttributes();

        String styleName = textStyle.getAttribute("name").toString();
        int styleStart = Integer.parseInt(textStyle.getAttribute("styleStart").toString());
        int styleLength = Integer.parseInt(textStyle.getAttribute("styleLength").toString());

        SimpleAttributeSet matchingStyle = getStyle(styleName);

        // Copy the attributes, but only if they aren't already set
        Iterator attributeIterator = textStyle.getAttributeNames().asIterator();
        while(attributeIterator.hasNext())
        {
            String nextAttributeName = attributeIterator.next().toString();
            if(matchingStyle.getAttribute(nextAttributeName) == null)
            {
                matchingStyle.addAttribute(nextAttributeName, textStyle.getAttribute(nextAttributeName));
            }
        }


        doc.setCharacterAttributes(styleStart, styleLength, matchingStyle, true);

        if((styleStart + styleLength) < doc.getLength())
            updateStyles(doc, (styleStart + styleLength));
    }

    public String getLatestLine(StyledDocument doc) throws BadLocationException
    {
        Element root = doc.getDefaultRootElement();
        int lines = root.getElementCount();

        String finalLine = "";

        while(finalLine.isEmpty())
        {

            if(lines < 0)
                break;

            Element line = root.getElement( lines-- );

            if(null == line)
                continue;

            int start = line.getStartOffset();
            int end = line.getEndOffset();
            String text = doc.getText(start, end - start);
            finalLine = text.trim();
        }

        return finalLine;
    }

    private int getLinePosition(StyledDocument doc, String targetLine) throws BadLocationException
    {
        Element root = doc.getDefaultRootElement();
        int lines = root.getElementCount();

        for (int i = 0; i < lines; i++)
        {
            Element line = root.getElement(i);

            if(null == line)
                continue;

            int start = line.getStartOffset();
            int end = line.getEndOffset();
            String text = doc.getText(start, end - start);

            if(text.trim().equals(targetLine.trim()))
            {
                return start;
            }
        }

        return 0;
    }

    public SimpleAttributeSet getStyleAtPosition(StyledDocument doc, int position, String relativeLine) throws BadLocationException
    {
        if(!relativeLine.isBlank())
            position = position + getLinePosition(doc, relativeLine);

        AttributeSet textStyle = doc.getCharacterElement(position).getAttributes();
        String styleName = textStyle.getAttribute("name").toString();
        return getStyle(styleName);
    }

    /**
     * Inserts a string onto the end of the doc.
     *
     * @param doc
     * @param timeLine
     * @param fromUser
     * @param line
     */
    public void formattedDocument(StyledDocument doc, String timeLine, IRCUser fromUser, String fromString, String line)
    {
        if (fromUser != null && null != myNick && myNick.equals(fromUser.toString()))
        {
            nameStyle = this.myStyle();
        } else
        {
            if (null != myNick && line.indexOf(myNick) > -1)
                nameStyle = highStyle();
            else
                nameStyle = defaultStyle();
        }

        if (fromUser == null && fromString.equals(Constants.EVENT_USER))
        {
            nameStyle = lowStyle();
            lineStyle = lowStyle();
        } else {
            lineStyle = defaultStyle();
        }

        timeStyle = defaultStyle();

        try
        {

            // doc.insertString(doc.getLength(), timeLine, timeStyle);
            appendString(doc, timeLine, timeStyle);
            appendString(doc, " <", lineStyle);

            if(fromUser != null)
            {
                SimpleAttributeSet clickableNameStyle = nameStyle;
                clickableNameStyle.addAttribute("type", "IRCUser");
                clickableNameStyle.addAttribute("clickableText", new ClickableText(fromUser.toString(), nameStyle, fromUser));

                // doc.insertString(doc.getLength(), fromUser.toString(), clickableNameStyle);
                appendString(doc, fromUser.toString(), clickableNameStyle);
            } else {
                appendString(doc, fromString, nameStyle);
            }

            appendString(doc, "> ", lineStyle);

            // find and match against any URLs that may be in the text
            Pattern pattern = Pattern.compile(Constants.URL_REGEX);
            Matcher matcher = pattern.matcher(line);
            SimpleAttributeSet linkStyle = urlStyle();

            while (matcher.find()) {
                // pre http
                appendString(doc, line.substring(0, matcher.start()), lineStyle);

                // http "clickableText"
                String httpLine = line.substring(matcher.start(), matcher.end());
                linkStyle.addAttribute("clickableText", new ClickableText(httpLine, linkStyle, fromUser));
                appendString(doc, httpLine, linkStyle);

                // post http
                line = line.substring(matcher.end());

                // search again
                matcher = pattern.matcher(line);
            }

            // print the remaining text
            appendString(doc, line, lineStyle);

            appendString(doc, System.getProperty("line.separator"), lineStyle);
        } catch (BadLocationException e)
        {
            Constants.LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }

}
