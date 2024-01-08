package urChatBasic.frontend.panels;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.base.Constants.EventType;
import urChatBasic.base.Constants.Placement;
import urChatBasic.frontend.utils.URPanels;

public class InterfacePanel extends UROptionsPanel {
    public static final String PANEL_DISPLAY_NAME = "Interface";

    public JCheckBox showEventTicker = new JCheckBox("Show Event Ticker");
    public JCheckBox showUsersList = new JCheckBox("Show Users List");
    public JCheckBox enableClickableLinks = new JCheckBox("Make links clickable");
    public JCheckBox showJoinsQuitsEventTicker = new JCheckBox("Show Joins/Quits in the Event Ticker");
    public JCheckBox showJoinsQuitsMainWindow = new JCheckBox("Show Joins/Quits in the Chat Window");
    public JCheckBox logChannelText = new JCheckBox("Save and log all channel text");
    public JCheckBox logServerActivity = new JCheckBox("Save and log all Server activity");
    public JCheckBox logClientText = new JCheckBox("Log client text (Allows up or down history)");
    public JCheckBox limitServerLines = new JCheckBox("Limit the number of lines in Server activity");
    private final JCheckBox limitChannelLines = new JCheckBox("Limit the number of lines in channel text");
    public JCheckBox enableTimeStamps = new JCheckBox("Time Stamp chat messages");

    public JTextField limitServerLinesCount = new JTextField();
    public JTextField limitChannelLinesCount = new JTextField();

    public final int TICKER_DELAY_MIN = 0;
    public final int TICKER_DELAY_MAX = 30;
    public final int TICKER_DELAY_INIT = 20;
    public final int DEFAULT_LINES_LIMIT = Integer.parseInt(Constants.DEFAULT_LIMIT_CHANNEL_LINES_COUNT);
    public JSlider eventTickerDelay =
            new JSlider(JSlider.HORIZONTAL, TICKER_DELAY_MIN, TICKER_DELAY_MAX, TICKER_DELAY_INIT);

    public InterfacePanel (MainOptionsPanel optionsPanel)
    {
        super(PANEL_DISPLAY_NAME, optionsPanel);
        setupInterfacePanel();
    }

    public InterfacePanel (MainOptionsPanel optionsPanel, Optional<Integer> preferredIndex)
    {
        super(PANEL_DISPLAY_NAME, optionsPanel, preferredIndex);
        setupInterfacePanel();
    }

    /**
     * Adds all the components to the panel, with the related preference Keys for that component.
     * i.e showEventTicker is set via the KEY_EVENT_TICKER_ACTIVE key ('show event ticker')
     */
    private void setupInterfacePanel ()
    {
        URPanels.addToPanel(this, showEventTicker, null, Placement.DEFAULT, null, Constants.KEY_EVENT_TICKER_ACTIVE);
        URPanels.addToPanel(this, enableClickableLinks, null, Placement.DEFAULT, null, Constants.KEY_CLICKABLE_LINKS_ENABLED);
        URPanels.addToPanel(this, showJoinsQuitsEventTicker, null, Placement.DEFAULT, null, Constants.KEY_EVENT_TICKER_JOINS_QUITS);
        URPanels.addToPanel(this, showJoinsQuitsMainWindow, null, Placement.DEFAULT, null, Constants.KEY_MAIN_WINDOW_JOINS_QUITS);
        URPanels.addToPanel(this, logChannelText, null, Placement.DEFAULT, null, Constants.KEY_LOG_CHANNEL_ACTIVITY);
        URPanels.addToPanel(this, logServerActivity, null, Placement.DEFAULT, null, Constants.KEY_LOG_SERVER_ACTIVITY);
        URPanels.addToPanel(this, logClientText, null, Placement.DEFAULT, null, Constants.KEY_LOG_CLIENT_TEXT);
        URPanels.addToPanel(this, limitServerLines, null, Placement.DEFAULT, null, Constants.KEY_LIMIT_SERVER_LINES);
        URPanels.addToPanel(this, limitServerLinesCount, null, Placement.RIGHT, null, Constants.KEY_LIMIT_SERVER_LINES_COUNT);
        URPanels.addToPanel(this, limitChannelLines, null, Placement.DEFAULT, null, Constants.KEY_LIMIT_CHANNEL_LINES);
        URPanels.addToPanel(this, limitChannelLinesCount, null, Placement.RIGHT, null, Constants.KEY_LIMIT_CHANNEL_LINES_COUNT);
        URPanels.addToPanel(this, enableTimeStamps, null, Placement.DEFAULT, null, Constants.KEY_TIME_STAMPS);

        // Turn on labels at major tick mark.
        eventTickerDelay.setMajorTickSpacing(10);
        eventTickerDelay.setMinorTickSpacing(1);
        eventTickerDelay.setPaintTicks(true);

        eventTickerDelay.setPaintLabels(true);
        // eventTickerDelay.setMaximumSize(new Dimension(400, 40));

        eventTickerDelay.setToolTipText("Event Ticker movement delay (Lower is faster)");

        // interfacePanel.add(eventTickerLabel);
        URPanels.addToPanel(this, eventTickerDelay, "Event Ticker Delay", Placement.DEFAULT, null, Constants.KEY_EVENT_TICKER_DELAY);

        URProfilesUtil.addListener(EventType.CHANGE, e -> {
            URPanels.getPreferences(this);
        });
    }

