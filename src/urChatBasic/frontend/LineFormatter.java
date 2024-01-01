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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.Option;
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
    private Preferences settingsPath;
    private URStyle urlStyle;
    private URStyle channelStyle;
    private URStyle timeStyle;
    private URStyle lineStyle;
    private URStyle nickStyle;
    private URStyle highStyle;
    private URStyle mediumStyle;
    private URStyle lowStyle;
    private JTextPane docOwner;
    public StyledDocument doc;
    public URStyle myStyle;
    private Map<String, URStyle> formatterStyles = new HashMap<>();
    private Optional<Date> timeLine = Optional.empty();


    public LineFormatter(URStyle baseStyle, JTextPane docOwner ,final IRCServerBase server, Preferences settingsPath)
    {
        // TODO: Need to load attributes from formatterPrefs
        this.settingsPath = settingsPath;

        this.docOwner = docOwner;

        // this.docOwner.setBackground(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING));
        doc = this.docOwner.getStyledDocument();

        if (null != server)
        {
            myNick = server.getNick();
            myServer = server;
        } else
        {
            myNick = null;
        }

        initStyles(baseStyle);
    }

    public void setFont(Font newFont)
    {
        targetStyle.setFont(newFont);
        if (doc.getLength() > 0)
            updateStyles(targetStyle);
    }

    public void initStyles (URStyle baseStyle)
    {
        // The JTextPane is technically 'disabled', so we need to change the colour to be the default enabled colour.
        docOwner.setBackground(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING));

        targetStyle = baseStyle.clone();

        targetStyle.getForeground().ifPresent(fg -> myForeground = fg);
        targetStyle.getBackground().ifPresent(bg -> myBackground = bg);

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

    public URStyle defaultStyle(String name, boolean load)
    {
        if (name == null)
            name = "defaultStyle";

        URStyle tempStyle = new URStyle(name, targetStyle.getFont());
        // tempStyle.addAttribute("type", "default");
        // get the contrasting colour of the background colour
        // StyleConstants.setForeground(defaultStyle, new Color(formatterPrefs.node(name).getInt("font
        // foreground",
        // URColour.getContrastColour(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING)).getRGB())));

        StyleConstants.setFontFamily(tempStyle, targetStyle.getFont().getFamily());
        StyleConstants.setFontSize(tempStyle, targetStyle.getFont().getSize());
        StyleConstants.setBold(tempStyle, targetStyle.getFont().isBold());
        StyleConstants.setItalic(tempStyle, targetStyle.getFont().isItalic());

        StyleConstants.setForeground(tempStyle, myForeground);

        if (load)
            tempStyle.load(settingsPath);

        return tempStyle;
    }

    public URStyle lowStyle(boolean load)
    {
        String name = "lowStyle";

        URStyle tempStyle = defaultStyle(name, load);


        StyleConstants.setForeground(tempStyle, UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING).darker());

        if (StyleConstants.getForeground(tempStyle).getRGB() == myForeground.getRGB())
            if (URColour.useDarkColour(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING)))
            {
                StyleConstants.setForeground(tempStyle, UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING).darker());
            } else
            {
                StyleConstants.setForeground(tempStyle, UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING).brighter());
            }

        if (load)
            tempStyle.load(settingsPath);


        return tempStyle;
    }

    public URStyle mediumStyle(boolean load)
    {
        String name = "mediumStyle";

        URStyle tempStyle = defaultStyle(name, load);

        if (load)
            tempStyle.load(settingsPath);

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
            tempStyle.load(settingsPath);

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
            tempStyle.load(settingsPath);

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
            tempStyle.load(settingsPath);

        return tempStyle;
    }

    public URStyle myStyle(boolean load)
    {
        String name = "myStyle";

        URStyle tempStyle = defaultStyle(name, load);
        tempStyle.addAttribute("type", "IRCUser");

        // StyleConstants.setForeground(tempStyle, Color.GREEN);
        StyleConstants.setForeground(tempStyle,
                URColour.getInvertedColour(UIManager.getColor("CheckBoxMenuItem.selectionBackground")));

        StyleConstants.setBold(tempStyle, true);
        StyleConstants.setUnderline(tempStyle, true);

        if (load)
            tempStyle.load(settingsPath);

        return tempStyle;
    }

    public URStyle nickStyle(boolean load)
    {
        String name = "nickStyle";

        URStyle tempStyle = defaultStyle(name, load);
        tempStyle.addAttribute("type", "IRCUser");

        StyleConstants.setUnderline(tempStyle, true);

        if (load)
            tempStyle.load(settingsPath);

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
    private void insertString(String insertedString, URStyle style, int position, boolean setCharacterAttributes)
            throws BadLocationException
    {
        style = style.clone();

        // remove the existing attributes
        style.removeAttribute("styleStart");
        style.removeAttribute("styleLength");

        // add an attribute so we know when the style is expected to start and end.
        style.addAttribute("styleStart", position);
        style.addAttribute("styleLength", insertedString.length());

        // Append the date to the first entry on this line, and don't append it elsewhere
        if(timeLine.isPresent() && insertedString.length() > 0)
        {
            style.addAttribute("date", timeLine.get());
            timeLine = Optional.empty();
        }

        doc.insertString(position, insertedString, style);

        String styleName = style.getAttribute("name").toString();
        SimpleAttributeSet matchingStyle = getStyle(styleName, false);
        matchingStyle.removeAttribute("type");
        if(setCharacterAttributes)
        {
            Iterator<?> attributeIterator = style.getAttributeNames().asIterator();
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
                        matchingStyle.addAttribute(nextAttributeName, style.getAttribute(nextAttributeName));
                }
            }

            doc.setCharacterAttributes(position, insertedString.length(), matchingStyle, true);
        }
    }

    // Adds the string (with all needed attributes) to the end of the document
    private void appendString(String insertedString, URStyle style)
            throws BadLocationException
    {
        int position = doc.getLength();

        if((myServer == null || !myServer.hasConnection()) || myServer.isConnected())
            insertString(insertedString, style, position, false);
    }

    public URStyle getStyleDefault(String styleName)
    {
       switch (styleName)
        {
            case "mediumStyle":
                return mediumStyle(false);
            case "highStyle":
                return highStyle(false);
            case "nickStyle":
                return nickStyle(false);
            case "myStyle":
                return myStyle(false);
            case "lowStyle":
                return lowStyle(false);
            case "urlStyle":
                return urlStyle(false);
            case "channelStyle":
                return channelStyle(false);
            default:
                return defaultStyle(null, true);
        }
    }

    public URStyle getStyle(String styleName, boolean load)
    {
        // TODO: Might need to readjust this again?
        URStyle currentStyle = formatterStyles.get(styleName).clone();
        if(load)
        {
            currentStyle.load(settingsPath);
        }

        return currentStyle;

    }

    /**
     * Reloads all the styles, then updates the doc
     * @param newBaseStyle
     */
    public void updateStyles(URStyle newBaseStyle)
    {
        initStyles(newBaseStyle);

        Constants.LOGGER.log(Level.FINE, "Updating styles.");
        if(doc.getLength() > 0)
            updateDocStyles(0);
    }

    private void updateDocStyles(int startPosition)
    {
        URStyle textStyle = getStyleAtPosition(startPosition, null);
        String styleName = textStyle.getAttribute("name").toString();
        int styleStart = startPosition;
        int styleLength = Integer.parseInt(textStyle.getAttribute("styleLength").toString());

        // String styleText = doc.getText(startPosition, styleLength);

        URStyle matchingStyle = getStyle(styleName, false);
        matchingStyle.removeAttribute("type");
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

                    if(textStyle.getAttribute("name").equals("lowStyle"))
                        timeStyle = getStyle(textStyle.getAttribute("name").toString(), false);
                    else
                        timeStyle = defaultStyle(null, false);

                    timeStyle.addAttribute("date", lineDate);
                    timeStyle.addAttribute("type", "time");
                    insertString(newTimeString, timeStyle, styleStart, true);
                    styleLength = newTimeString.length();
                } else
                {
                    if (hasTime)
                    {
                        textStyle = getStyleAtPosition(startPosition, null);

                        styleName = textStyle.getAttribute("name").toString();
                        styleStart = startPosition;
                        styleLength = Integer.parseInt(textStyle.getAttribute("styleLength").toString());

                        matchingStyle = getStyle(styleName, false);
                        matchingStyle.addAttribute("date", lineDate);

                        isDateStyle = false;
                    }
                }
            } catch (BadLocationException ble)
            {
                Constants.LOGGER.log(Level.WARNING, ble.getLocalizedMessage());
            }
        } else if (DriverGUI.gui != null && textStyle.getAttribute("type") != null && textStyle.getAttribute("nickParts") != null)
        {
            String[] styleNickParts = (String[]) textStyle.getAttribute("nickParts");
            String[] nickParts = UserGUI.getNickFormatString(styleNickParts[1]);

            try
            {
                URStyle previousStyle = getStyleAtPosition(styleStart - 1, null);
                URStyle nextStyle = getStyleAtPosition(styleStart + styleLength, null);

                switch (textStyle.getAttribute("type").toString()) {
                    case "nickPart0":
                            doc.remove(styleStart, styleLength);
                            while(nextStyle.getAttribute("type") != null && !nextStyle.getAttribute("type").equals("nick") && !nextStyle.getAttribute("type").equals("IRCUser"))
                            {
                                doc.remove(styleStart, styleLength++);
                                nextStyle = getStyleAtPosition(styleStart + styleLength, null);
                            }

                            if(nextStyle.getAttribute("type") != null && (nextStyle.getAttribute("type").equals("nick") || nextStyle.getAttribute("type").equals("IRCUser")))
                            {
                                styleLength = nickParts[0].length();
                                textStyle.addAttribute("styleLength", styleLength);
                                insertString(nickParts[0], textStyle, styleStart, false);
                            }
                        break;
                    case "nickPart2":
                            doc.remove(styleStart, styleLength);
                            while(previousStyle.getAttribute("type") != null && !previousStyle.getAttribute("type").equals("nick") && !previousStyle.getAttribute("type").equals("IRCUser"))
                            {
                                doc.remove(styleStart, styleLength++);
                                previousStyle = getStyleAtPosition(styleStart + styleLength, null);
                            }

                            if(previousStyle.getAttribute("type") != null && (previousStyle.getAttribute("type").equals("nick") || previousStyle.getAttribute("type").equals("IRCUser")))
                            {
                                styleLength = nickParts[2].length();
                                textStyle.addAttribute("styleLength", styleLength);
                                insertString(nickParts[2], textStyle, styleStart, false);
                            }
                        break;
                    default:
                            if(previousStyle.getAttribute("type") == null || !previousStyle.getAttribute("type").equals("nickPart0"))
                            {
                                URStyle updatedPreviousStyle = getStyle(previousStyle.getName(), false);
                                updatedPreviousStyle.addAttribute("type", "nickPart0");
                                updatedPreviousStyle.addAttribute("nickParts", nickParts);
                                insertString(nickParts[0], updatedPreviousStyle, styleStart, true);
                                styleStart += nickParts[0].length();
                            }

                            if(nextStyle.getAttribute("type") == null || !nextStyle.getAttribute("type").equals("nickPart2"))
                            {
                                URStyle updatedNextStyle = getStyle(nextStyle.getName(), false);
                                updatedNextStyle.addAttribute("type", "nickPart2");
                                updatedNextStyle.addAttribute("nickParts", nickParts);
                                insertString(nickParts[2], updatedNextStyle, styleStart+styleLength, true);
                            }
                        break;
                }
            } catch (BadLocationException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
            updateDocStyles((styleStart + styleLength));
    }

    public String getFirstLine() throws BadLocationException
    {
        Element root = doc.getDefaultRootElement();
        int linePos = 0;

        String finalLine = "";

        while (finalLine.isEmpty())
        {

            if (linePos < 0)
                break;

            Element line = root.getElement(linePos++);

            if (null == line)
                continue;

            int start = line.getStartOffset();
            int end = line.getEndOffset();
            String text = doc.getText(start, end - start);
            finalLine = text.trim();
        }

        return finalLine;
    }

    public String getLatestLine() throws BadLocationException
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

     // New method to get line at position
     public String getLineAtPosition(int position) throws BadLocationException {
        Element root = doc.getDefaultRootElement();
        int lineCount = root.getElementCount();

        // Find the line that contains the given position
        int lineIndex = root.getElementIndex(position);
        Element lineElement = root.getElement(lineIndex);

        // Get start and end offsets of the line
        int startOffset = lineElement.getStartOffset();
        int endOffset = lineElement.getEndOffset();

        // Extract the text of the line
        String lineText = doc.getText(startOffset, endOffset - startOffset);

        // Return the line text
        return lineText;
    }

    private int getLinePosition(String targetLine) throws BadLocationException
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

    public URStyle getStyleAtPosition(int position, String relativeLine)
    {
        try{
        if (relativeLine != null && !relativeLine.isBlank())
            position = position + getLinePosition(relativeLine);

        } catch (BadLocationException ble)
        {
            // TODO
            Constants.LOGGER.log(Level.WARNING, ble.getLocalizedMessage());
        }
        AttributeSet textStyle = doc.getCharacterElement(position).getAttributes();

        return new URStyle(new SimpleAttributeSet(textStyle));
    }

    private void parseClickableText(IRCUser fromUser, String line, URStyle defaultStyle)
            throws BadLocationException
    {
        HashMap<String, URStyle> regexStrings = new HashMap<>();
        regexStrings.put(Constants.URL_REGEX, urlStyle);
        regexStrings.put(Constants.CHANNEL_REGEX, channelStyle);
        // final String line = getLatestLine(doc);
        final int relativePosition = getLinePosition(getLatestLine());

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
            appendString(remainingLine.substring(0, nextLineStart - offset), defaultStyle);

            appendString(nextLine.getAttribute("clickableText").toString(), nextLine);

            remainingLine = remainingLine.substring((nextLineStart + nextLineLength) - offset);
        }

        appendString(remainingLine, defaultStyle);
    }

    /**
     *
     * @param lineDate
     * @param fromUser
     * @param fromString
     * @param line
     */
    public void formattedDocument(Date lineDate, IRCUser fromUser, String fromString, String line)
    {
        // build the timeLine string
        timeLine = Optional.of(lineDate);
        String[] nickParts;

        final URStyle nickPositionStyle;
        final URStyle linePositionStyle;
        final URStyle timePositionStyle;

        if (fromUser != null && null != myNick && myNick.equals(fromUser.toString()))
        {
            // This message is from me
            nickPositionStyle = myStyle.clone();
            linePositionStyle = lineStyle.clone();
            timePositionStyle = timeStyle.clone();
            nickParts = UserGUI.getNickFormatString(fromUser.getName());
        } else if (fromUser == null && fromString.equals(Constants.EVENT_USER))
        {
            // This is an event message
            nickPositionStyle = lowStyle.clone();
            linePositionStyle = lowStyle.clone();
            timePositionStyle = lowStyle.clone();
            nickParts = UserGUI.getNickFormatString(fromString);
        } else
        {
            nickParts = UserGUI.getNickFormatString(fromUser.getName());

            // This message is from someone else
            // Does this message have my nick in it?
            if (myNick != null && line.indexOf(myNick) > -1)
                nickPositionStyle = highStyle.clone();
            else
                nickPositionStyle = nickStyle.clone();

            linePositionStyle = lineStyle.clone();
            timePositionStyle = timeStyle.clone();
        }

        try
        {

            // doc.insertString(doc.getLength(), timeLine, timeStyle);
            // if(null != timeLine && !timeLine.isBlank())
            if (DriverGUI.gui.isTimeStampsEnabled())
            {
                // add the date to the end of the string to preserve the timestamp of the line
                // when updating styles
                // timePositionStyle.addAttribute("date", lineDate);
                timePositionStyle.removeAttribute("type");
                timePositionStyle.addAttribute("type", "time");
                appendString(UserGUI.getTimeLineString(timeLine.get()) + " ", timePositionStyle);
                timePositionStyle.removeAttribute("type");
            }

            linePositionStyle.addAttribute("type", "nickPart0");
            linePositionStyle.addAttribute("nickParts", nickParts);
            appendString(nickParts[0], linePositionStyle);

            if (fromUser != null)
            {
                URStyle clickableNameStyle = nickPositionStyle.clone();
                clickableNameStyle.addAttribute("type", "IRCUser");
                clickableNameStyle.addAttribute("clickableText",
                        new ClickableText(fromUser.toString(), nickPositionStyle, fromUser));

                clickableNameStyle.addAttribute("nickParts", nickParts);
                appendString(nickParts[1], clickableNameStyle);
            } else
            {
                nickPositionStyle.addAttribute("type", "nick");
                nickPositionStyle.addAttribute("nickParts", nickParts);
                appendString(nickParts[1], nickPositionStyle);
            }

            linePositionStyle.addAttribute("type", "nickPart2");
            appendString(nickParts[2], linePositionStyle);

            linePositionStyle.removeAttribute("type");
            linePositionStyle.removeAttribute("nickParts");

            // print the remaining text
            // appendString(doc, " "+line, lineStyle);

            // parse the outputted line for clickable text
            parseClickableText(fromUser, " " + line, linePositionStyle);
            appendString(System.getProperty("line.separator"), linePositionStyle);
        } catch (BadLocationException e)
        {
            Constants.LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }

    public StyledDocument getDocument()
    {
        return doc;
    }

    public void setSettingsPath(Preferences profilePath)
    {
        settingsPath = profilePath;
        updateStyles(targetStyle);
    }

}
