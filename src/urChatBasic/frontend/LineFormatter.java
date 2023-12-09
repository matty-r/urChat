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
    private URStyle urlStyle;
    private URStyle channelStyle;
    private URStyle timeStyle;
    private URStyle lineStyle;
    private URStyle nickStyle;
    private URStyle highStyle;
    private URStyle mediumStyle;
    private URStyle lowStyle;
    public URStyle myStyle;
    private Map<String, URStyle> formatterStyles = new HashMap<>();


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

        // TODO: split this mess out to a method
        timeStyle = defaultStyle(null, true);
        lineStyle = defaultStyle(null, true);
        nickStyle = nickStyle(true);
        myStyle = myStyle(true);
        channelStyle = channelStyle(true);
        urlStyle = urlStyle(true);
        highStyle = highStyle(true);
        mediumStyle = mediumStyle(true);
        lowStyle = lowStyle(true);

        formatterStyles.put(timeStyle.getName(), timeStyle);
        formatterStyles.put(lineStyle.getName(), lineStyle);
        formatterStyles.put(nickStyle.getName(), nickStyle);
        formatterStyles.put(myStyle.getName(), myStyle);
        formatterStyles.put(channelStyle.getName(), channelStyle);
        formatterStyles.put(urlStyle.getName(), urlStyle);
        formatterStyles.put(highStyle.getName(), highStyle);
        formatterStyles.put(mediumStyle.getName(), mediumStyle);
        formatterStyles.put(lowStyle.getName(), lowStyle);
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
        // TODO: Might need to readjust this again?
        URStyle currentStyle = formatterStyles.get(styleName).clone();
        if(load)
        {
            currentStyle.load(formatterPrefs);
        }

        return currentStyle;
        // switch (styleName)
        // {
        //     case "mediumStyle":
        //         return mediumStyle(load);
        //     case "highStyle":
        //         return highStyle(load);
        //     case "nickStyle":
        //         return nickStyle(load);
        //     case "myStyle":
        //         return myStyle(load);
        //     case "lowStyle":
        //         return lowStyle(load);
        //     case "urlStyle":
        //         return urlStyle(load);
        //     case "channelStyle":
        //         return channelStyle(load);
        //     default:
        //         return defaultStyle(null, true);
        // }
    }

    /**
     * Reloads all the styles, then updates the doc
     * @param doc
     * @param startPosition
     */
    public void updateStyles(StyledDocument doc, int startPosition)
    {
        targetStyle.load(formatterPrefs);

        for (URStyle formatterStyle : formatterStyles.values()) {
            formatterStyle = getStyleBase(formatterStyle.getName(), true);
        }

        System.out.println("Updating styles.");
        updateDocStyles(doc, startPosition);
    }

    private void updateDocStyles(StyledDocument doc, int startPosition)
    {
        SimpleAttributeSet textStyle = new SimpleAttributeSet(doc.getCharacterElement(startPosition).getAttributes());

        String styleName = textStyle.getAttribute("name").toString();
        int styleStart = startPosition;
        int styleLength = Integer.parseInt(textStyle.getAttribute("styleLength").toString());

        SimpleAttributeSet matchingStyle = getStyleBase(styleName, false);

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
            updateDocStyles(doc, (styleStart + styleLength));
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
        regexStrings.put(Constants.URL_REGEX, urlStyle);
        regexStrings.put(Constants.CHANNEL_REGEX, channelStyle);
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
                URStyle linkStyle = entry.getValue().clone();
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
        final URStyle nickPositionStyle;
        final URStyle linePositionStyle;

        if (fromUser != null && null != myNick && myNick.equals(fromUser.toString()))
        {
            // This message is from me
            nickPositionStyle = myStyle;
            linePositionStyle = lineStyle;
        } else if (fromUser == null && fromString.equals(Constants.EVENT_USER))
        {
            // This is an event message
            nickPositionStyle = lowStyle;
            linePositionStyle = lowStyle;
        } else
        {
            // This message is from someone else
            // Does this message have my nick in it?
            if (myNick != null && line.indexOf(myNick) > -1)
                nickPositionStyle = highStyle;
            else
                nickPositionStyle = nickStyle;

            linePositionStyle = lineStyle;
        }

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
                linePositionStyle.removeAttribute("date");
            } else
            {
                linePositionStyle.addAttribute("date", lineDate);
            }

            appendString(doc, "<", linePositionStyle);
            linePositionStyle.removeAttribute("date");

            if (fromUser != null)
            {
                URStyle clickableNameStyle = nickPositionStyle;
                clickableNameStyle.addAttribute("type", "IRCUser");
                clickableNameStyle.addAttribute("clickableText",
                        new ClickableText(fromUser.toString(), nickPositionStyle, fromUser));

                // doc.insertString(doc.getLength(), fromUser.toString(), clickableNameStyle);
                appendString(doc, fromUser.toString(), clickableNameStyle);
            } else
            {
                appendString(doc, fromString, nickPositionStyle);
            }

            appendString(doc, ">", linePositionStyle);

            // print the remaining text
            // appendString(doc, " "+line, lineStyle);

            // parse the outputted line for clickable text
            parseClickableText(doc, fromUser, " " + line, linePositionStyle);

            appendString(doc, System.getProperty("line.separator"), linePositionStyle);
        } catch (BadLocationException e)
        {
            Constants.LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }

}
