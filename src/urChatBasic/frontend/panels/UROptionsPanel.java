package urChatBasic.frontend.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants.EventType;
import urChatBasic.frontend.utils.URPanels;

public class UROptionsPanel extends JPanel
{
    public String panelDisplayName;
    private JScrollPane panelScroller;
    protected EventListenerList displayListenerList = new EventListenerList();
    protected EventListenerList hideListenerList = new EventListenerList();
    protected transient ActionEvent actionEvent = null;

    public UROptionsPanel (String displayName, MainOptionsPanel optionsPanel)
    {
        this(displayName, optionsPanel, Optional.empty());
    }

    public UROptionsPanel (String displayName, MainOptionsPanel optionsPanel, Optional<Integer> preferredIndex)
    {
        panelDisplayName = displayName;
        panelScroller = new JScrollPane(this);
        optionsPanel.addToOptions(displayName, this, preferredIndex);

        URProfilesUtil.addListener(EventType.CHANGE, e -> {
            URPanels.getPreferences(this);
        });
    }

    @Override
    public String toString ()
    {
        return panelDisplayName;
    }

    public JScrollPane getScroller ()
    {
        return panelScroller;
    }

    public void addDisplayListener (ActionListener actionListener)
    {
        displayListenerList.add(ActionListener.class, actionListener);
    }

    public void addHideListener (ActionListener actionListener)
    {
        hideListenerList.add(ActionListener.class, actionListener);
    }

    public void getPreferences ()
    {
        URPanels.getPreferences(this);
    }

    public void putPreferences ()
    {
        URPanels.putPreferences(this);
    }

    /**
     * Fire the appropriate listener list. isShowing will fire the displayListenerList, else
     * hideListenerList
     *
     * @param isShowing
     */
    public void fireListeners (boolean isShowing)
    {
        Object[] listeners;
        if (isShowing)
            listeners = displayListenerList.getListenerList();
        else
            listeners = hideListenerList.getListenerList();

        // Reverse order
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == ActionListener.class)
            {
                if (this.actionEvent == null)
                {
                    this.actionEvent = new ActionEvent(this, i, TOOL_TIP_TEXT_KEY);
                }

                ((ActionListener) listeners[i + 1]).actionPerformed(this.actionEvent);
            }
        }
    }
}
