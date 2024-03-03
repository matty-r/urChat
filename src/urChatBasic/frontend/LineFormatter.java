package urChatBasic.frontend;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
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
import urChatBasic.frontend.panels.InterfacePanel;
import urChatBasic.frontend.utils.URColour;

public class LineFormatter
{
    private String myNick;
    private Color myForeground;
    private Color myBackground;
    private IRCServerBase myServer;
    private Preferences settingsPath;
    // TODO: This should be an enum with all the styles
    private URStyle targetStyle;
    // private URStyle urlStyle;
    // private URStyle channelStyle;
    // private URStyle timeStyle;
    // private URStyle lineStyle;
    // private URStyle nickStyle;
    // private URStyle highStyle;
    // private URStyle mediumStyle;
    // private URStyle lowStyle;
    private JTextPane docOwner;
    private JScrollPane docScroller;
    public StyledDocument doc;
    // public URStyle myStyle;
    private Map<String, URStyle> formatterStyles = new HashMap<>();
    private Map<String, URStyle> updatedStyles = new HashMap<>();
    private Optional<Date> timeLine = Optional.empty();
    private AtomicLong updateStylesTime = new AtomicLong(0);
    public AtomicBoolean updateStylesInProgress = new AtomicBoolean(false);

    public LineFormatter(URStyle baseStyle, JTextPane docOwner ,JScrollPane docScroller, final IRCServerBase server, Preferences settingsPath)
    {
        // TODO: Need to load attributes from formatterPrefs
        this.settingsPath = settingsPath;

        this.docOwner = docOwner;
        this.docScroller = docScroller;

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

    public void setStyle (URStyle newStyle)
    {
        targetStyle = newStyle.clone();
        if (doc.getLength() > 0)
            updateStyles(targetStyle);
    }

    public void setFont (Font newFont)
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

        // URStyle timeStyle = defaultStyle(null, true);
        // URStyle lineStyle = defaultStyle(null, true);
        URStyle defaultStyle = defaultStyle(null, true);
        URStyle nickStyle = nickStyle(true);
        URStyle myStyle = myStyle(true);
        URStyle channelStyle = channelStyle(true);
        URStyle urlStyle = urlStyle(true);
        URStyle highStyle = highStyle(true);
        URStyle mediumStyle = mediumStyle(true);
        URStyle lowStyle = lowStyle(true);

        // updatedStyles.put(timeStyle.getName(), timeStyle);
        // updatedStyles.put(lineStyle.getName(), lineStyle);
        updatedStyles.put(defaultStyle.getName(), defaultStyle);
        updatedStyles.put(nickStyle.getName(), nickStyle);
        updatedStyles.put(myStyle.getName(), myStyle);
        updatedStyles.put(channelStyle.getName(), channelStyle);
        updatedStyles.put(urlStyle.getName(), urlStyle);
        updatedStyles.put(highStyle.getName(), highStyle);
        updatedStyles.put(mediumStyle.getName(), mediumStyle);
        updatedStyles.put(lowStyle.getName(), lowStyle);

        // TODO: Styles should be an enum

        List<URStyle> changedStyles = new ArrayList<>(updatedStyles.values());

        for (URStyle updatedStyle : updatedStyles.values()) {
            if(formatterStyles.containsKey(updatedStyle.getName()) && formatterStyles.get(updatedStyle.getName()).equals(updatedStyle))
                changedStyles.remove(updatedStyle);
            else
                formatterStyles.put(updatedStyle.getName(), updatedStyle);
        }
    }

    public int getStylesHash ()
    {
        int formatterStylesHash = formatterStyles.hashCode();
        int nickFormatHash = String.join("", DriverGUI.gui.getNickFormatString("nick")).hashCode();
        int timeStampHash = DriverGUI.gui.getTimeStampString(new Date(0L)).hashCode();


        return formatterStylesHash + nickFormatHash + timeStampHash;
    }

    class ViewPortRange {
        private int start;
        private int end;

        public ViewPortRange ()
        {
            JViewport viewport = docScroller.getViewport();
            Point startPoint = viewport.getViewPosition();
            Dimension size = viewport.getExtentSize();
            Point endPoint = new Point(startPoint.x + size.width, startPoint.y + size.height);

            start = docOwner.viewToModel2D(startPoint);
            end = docOwner.viewToModel2D(endPoint);
        }

        public int getStart ()
        {
            return start;
        }

        public int getEnd ()
        {
            return end;
        }
    }

