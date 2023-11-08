package urChatBasic.frontend;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

        if(URColour.useDarkColour(UIManager.getColor("Panel.background")))
        {
            StyleConstants.setForeground(tempStyle, Color.DARK_GRAY);
        } else {
            StyleConstants.setForeground(tempStyle, Color.LIGHT_GRAY);
        }


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

        @Override
        public String toString()
        {
            return textLink;
        }

        public void execute()
        {
            if (!textLink.isEmpty() && attributeSet.getAttribute("type").equals("url"))
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

    // Inserts the string at the position
    private void insertString(StyledDocument doc, String insertedString, SimpleAttributeSet style, int position) throws BadLocationException
    {
        // remove the existing attributes
        style.removeAttribute("styleStart");
        style.removeAttribute("styleLength");

        // add an attribute so we know when the style is expected to start and end.
        style.addAttribute("styleStart", position);
        style.addAttribute("styleLength", insertedString.length());
        doc.insertString(position, insertedString, style);
    }

    // Adds the string (with all needed attributes) to the end of the document
    private void appendString(StyledDocument doc, String insertedString, SimpleAttributeSet style) throws BadLocationException
    {
        int position = doc.getLength();

        insertString(doc, insertedString, style, position);
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
        SimpleAttributeSet textStyle = new SimpleAttributeSet(doc.getCharacterElement(startPosition).getAttributes());

        String styleName = textStyle.getAttribute("name").toString();
        int styleStart = startPosition;
        int styleLength = Integer.parseInt(textStyle.getAttribute("styleLength").toString());

        SimpleAttributeSet matchingStyle = getStyle(styleName);

        // TODO: Update the time format. Check if there is a timeStyle already, if there is then remove it from the line
        // and insert the new timeStyle/Format. Otherwise we just need to insert it. The first character/style will have
        // the 'date' attribute of when the line was added.

        boolean isDateStyle = false;
        if (null != gui && null != textStyle.getAttribute("date"))
        {
            isDateStyle = true;
            try
            {
                Date lineDate = (Date) textStyle.getAttribute("date");
                String newTimeString = gui.getTimeLineString(lineDate) + " ";
                boolean hasTime = false;

                if (textStyle.getAttribute("type").toString().equalsIgnoreCase("time"))
                {
                    hasTime = true;
                    doc.remove(styleStart, styleLength);
                }

                if(gui.isTimeStampsEnabled())
                {
                    textStyle.removeAttribute("date");
                    textStyle.removeAttribute("time");

                    if(!hasTime)
                        doc.setCharacterAttributes(styleStart, styleLength, textStyle, true);

                    SimpleAttributeSet timeStyle = getStyle(styleName);
                    timeStyle.addAttribute("date", lineDate);
                    timeStyle.addAttribute("type", "time");
                    insertString(doc, newTimeString, timeStyle, styleStart);
                    styleLength = newTimeString.length();
                }
            } catch (BadLocationException $ble)
            {
                //
            }
        }

        // Copy the attributes, but only if they aren't already set
        Iterator attributeIterator = textStyle.getAttributeNames().asIterator();
        while(attributeIterator.hasNext())
        {
            String nextAttributeName = attributeIterator.next().toString();

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

        if(!isDateStyle)
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

        return new SimpleAttributeSet(textStyle);
    }

    private void parseClickableText(StyledDocument doc, IRCUser fromUser, String line, SimpleAttributeSet defaultStyle) throws BadLocationException
    {
        HashMap<String, SimpleAttributeSet> regexStrings = new HashMap<>();
        regexStrings.put(Constants.URL_REGEX, urlStyle());
        regexStrings.put(Constants.CHANNEL_REGEX, channelStyle());
        // final String line = getLatestLine(doc);
        final int relativePosition = getLinePosition(doc, getLatestLine(doc));

        ArrayList<SimpleAttributeSet> clickableLines = new ArrayList<SimpleAttributeSet>();

        for (Map.Entry<String, SimpleAttributeSet> entry : regexStrings.entrySet()) {
            String regex = entry.getKey();


            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(line);

            // do stuff for each match
            while (matcher.find()) {
                SimpleAttributeSet linkStyle = getStyle(entry.getValue().getAttribute("name").toString());
                String clickableLine = matcher.group(1);
                linkStyle.addAttribute("clickableText", new ClickableText(clickableLine, linkStyle, fromUser));

                int styleStart = relativePosition + matcher.start(1);
                int styleLength = clickableLine.length();

                linkStyle.addAttribute("styleStart", styleStart);
                linkStyle.addAttribute("styleLength", styleLength);

                clickableLines.add(linkStyle);
            }
        }

        clickableLines.sort((set1, set2) -> {
            int styleStart1 = (int) set1.getAttribute("styleStart");
            int styleStart2 = (int) set2.getAttribute("styleStart");
            return Integer.compare(styleStart1, styleStart2);
        });

        Iterator<SimpleAttributeSet> linesIterator = clickableLines.iterator();
        String remainingLine = line;
        while (linesIterator.hasNext())
        {
            SimpleAttributeSet nextLine = linesIterator.next();

            // Offset based on the difference between the original line and the remaining line,
            // plus the relativePosition within the document.
            int offset = (line.length() - remainingLine.length()) + relativePosition;
            int nextLineStart = Integer.parseInt(nextLine.getAttribute("styleStart").toString());
            int nextLineLength = Integer.parseInt(nextLine.getAttribute("styleLength").toString());

            // Append the string that comes before the next clickable text
            appendString(doc, remainingLine.substring(0,  nextLineStart- offset), defaultStyle);

            appendString(doc, nextLine.getAttribute("clickableText").toString(), nextLine);

            remainingLine = remainingLine.substring((nextLineStart + nextLineLength) - offset);
        }

        appendString(doc, remainingLine, defaultStyle);
    }

    /**
     * Inserts a string onto the end of the doc.
     *
     * @param doc
     * @param timeLine
     * @param fromUser
     * @param line
     */
    public void formattedDocument(StyledDocument doc, Date lineDate, IRCUser fromUser, String fromString, String line)
    {
        // build the timeLine string
        String timeLine = UserGUI.getTimeLineString(lineDate);

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

        timeStyle = lineStyle;

        try
        {

            // doc.insertString(doc.getLength(), timeLine, timeStyle);
            // if(null != timeLine && !timeLine.isBlank())
            if(!timeLine.isBlank())
            {
                // add the date to the end of the string to preserve the timestamp of the line
                // when updating styles
                timeStyle.addAttribute("date", lineDate);
                timeStyle.removeAttribute("type");
                timeStyle.addAttribute("type", "time");
                appendString(doc, timeLine + " ", timeStyle);
                timeStyle.removeAttribute("type");
            } else {
                lineStyle.addAttribute("date", lineDate);
            }

            appendString(doc, "<", lineStyle);
            lineStyle.removeAttribute("date");

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
            // appendString(doc, " "+line, lineStyle);

            // parse the outputted line for clickable text
            parseClickableText(doc, fromUser, " "+line, lineStyle);

            appendString(doc, System.getProperty("line.separator"), lineStyle);
        } catch (BadLocationException e)
        {
            Constants.LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }

}
