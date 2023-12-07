package urChatBasic.frontend;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.prefs.Preferences;
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
import urChatBasic.backend.utils.URStyle;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCServerBase;
import urChatBasic.frontend.dialogs.YesNoDialog;
import urChatBasic.frontend.utils.URColour;

public class LineFormatter
{
    private String myNick;
    private URStyle targetStyle;
    private Color myForeground;
    private Color myBackground;
    private IRCServerBase myServer;
    private Preferences formatterPrefs;
    public URStyle timeStyle;
    public URStyle lineStyle;
    public URStyle nickStyle;
    public URStyle myStyle;

    public LineFormatter(URStyle baseStyle, final IRCServerBase server, Preferences formatterPrefs)
    {
        // TODO: Need to load attributes from formatterPrefs
        this.formatterPrefs = formatterPrefs;

        if (null != server)
        {
            myNick = server.getNick();
            myServer = server;
        } else
        {
            myNick = null;
        }

        targetStyle = new URStyle(myNick, baseStyle.getFont());
        targetStyle.setForeground(baseStyle.getForeground());
        targetStyle.setBackground(baseStyle.getBackground());

        // TODO: should we be using something like UIManager
        // UIManager.getFont for the default fonts isntead?
        myForeground = targetStyle.getForeground();
        myBackground = targetStyle.getBackground();

        timeStyle = defaultStyle(null, true);
        lineStyle = defaultStyle(null, true);
        nickStyle = nickStyle(false);
        myStyle = myStyle(false);
    }

    public void setFont(StyledDocument doc, Font newFont)
    {
        targetStyle.setFont(newFont);
        if (doc.getLength() > 0)
            updateStyles(doc, 0);
    }

    public URStyle defaultStyle(String name, boolean load)
    {
        if (name == null)
            name = "defaultStyle";

        URStyle tempStyle = new URStyle(name, targetStyle.getFont());
        tempStyle.addAttribute("type", "default");
        // get the contrasting colour of the background colour
        // StyleConstants.setForeground(defaultStyle, new Color(formatterPrefs.node(name).getInt("font
        // foreground",
        // URColour.getContrastColour(UIManager.getColor("Panel.background")).getRGB())));

        StyleConstants.setFontFamily(tempStyle, targetStyle.getFont().getFamily());
        StyleConstants.setFontSize(tempStyle, targetStyle.getFont().getSize());
        StyleConstants.setBold(tempStyle, targetStyle.getFont().isBold());
        StyleConstants.setItalic(tempStyle, targetStyle.getFont().isItalic());

        StyleConstants.setForeground(tempStyle, myForeground);
        StyleConstants.setBackground(tempStyle, myBackground);

        if (load)
            tempStyle.load(formatterPrefs);

        return tempStyle;
    }

    public URStyle lowStyle(boolean load)
    {
        String name = "lowStyle";

        URStyle tempStyle = defaultStyle(name, load);


        StyleConstants.setForeground(tempStyle, UIManager.getColor("Panel.background").darker());

        if (StyleConstants.getForeground(tempStyle).getRGB() == myForeground.getRGB())
            if (URColour.useDarkColour(UIManager.getColor("Panel.background")))
            {
                StyleConstants.setForeground(tempStyle, UIManager.getColor("Panel.background").darker());
            } else
            {
                StyleConstants.setForeground(tempStyle, UIManager.getColor("Panel.background").brighter());
            }

        if (load)
            tempStyle.load(formatterPrefs);


        return tempStyle;
    }

    public URStyle mediumStyle(boolean load)
    {
        String name = "mediumStyle";

        URStyle tempStyle = defaultStyle(name, load);

        if (load)
            tempStyle.load(formatterPrefs);

        return tempStyle;
    }

    public URStyle highStyle(boolean load)
    {
        String name = "highStyle";

        URStyle tempStyle = defaultStyle(name, load);

        StyleConstants.setBackground(tempStyle, UIManager.getColor("CheckBoxMenuItem.selectionBackground"));
        StyleConstants.setForeground(tempStyle,
                URColour.getContrastColour(UIManager.getColor("CheckBoxMenuItem.selectionBackground")));

        StyleConstants.setBold(tempStyle, true);
        StyleConstants.setItalic(tempStyle, true);

        if (load)
            tempStyle.load(formatterPrefs);

        return tempStyle;
    }