    public URStyle dateStyle(URStyle baseStyle, Date date, boolean load)
    {
        // TODO: date style can only be lowStyle or defaultStyle
        // for now.
        if(!baseStyle.getName().equals("lowStyle"))
            baseStyle = defaultStyle(null, false);

        URStyle matchingStyle = getStyle(baseStyle.getName(), load);
        matchingStyle.addAttribute("type", "time");
        matchingStyle.addAttribute("date", date);

        return matchingStyle;
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
        // URColour.getContrastColour(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING)).getRGB())));

        StyleConstants.setFontFamily(tempStyle, targetStyle.getFont().getFamily());
        StyleConstants.setFontSize(tempStyle, targetStyle.getFont().getSize());
        StyleConstants.setBold(tempStyle, targetStyle.getFont().isBold());
        StyleConstants.setItalic(tempStyle, targetStyle.getFont().isItalic());

        StyleConstants.setForeground(tempStyle, myForeground);
        StyleConstants.setBackground(tempStyle, myBackground);

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
                    Constants.LOGGER.warn(e.getLocalizedMessage(), e);
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
                    Constants.LOGGER.warn(e.getLocalizedMessage(), e);
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

    private synchronized void setDocAttributes(int startPosition, int length, URStyle oldStyle)
    {
        // Don't do anything if there is no length
        if(length <= 0 )
            return;

        // update/add the attributes which help update the doc styles later
        oldStyle.addAttribute("styleStart", startPosition);
        oldStyle.addAttribute("styleLength", length);

        if(oldStyle.getAttribute("type") == null)
            oldStyle.addAttribute("type", "default");

        SimpleAttributeSet matchingStyle = getStyle(oldStyle.getName(), false);

        // remove the type because it will be copied back in when the style is rebuilt later
        matchingStyle.removeAttribute("type");

        // Copy the attributes, but only if they aren't already set
        Iterator<?> attributeIterator = oldStyle.getAttributeNames().asIterator();
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
                    matchingStyle.addAttribute(nextAttributeName, oldStyle.getAttribute(nextAttributeName));
            }
        }

