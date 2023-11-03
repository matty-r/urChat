package urChatBasic.frontend;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCServerBase;
import urChatBasic.frontend.dialogs.YesNoDialog;
import urChatBasic.frontend.utils.URColour;

public class LineFormatter
{
    private String myNick;
    private Font myFont;
    private IRCServerBase myServer;
    public SimpleAttributeSet defaultStyle;
    public SimpleAttributeSet timeStyle;
    public SimpleAttributeSet nameStyle;
    public SimpleAttributeSet lineStyle;
    protected UserGUI gui = DriverGUI.gui;

    public LineFormatter(Font myFont, final IRCServerBase server)
    {
        // myNick = server.getNick();

        if(null != server)
        {
            myNick = server.getNick();
            myServer = server;
        }
        else
        {
            myNick = null;
        }

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
        defaultStyle.addAttribute("type", "default");
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

    public SimpleAttributeSet channelStyle()
    {
        SimpleAttributeSet tempStyle = urlStyle();

        tempStyle.addAttribute("name", "channelStyle");
        tempStyle.addAttribute("type", "channel");

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

    public void setNick(String myNick)
    {
        this.myNick = myNick;
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
                try {
                    AtomicBoolean doOpenLink = new AtomicBoolean(false);

                    YesNoDialog confirmOpenLink = new YesNoDialog("Are you sure you want to open "+textLink+"?", "Open Link",
                        JOptionPane.QUESTION_MESSAGE, e -> doOpenLink.set(e.getActionCommand().equalsIgnoreCase("Yes")));

                    confirmOpenLink.setVisible(true);

                    if(doOpenLink.get())
                        Desktop.getDesktop().browse(new URL(textLink).toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(!textLink.isEmpty() && attributeSet.getAttribute("type").equals("channel"))
            {
                try {
                    AtomicBoolean doJoinChannel = new AtomicBoolean(false);

                    YesNoDialog confirmOpenLink = new YesNoDialog("Are you sure you want to join channel "+textLink+"?", "Join Channel",
                        JOptionPane.QUESTION_MESSAGE, e -> doJoinChannel.set(e.getActionCommand().equalsIgnoreCase("Yes")));

                    confirmOpenLink.setVisible(true);

                    if(doJoinChannel.get())
                    {
                        myServer.sendClientText("/join " + textLink, "");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public JPopupMenu rightClickMenu()
        {
            if(attributeSet.getAttribute("type").equals("IRCUser"))
            {
                fromUser.createPopUp();
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
            case "urlStyle":
                return urlStyle();
            case "channelStyle":
                return channelStyle();
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

        if(styleName.equalsIgnoreCase("urlStyle"))
        {
            System.out.println("sda");
        }

        // Copy the attributes, but only if they aren't already set
        Iterator attributeIterator = textStyle.getAttributeNames().asIterator();
        while(attributeIterator.hasNext())
        {
            String nextAttributeName = attributeIterator.next().toString();

            // get attribute "foreground" isn't working here despite foregrounf having been set
            if(matchingStyle.getAttribute(nextAttributeName) == null)
            {
                Iterator matchingIterator = matchingStyle.getAttributeNames().asIterator();
                boolean needsToBeSet = true;

                while(matchingIterator.hasNext())
                {
                    if(matchingIterator.next().toString().equalsIgnoreCase(nextAttributeName))
                    {
                        needsToBeSet = false;
                        break;
                    }
                }
                if(needsToBeSet)
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

        // String styleName = textStyle.getAttribute("name").toString();
        return new SimpleAttributeSet(textStyle);
    }

    private String parseClickableText(StyledDocument doc, IRCUser fromUser) throws BadLocationException
    {
        HashMap<String, SimpleAttributeSet> regexStrings = new HashMap<>();
        regexStrings.put(Constants.URL_REGEX, urlStyle());
        regexStrings.put(Constants.CHANNEL_REGEX, channelStyle());
        final String line = getLatestLine(doc);
        final int relativePosition = getLinePosition(doc, getLatestLine(doc));

        for (Map.Entry<String, SimpleAttributeSet> entry : regexStrings.entrySet()) {
            String regex = entry.getKey();
            SimpleAttributeSet linkStyle = entry.getValue();

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(line);

            // do stuff for each match
            while (matcher.find()) {

                String clickableLine = line.substring(matcher.start(), matcher.end());
                linkStyle.addAttribute("clickableText", new ClickableText(clickableLine, linkStyle, fromUser));

                int styleStart = relativePosition + matcher.start();
                int styleLength = matcher.end() - matcher.start();

                linkStyle.addAttribute("styleStart", styleStart);
                linkStyle.addAttribute("styleLength", styleLength);

                // update the styleLength of the previous style
                SimpleAttributeSet oldStyle = getStyleAtPosition(doc, styleStart - 1, "");
                SimpleAttributeSet replaceStyle = getStyle(oldStyle.getAttribute("name").toString());
                int newStart = Integer.parseInt(oldStyle.getAttribute("styleStart").toString());
                int newLength = styleStart - Integer.parseInt(oldStyle.getAttribute("styleStart").toString()) + 1;
                replaceStyle.addAttribute("styleStart", newStart);
                replaceStyle.addAttribute("styleLength", newLength);
                replaceStyle.addAttribute("docLength", oldStyle.getAttribute("docLength"));

                doc.setCharacterAttributes(newStart, newLength, replaceStyle, true);

                doc.setCharacterAttributes(styleStart, styleLength, linkStyle, true);
            }
        }

        return line;
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
            if(null != timeLine && !timeLine.isBlank())
                appendString(doc, timeLine + " ", timeStyle);

            appendString(doc, "<", lineStyle);

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

            appendString(doc, ">", lineStyle);

            // print the remaining text
            appendString(doc, " "+line, lineStyle);

            // parse the outputted line for clickable text
            parseClickableText(doc, fromUser);

            appendString(doc, System.getProperty("line.separator"), lineStyle);
        } catch (BadLocationException e)
        {
            Constants.LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }

}