    public int getLimitServerLinesCount ()
    {
        try
        {
            return Integer.parseInt(limitServerLinesCount.getText());
        } catch (Exception e)
        {
            // Was an error, default to 1000
            return DEFAULT_LINES_LIMIT;
        }
    }

    public int getEventTickerDelay ()
    {
        return eventTickerDelay.getValue();
    }

    public void enableLimitChannelLines (boolean enable)
    {
        limitChannelLines.setSelected(enable);
    }

    public boolean getLimitChannelLines ()
    {
        return limitChannelLines.isSelected();
    }

    public void setLimitChannelLinesCount (int limit)
    {
        limitChannelLinesCount.setText(Integer.toString(limit));
    }

    public void setLimitServerLinesCount (int limit)
    {
        limitServerLinesCount.setText(Integer.toString(limit));
    }

    public int getLimitChannelLinesCount ()
    {
        try
        {
            return Integer.parseInt(limitChannelLinesCount.getText());
        } catch (Exception e)
        {
            // Was an error, set to default
            return DEFAULT_LINES_LIMIT;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#saveChannelHistory()
     */
    public Boolean saveChannelHistory ()
    {
        return logChannelText.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#saveServerHistory()
     */
    public Boolean saveServerHistory ()
    {
        return logServerActivity.isSelected();
    }

        /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isShowingEventTicker()
     */
    public Boolean isShowingEventTicker ()
    {
        return showEventTicker.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isShowingUsersList()
     */
    public Boolean isShowingUsersList ()
    {
        return showUsersList.isSelected();
    }

    public Boolean isClickableLinksEnabled ()
    {
        return enableClickableLinks.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isJoinsQuitsTickerEnabled()
     */
    public Boolean isJoinsQuitsTickerEnabled ()
    {
        return showJoinsQuitsEventTicker.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isJoinsQuitsMainEnabled()
     */
    public Boolean isJoinsQuitsMainEnabled ()
    {
        return showJoinsQuitsMainWindow.isSelected();
    }

    public void setJoinsQuitsMain (boolean enable)
    {
        showJoinsQuitsMainWindow.setSelected(enable);
    }

    public void setJoinsQuitsEventTicker (boolean enable)
    {
        showJoinsQuitsEventTicker.setSelected(enable);
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isChannelHistoryEnabled()
     */
    public Boolean isChannelHistoryEnabled ()
    {
        return logChannelText.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isLimitedServerActivity()
     */
    public Boolean isLimitedServerActivity ()
    {
        return limitServerLines.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isLimitedChannelActivity()
     */
    public Boolean isLimitedChannelActivity ()
    {
        return limitChannelLines.isSelected();
    }

    /*
     * (non-Javadoc)
     *
     * @see urChatBasic.frontend.UserGUIBase#isTimeStampsEnabled()
     */
    public Boolean isTimeStampsEnabled ()
    {
        return enableTimeStamps.isSelected();
    }

    public Boolean isClientHistoryEnabled ()
    {
        return logClientText.isSelected();

    }
}