        Constants.LOGGER.debug( "Setting character attributes at: " + startPosition + " length: " + length);
        doc.setCharacterAttributes(startPosition, length, matchingStyle, true);
    }

    // Inserts the string at the position
    private synchronized void insertString(String insertedString, URStyle style, int position)
    {
        Constants.LOGGER.debug( "Inserting a string: [" + insertedString.trim() + "] at position: " + position);
        // Append the date to the first entry on this line, and don't append it elsewhere
        if(timeLine.isPresent() && insertedString.length() > 0)
        {
            style.addAttribute("date", timeLine.get());
            timeLine = Optional.empty();
        }

        try {
            doc.insertString(position, insertedString, style);
        } catch (BadLocationException ble)
        {
            Constants.LOGGER.error(ble.getLocalizedMessage());
        }

        setDocAttributes(position, insertedString.length(), style);
    }

    /**
     * Adds the string (with all needed attributes) to the document either at the end, or the docPosition.
     * @param insertedString
     * @param style
     * @param docPosition
     * @return doc position after inserted string
     * @throws BadLocationException
     */
    private int addString(String insertedString, URStyle style, Optional<Integer> docPosition)
            throws BadLocationException
    {
        int position = doc.getLength();

        if(docPosition.isPresent())
            position = docPosition.get();

        if((myServer == null || !myServer.hasConnection()) || myServer.isConnected())
            insertString(insertedString, style, position);

        return position + insertedString.length();
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
     *
     * @param newBaseStyle
     */
    public void updateStyles (URStyle newBaseStyle)
    {
        int lastStylesHash = getStylesHash();

        updateStylesInProgress.set(true);

        initStyles(newBaseStyle);

        int currentStylesHash = getStylesHash();

        if (doc.getLength() > 0)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run ()
                {
                    try
                    {
                        if (currentStylesHash != lastStylesHash)
                        {
                            Constants.LOGGER.info( "Updating styles for " + settingsPath.name());
                            updateDocStyles(0, getStylesHash());
                        } else {
                            Constants.LOGGER.debug("NOT updating styles for " + settingsPath.name()+". Styles hash matches.");
                        }
                    } catch (BadLocationException e)
                    {
                        Constants.LOGGER.warn(e.getLocalizedMessage(), e);
                    } finally {
                        updateStylesInProgress.set(false);
                    }
                }
            });
        }
    }

    private void updateDocStyles (int currentPosition, int updateHash) throws BadLocationException
    {
        updateStylesTime.set(Instant.now().getEpochSecond());
        Element root = doc.getDefaultRootElement();
        int lineCount = root.getElementCount();
        int lineIndex = 0;

        // Get the nick format initially, then we will just replace the nickPart[1] (which is the actual nick) when needed
        String[] nickParts = DriverGUI.gui.getNickFormatString("nick");

        // looping all lines in the doc
        while (lineIndex < lineCount)
        {
            Constants.LOGGER.debug( "Updating line "+lineIndex);
            Element lineElement = root.getElement(lineIndex);

            // looping all the styles used in this line
            while (currentPosition < lineElement.getEndOffset())
            {
                Constants.LOGGER.debug( "Working at: " + currentPosition + " to: " + lineElement.getEndOffset());
                URStyle currentStyle = getStyleAtPosition(currentPosition, null);

                // Has style to update
                if (currentStyle != null && currentStyle.getAttributeCount() > 0)
                {
                    // this line has already been updated
                    if(currentStyle.getAttribute("stylesHash") != null && currentStyle.getAttribute("stylesHash").equals(updateHash))
                        break;

                    int styleLength = Integer.parseInt(currentStyle.getAttribute("styleLength").toString());
                    String styleString = doc.getText(currentPosition, styleLength);

                    if (currentStyle.getAttribute("date") != null) // Has a date, could be a timestamp
                    {
                        Date lineDate = (Date) currentStyle.getAttribute("date");
                        String newTimeStamp = DriverGUI.gui.getTimeStampString(lineDate) + " ";

                        // Is a timestamp?
                        if (currentStyle.getAttribute("type").toString().equals("time"))
                        {
                            // It's not formatted correctly, so it needs to be updated
                            if (!newTimeStamp.equals(styleString))
                            {
                                doc.remove(currentPosition, styleLength);
                                currentStyle = dateStyle(currentStyle, lineDate, false);
                                // Inserts the new timestamp, and updates the formatting
                                currentStyle.addAttribute("stylesHash", getStylesHash());
                                insertString(newTimeStamp, currentStyle, currentPosition);
                            } else {
                                currentStyle.addAttribute("stylesHash", getStylesHash());
                                setDocAttributes(currentPosition, styleLength, currentStyle);
                            }
                        } else
                        {
                            // it has a date but isn't a timestamp, so check date time is enabled and insert the timestamp string
                            if (((InterfacePanel) DriverGUI.gui.interfacePanel).isTimeStampsEnabled())
                            {
                                // this removes the date from what will become the next style on the line so that we don't insert the timestamp
                                // multiple times
                                currentStyle.removeAttribute("date");
                                setDocAttributes(currentPosition, styleLength, currentStyle);

                                currentStyle = dateStyle(currentStyle, lineDate, false);
                                // Inserts the new string, and updates the formatting
                                currentStyle.addAttribute("stylesHash", getStylesHash());
                                insertString(newTimeStamp, currentStyle, currentPosition);
                            }
                        }
                    } else if (currentStyle.getAttribute("nickParts") != null) // Has nickParts, could be the nick
                    {
                        URStyle previousStyle = getStyleAtPosition(currentPosition - 1, null);
                        URStyle nextStyle = getStyleAtPosition(currentPosition + styleLength, null);

                        switch (currentStyle.getAttribute("type").toString()) {
                            case "nickPart0":
                                    doc.remove(currentPosition, styleLength);
                                    while(nextStyle.getAttribute("type") != null && !nextStyle.getAttribute("type").equals("nick") && !nextStyle.getAttribute("type").equals("IRCUser"))
                                    {
                                        doc.remove(currentPosition, styleLength++);
                                        nextStyle = getStyleAtPosition(currentPosition + styleLength, null);
                                    }

                                    if(nextStyle.getAttribute("type") != null && (nextStyle.getAttribute("type").equals("nick") || nextStyle.getAttribute("type").equals("IRCUser")))
                                    {
                                        styleLength = nickParts[0].length();
                                        currentStyle.addAttribute("styleLength", styleLength);
                                        insertString(nickParts[0], currentStyle, currentPosition);
                                    }
                                break;
                            case "nickPart2":
                                    doc.remove(currentPosition, styleLength);
                                    while(previousStyle.getAttribute("type") != null && !previousStyle.getAttribute("type").equals("nick") && !previousStyle.getAttribute("type").equals("IRCUser"))
                                    {
                                        doc.remove(currentPosition, styleLength++);
                                        previousStyle = getStyleAtPosition(currentPosition + styleLength, null);
                                    }

                                    if(previousStyle.getAttribute("type") != null && (previousStyle.getAttribute("type").equals("nick") || previousStyle.getAttribute("type").equals("IRCUser")))
                                    {
                                        styleLength = nickParts[2].length();
                                        currentStyle.addAttribute("styleLength", styleLength);
                                        insertString(nickParts[2], currentStyle, currentPosition);
                                    }
                                break;
                            default:
                                    if(previousStyle.getAttribute("type") == null || !previousStyle.getAttribute("type").equals("nickPart0"))
                                    {
                                        URStyle updatedPreviousStyle = getStyle(previousStyle.getName(), false);
                                        updatedPreviousStyle.addAttribute("type", "nickPart0");
                                        updatedPreviousStyle.addAttribute("nickParts", nickParts);
                                        insertString(nickParts[0], updatedPreviousStyle, currentPosition);
                                        currentPosition += nickParts[0].length();
                                    }

                                    setDocAttributes(currentPosition, styleLength, currentStyle);

                                    if(nextStyle.getAttribute("type") == null || !nextStyle.getAttribute("type").equals("nickPart2"))
                                    {
                                        URStyle updatedNextStyle = getStyle(nextStyle.getName(), false);
                                        updatedNextStyle.addAttribute("type", "nickPart2");
                                        updatedNextStyle.addAttribute("nickParts", nickParts);
                                        insertString(nickParts[2], updatedNextStyle, currentPosition+styleLength);
                                    }
                                break;
                        }
                    } else {
                        setDocAttributes(currentPosition, styleLength, currentStyle);
                    }

                    // set position for the next style in the line
                    currentPosition += Integer.parseInt(currentStyle.getAttribute("styleLength").toString());
                } else
                {
                    currentPosition++;
                }
            }

            // move to the next position (should be the start of the next line)
            lineIndex++;
        }

        Constants.LOGGER.info( "Took " + Duration.between(Instant.ofEpochSecond(updateStylesTime.get()), Instant.now()).toMillis() +  "ms to update styles.");
        updateStylesTime.set(0);
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

    public void removeFirstLine ()
    {
        Element firstLine = doc.getDefaultRootElement().getElement(0);
        int endIndex = firstLine.getEndOffset();
        try {
            doc.remove(0, endIndex);
        } catch (BadLocationException ble)
        {
            // TODO:
            ble.printStackTrace();
        }
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

    public String getLine(int lineNumber) throws BadLocationException
    {
        Element root = doc.getDefaultRootElement();
        int lines = root.getElementCount();

        String finalLine = "";

        while (finalLine.isEmpty())
        {

            if (lines < 0)
                break;

            Element line = root.getElement(lineNumber);

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

    public int getLineCount ()
    {
        Element root = doc.getDefaultRootElement();
        return root.getElementCount();
    }

    private int getLinePosition(String targetLine) throws BadLocationException {
        Element root = doc.getDefaultRootElement();
        int lines = root.getElementCount();

        // Create a regex pattern to match the target line
        Pattern pattern = Pattern.compile(".*"+Pattern.quote(targetLine.trim())+"$");

        for (int i = 0; i < lines; i++) {
            Element line = root.getElement(i);

            if (line == null)
                continue;

            int start = line.getStartOffset();
            int end = line.getEndOffset();
            String text = doc.getText(start, end - start);

            Matcher matcher = pattern.matcher(text.trim());

            // If the pattern matches, return the start offset of the line
            if (matcher.find()) {
                return start;
            }
        }

        return -1; // Return -1 if the target line is not found
    }

    public URStyle getStyleAtPosition(int position, String relativeLine)
    {
        try{
        if (relativeLine != null && !relativeLine.isBlank())
            position = position + getLinePosition(relativeLine);

        } catch (BadLocationException ble)
        {
            // TODO
            Constants.LOGGER.error(ble.getLocalizedMessage());
        }
        AttributeSet textStyle = doc.getCharacterElement(position).getAttributes();
        return new URStyle(new SimpleAttributeSet(textStyle));
    }

    private int parseClickableText(IRCUser fromUser, String line, URStyle defaultStyle, int position)
            throws BadLocationException
    {
        HashMap<String, URStyle> regexStrings = new HashMap<>();
        regexStrings.put(Constants.URL_REGEX, getStyle("urlStyle", false));
        regexStrings.put(Constants.CHANNEL_REGEX, getStyle("channelStyle", false));
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
            position = addString(remainingLine.substring(0, nextLineStart - offset), defaultStyle, Optional.of(position));

            position = addString(nextLine.getAttribute("clickableText").toString(), nextLine, Optional.of(position));

            remainingLine = remainingLine.substring((nextLineStart + nextLineLength) - offset);
        }

        position = addString(remainingLine, defaultStyle, Optional.of(position));

        return position;
    }

    /**
     *
     * @param lineDate
     * @param fromUser
     * @param fromString
     * @param line
     */
    public void appendMessage(Optional<Date> lineDate, IRCUser fromUser, String fromString, String line)
    {
        int insertPosition = doc.getLength();

        if(lineDate.isEmpty())
            lineDate = Optional.of(new Date());
        else
            insertPosition = 0;

        // build the timeLine string
        timeLine = lineDate;
        String[] nickParts;

        final URStyle nickPositionStyle;
        final URStyle linePositionStyle;
        final URStyle timePositionStyle;

        if (fromUser != null && null != myNick && myNick.equals(fromUser.toString()))
        {
            // This message is from me
            nickPositionStyle = getStyle("myStyle", false);
            linePositionStyle = getStyle("defaultStyle", false);
            timePositionStyle =  getStyle("defaultStyle", false);
            nickParts = DriverGUI.gui.getNickFormatString(fromUser.getName());
        } else if (fromUser == null && fromString.equals(Constants.EVENT_USER))
        {
            // This is an event message
            nickPositionStyle = getStyle("lowStyle", false);
            linePositionStyle = getStyle("lowStyle", false);
            timePositionStyle = getStyle("lowStyle", false);
            nickParts = DriverGUI.gui.getNickFormatString(fromString);
        } else
        {
            nickParts = DriverGUI.gui.getNickFormatString(fromUser.getName());

            // This message is from someone else
            // Does this message have my nick in it?
            if (myNick != null && line.indexOf(myNick) > -1)
                nickPositionStyle = getStyle("highStyle", false);
            else
                nickPositionStyle = getStyle("nickStyle", false);

            linePositionStyle = getStyle("defaultStyle", false);
            timePositionStyle = getStyle("defaultStyle", false);
        }

        try
        {

            // doc.insertString(doc.getLength(), timeLine, defaultStyle);
            // if(null != timeLine && !timeLine.isBlank())
            if (((InterfacePanel) DriverGUI.gui.interfacePanel).isTimeStampsEnabled())
            {
                // add the date to the end of the string to preserve the timestamp of the line
                // when updating styles
                URStyle lineDateStyle = dateStyle(timePositionStyle, lineDate.get(), false);
                lineDateStyle.addAttribute("stylesHash", getStylesHash());
                insertPosition = addString(DriverGUI.gui.getTimeStampString(timeLine.get()) + " ", lineDateStyle, Optional.of(insertPosition));
            }

            linePositionStyle.addAttribute("type", "nickPart0");
            linePositionStyle.addAttribute("nickParts", nickParts);
            insertPosition = addString(nickParts[0], linePositionStyle, Optional.of(insertPosition));

            if (fromUser != null)
            {
                URStyle clickableNameStyle = nickPositionStyle;
                clickableNameStyle.addAttribute("type", "IRCUser");
                clickableNameStyle.addAttribute("clickableText",
                        new ClickableText(fromUser.toString(), nickPositionStyle, fromUser));

                clickableNameStyle.addAttribute("nickParts", nickParts);
                insertPosition = addString(nickParts[1], clickableNameStyle, Optional.of(insertPosition));
            } else
            {
                nickPositionStyle.addAttribute("type", "nick");
                nickPositionStyle.addAttribute("nickParts", nickParts);
                insertPosition = addString(nickParts[1], nickPositionStyle, Optional.of(insertPosition));
            }

            linePositionStyle.addAttribute("type", "nickPart2");
            insertPosition = addString(nickParts[2], linePositionStyle, Optional.of(insertPosition));

            linePositionStyle.removeAttribute("type");
            linePositionStyle.removeAttribute("nickParts");

            // print the remaining text
            // appendString(doc, " "+line, lineStyle);

            // parse the outputted line for clickable text
            insertPosition = parseClickableText(fromUser, " " + line, linePositionStyle, insertPosition);
            insertPosition = addString(System.getProperty("line.separator"), linePositionStyle, Optional.of(insertPosition));
        } catch (BadLocationException e)
        {
            Constants.LOGGER.error( e.getLocalizedMessage());
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