    public URStyle urlStyle(boolean load)
    {
        String name = "urlStyle";

        URStyle tempStyle = defaultStyle(name, load);

        tempStyle.addAttribute("name", name);
        tempStyle.addAttribute("type", "url");

        StyleConstants.setForeground(tempStyle, UIManager.getColor("CheckBoxMenuItem.selectionBackground"));
        StyleConstants.setBold(tempStyle, true);
        StyleConstants.setUnderline(tempStyle, true);

        if (load)
            tempStyle.load(formatterPrefs);

        return tempStyle;
    }

    // TODO: urlStyle and channelStyle don't load the correct styling in the fontPanel

    public URStyle channelStyle(boolean load)
    {
        String name = "channelStyle";

        URStyle tempStyle = defaultStyle(name, load);

        tempStyle.addAttribute("name", name);
        tempStyle.addAttribute("type", "channel");

        StyleConstants.setForeground(tempStyle, UIManager.getColor("CheckBoxMenuItem.selectionBackground"));
        StyleConstants.setBold(tempStyle, true);
        StyleConstants.setUnderline(tempStyle, true);

        if (load)
            tempStyle.load(formatterPrefs);

        return tempStyle;
    }

    public URStyle myStyle(boolean load)
    {
        String name = "myStyle";

        URStyle tempStyle = defaultStyle(name, load);
        tempStyle.addAttribute("type", "myNick");

        // StyleConstants.setForeground(tempStyle, Color.GREEN);
        StyleConstants.setForeground(tempStyle,
                URColour.getInvertedColour(UIManager.getColor("CheckBoxMenuItem.selectionBackground")));

        StyleConstants.setBold(tempStyle, true);
        StyleConstants.setUnderline(tempStyle, true);

        if (load)
            tempStyle.load(formatterPrefs);

        return tempStyle;
    }

    public URStyle nickStyle(boolean load)
    {
        String name = "nickStyle";

        URStyle tempStyle = defaultStyle(name, load);
        tempStyle.addAttribute("type", "nick");

        StyleConstants.setUnderline(tempStyle, true);

        if (load)
            tempStyle.load(formatterPrefs);

        return tempStyle;
    }

    public void setNick(String myNick)
    {
        this.myNick = myNick;
    }

    public class ClickableText extends AbstractAction
    {
        private String textLink;
        private URStyle attributeSet;
        private IRCUser fromUser;

        ClickableText(String textLink, URStyle attributeSet, IRCUser fromUser)
        {
            this.textLink = textLink;
            this.attributeSet = attributeSet;

            if (fromUser != null)
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
                try
                {
                    AtomicBoolean doOpenLink = new AtomicBoolean(false);

                    YesNoDialog confirmOpenLink = new YesNoDialog("Are you sure you want to open " + textLink + "?",
                            "Open Link", JOptionPane.QUESTION_MESSAGE,
                            e -> doOpenLink.set(e.getActionCommand().equalsIgnoreCase("Yes")));

                    confirmOpenLink.setVisible(true);

                    if (doOpenLink.get())
                        Desktop.getDesktop().browse(new URL(textLink).toURI());
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            } else if (!textLink.isEmpty() && attributeSet.getAttribute("type").equals("channel"))
            {
                try
                {
                    AtomicBoolean doJoinChannel = new AtomicBoolean(false);

                    YesNoDialog confirmOpenLink =
                            new YesNoDialog("Are you sure you want to join channel " + textLink + "?", "Join Channel",
                                    JOptionPane.QUESTION_MESSAGE,
                                    e -> doJoinChannel.set(e.getActionCommand().equalsIgnoreCase("Yes")));

                    confirmOpenLink.setVisible(true);

                    if (doJoinChannel.get())
                    {
                        myServer.sendClientText("/join " + textLink, "");
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        public JPopupMenu rightClickMenu()
        {
            if (attributeSet.getAttribute("type").equals("IRCUser"))
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
    private void insertString(StyledDocument doc, String insertedString, SimpleAttributeSet style, int position)
            throws BadLocationException
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
    private void appendString(StyledDocument doc, String insertedString, SimpleAttributeSet style)
            throws BadLocationException
    {
        int position = doc.getLength();

        insertString(doc, insertedString, style, position);
    }

    public URStyle getStyleBase(String styleName, boolean load)
    {
        switch (styleName)
        {
            case "mediumStyle":
                return mediumStyle(load);
            case "highStyle":
                return highStyle(load);
            case "nickStyle":
                return nickStyle(load);
            case "myStyle":
                return myStyle(load);
            case "lowStyle":
                return lowStyle(load);
            case "urlStyle":
                return urlStyle(load);
            case "channelStyle":
                return channelStyle(load);
            default:
                return defaultStyle(null, true);
        }
    }

    // public Font getStyleAsFont(String styleName)
    // {
    // SimpleAttributeSet fontStyle = getStyleBase(styleName);

    // int savedFontBoldItalic = 0;

    // if (StyleConstants.isBold(fontStyle))
    // savedFontBoldItalic = Font.BOLD;
    // if (StyleConstants.isItalic(fontStyle))
    // savedFontBoldItalic |= Font.ITALIC;

    // Font styleFont = new Font(StyleConstants.getFontFamily(fontStyle), savedFontBoldItalic,
    // StyleConstants.getFontSize(fontStyle));

    // return styleFont;
    // }

    public void updateStyles(StyledDocument doc, int startPosition)
    {
        SimpleAttributeSet textStyle = new SimpleAttributeSet(doc.getCharacterElement(startPosition).getAttributes());

        String styleName = textStyle.getAttribute("name").toString();
        int styleStart = startPosition;
        int styleLength = Integer.parseInt(textStyle.getAttribute("styleLength").toString());

        SimpleAttributeSet matchingStyle = getStyleBase(styleName, true);

        boolean isDateStyle = false;
        if (null != DriverGUI.gui && null != textStyle.getAttribute("date"))
        {
            isDateStyle = true;
            try
            {
                Date lineDate = (Date) textStyle.getAttribute("date");
                String newTimeString = UserGUI.getTimeLineString(lineDate) + " ";
                boolean hasTime = false;

                if (null != textStyle.getAttribute("type")
                        && textStyle.getAttribute("type").toString().equalsIgnoreCase("time"))
                {
                    hasTime = true;
                    doc.remove(styleStart, styleLength);
                }

                if (DriverGUI.gui.isTimeStampsEnabled())
                {
                    textStyle.removeAttribute("date");
                    textStyle.removeAttribute("time");

                    if (!hasTime)
                        doc.setCharacterAttributes(styleStart, styleLength, textStyle, true);

                    SimpleAttributeSet timeStyle = getStyleBase(styleName, true);
                    timeStyle.addAttribute("date", lineDate);
                    timeStyle.addAttribute("type", "time");
                    insertString(doc, newTimeString, timeStyle, styleStart);
                    styleLength = newTimeString.length();
                } else
                {
                    if (hasTime)
                    {
                        textStyle = new SimpleAttributeSet(doc.getCharacterElement(startPosition).getAttributes());

                        styleName = textStyle.getAttribute("name").toString();
                        styleStart = startPosition;
                        styleLength = Integer.parseInt(textStyle.getAttribute("styleLength").toString());

                        matchingStyle = getStyleBase(styleName, true);
                        matchingStyle.addAttribute("date", lineDate);

                        isDateStyle = false;
                    }
                }
            } catch (BadLocationException $ble)
            {
                //
            }
        }

        // Copy the attributes, but only if they aren't already set
        Iterator<?> attributeIterator = textStyle.getAttributeNames().asIterator();
        while (attributeIterator.hasNext())
        {
            String nextAttributeName = attributeIterator.next().toString();

            if (matchingStyle.getAttribute(nextAttributeName) == null)
            {
                Iterator<?> matchingIterator = matchingStyle.getAttributeNames().asIterator();
                boolean needsToBeSet = true;

                while (matchingIterator.hasNext())
                {
                    if (matchingIterator.next().toString().equalsIgnoreCase(nextAttributeName))
                    {
                        needsToBeSet = false;
                        break;
                    }
                }
                if (needsToBeSet)
                    matchingStyle.addAttribute(nextAttributeName, textStyle.getAttribute(nextAttributeName));
            }
        }

        if (!isDateStyle)
            doc.setCharacterAttributes(styleStart, styleLength, matchingStyle, true);

        if ((styleStart + styleLength) < doc.getLength())
            updateStyles(doc, (styleStart + styleLength));
    }

    public String getLatestLine(StyledDocument doc) throws BadLocationException
    {
        Element root = doc.getDefaultRootElement();
        int lines = root.getElementCount();

        String finalLine = "";

        while (finalLine.isEmpty())
        {

            if (lines < 0)
                break;

            Element line = root.getElement(lines--);

            if (null == line)
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

            if (null == line)
                continue;

            int start = line.getStartOffset();
            int end = line.getEndOffset();
            String text = doc.getText(start, end - start);

            if (text.trim().equals(targetLine.trim()))
            {
                return start;
            }
        }

        return 0;
    }

    public SimpleAttributeSet getStyleAtPosition(StyledDocument doc, int position, String relativeLine)
            throws BadLocationException
    {
        if (!relativeLine.isBlank())
            position = position + getLinePosition(doc, relativeLine);

        AttributeSet textStyle = doc.getCharacterElement(position).getAttributes();

        return new SimpleAttributeSet(textStyle);
    }

    private void parseClickableText(StyledDocument doc, IRCUser fromUser, String line, URStyle defaultStyle)
            throws BadLocationException
    {
        HashMap<String, URStyle> regexStrings = new HashMap<>();
        regexStrings.put(Constants.URL_REGEX, urlStyle(true));
        regexStrings.put(Constants.CHANNEL_REGEX, channelStyle(true));
        // final String line = getLatestLine(doc);
        final int relativePosition = getLinePosition(doc, getLatestLine(doc));

        ArrayList<URStyle> clickableLines = new ArrayList<URStyle>();

        for (Map.Entry<String, URStyle> entry : regexStrings.entrySet())
        {
            String regex = entry.getKey();


            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(line);

            // do stuff for each match
            while (matcher.find())
            {
                URStyle linkStyle = getStyleBase(entry.getValue().getAttribute("name").toString(), true);
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

        Iterator<URStyle> linesIterator = clickableLines.iterator();
        String remainingLine = line;
        while (linesIterator.hasNext())
        {
            URStyle nextLine = linesIterator.next();

            // Offset based on the difference between the original line and the remaining line,
            // plus the relativePosition within the document.
            int offset = (line.length() - remainingLine.length()) + relativePosition;
            int nextLineStart = Integer.parseInt(nextLine.getAttribute("styleStart").toString());
            int nextLineLength = Integer.parseInt(nextLine.getAttribute("styleLength").toString());

            // Append the string that comes before the next clickable text
            appendString(doc, remainingLine.substring(0, nextLineStart - offset), defaultStyle);

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
            nickStyle = myStyle(true);
        } else
        {
            if (null != myNick && line.indexOf(myNick) > -1)
                nickStyle = highStyle(true);
            else
                nickStyle = nickStyle(true);
        }

        if (fromUser == null && fromString.equals(Constants.EVENT_USER))
        {
            nickStyle = lowStyle(true);
            lineStyle = lowStyle(true);
        } else
        {
            lineStyle = defaultStyle(null, true);
        }

        timeStyle = lineStyle;

        try
        {

            // doc.insertString(doc.getLength(), timeLine, timeStyle);
            // if(null != timeLine && !timeLine.isBlank())
            if (!timeLine.isBlank() && DriverGUI.gui.isTimeStampsEnabled())
            {
                // add the date to the end of the string to preserve the timestamp of the line
                // when updating styles
                timeStyle.addAttribute("date", lineDate);
                timeStyle.removeAttribute("type");
                timeStyle.addAttribute("type", "time");
                appendString(doc, timeLine + " ", timeStyle);
                timeStyle.removeAttribute("type");
                lineStyle.removeAttribute("date");
            } else
            {
                lineStyle.addAttribute("date", lineDate);
            }

            appendString(doc, "<", lineStyle);
            lineStyle.removeAttribute("date");

            if (fromUser != null)
            {
                URStyle clickableNameStyle = nickStyle;
                clickableNameStyle.addAttribute("type", "IRCUser");
                clickableNameStyle.addAttribute("clickableText",
                        new ClickableText(fromUser.toString(), nickStyle, fromUser));

                // doc.insertString(doc.getLength(), fromUser.toString(), clickableNameStyle);
                appendString(doc, fromUser.toString(), clickableNameStyle);
            } else
            {
                appendString(doc, fromString, nickStyle);
            }

            appendString(doc, ">", lineStyle);

            // print the remaining text
            // appendString(doc, " "+line, lineStyle);

            // parse the outputted line for clickable text
            parseClickableText(doc, fromUser, " " + line, lineStyle);

            appendString(doc, System.getProperty("line.separator"), lineStyle);
        } catch (BadLocationException e)
        {
            Constants.LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }

}
