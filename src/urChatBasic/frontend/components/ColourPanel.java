package urChatBasic.frontend.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.*;
import urChatBasic.backend.utils.URPreferencesUtil;
import urChatBasic.backend.utils.URStyle;
import urChatBasic.frontend.utils.URColour;

/* ColorChooserDemo.java requires no other files. */
public class ColourPanel extends JPanel implements ChangeListener
{
    protected EventListenerList listenerList = new EventListenerList();
    protected transient ActionEvent actionEvent = null;

    public Color selectedColor;
    private URStyle targetStyle;
    private URStyle defaultStyle;
    private Preferences settingsPath;
    private JPanel bottomPanel;
    private boolean isForeground = true;

    protected final JColorChooser TCC;
    private final JButton SAVE_BUTTON;
    private final JLabel PREVIEW_LABEL;

    public ColourPanel (String styleName, URStyle defaultStyle, Preferences settingsPath)
    {
        super(new BorderLayout());
        SAVE_BUTTON = new JButton("Apply & Save");
        PREVIEW_LABEL = new JLabel("Preview Text");
        this.settingsPath = settingsPath;
        this.defaultStyle = defaultStyle.clone();
        targetStyle = defaultStyle.clone();
        loadStyle();
        bottomPanel = createBottomPanel();
        // Set up color chooser for setting text color
        TCC = new JColorChooser(targetStyle.getForeground().get());
        TCC.setPreviewPanel(bottomPanel);
        TCC.getSelectionModel().addChangeListener(this);
        // bottomPanel.setPreferredSize(new Dimension(tcc.getPreferredSize().width, 56));
        add(TCC, BorderLayout.PAGE_END);
    }

    public JPanel createBottomPanel ()
    {
        final JPanel BOTTOM_PANEL = new JPanel();
        final JButton FG_BUTTON = new JButton("Toggle Background");
        final JButton AUTO_COLOUR_BUTTON = new JButton("Suggest colour");
        final JButton RESET_BUTTON = new JButton("Reset colour");


        BOTTOM_PANEL.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        BOTTOM_PANEL.add(PREVIEW_LABEL, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        BOTTOM_PANEL.add(FG_BUTTON, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        BOTTOM_PANEL.add(AUTO_COLOUR_BUTTON, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        BOTTOM_PANEL.add(RESET_BUTTON, gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        BOTTOM_PANEL.add(SAVE_BUTTON, gbc);

        FG_BUTTON.addActionListener(e -> {
            isForeground = !isForeground;
            FG_BUTTON.setText(isForeground ? "Toggle Background" : "Toggle Foreground");
        });

        // Just sets the colours to the default colours
        RESET_BUTTON.addActionListener(e -> {
            defaultStyle.getForeground().ifPresent(fg -> setPreviewColour(fg, true));
            defaultStyle.getBackground().ifPresent(bg -> setPreviewColour(bg, false));
        });

        AUTO_COLOUR_BUTTON.addActionListener(e -> {
            if (isForeground)
            {
                TCC.setColor(URColour.getInvertedColour(PREVIEW_LABEL.getBackground()));
            } else
            {
                TCC.setColor(URColour.getInvertedColour(PREVIEW_LABEL.getForeground()));
            }
        });

        SAVE_BUTTON.addActionListener(e -> {
            // Save the style first
            if (targetStyle.equals(defaultStyle))
                URPreferencesUtil.deleteStyleColours(targetStyle, settingsPath);
            else
                URPreferencesUtil.saveStyle(defaultStyle, targetStyle, settingsPath);

            // now fire the rest of the save listeners
            fireSaveListeners();
        });

        return BOTTOM_PANEL;
    }

    private void setPreviewColour (Color newColour, boolean setForeground)
    {
        if (setForeground)
        {
            PREVIEW_LABEL.setForeground(newColour);
            targetStyle.setForeground(newColour);
            if(isForeground & TCC != null)
                TCC.setColor(newColour);
        } else
        {
            PREVIEW_LABEL.setOpaque(true);

            PREVIEW_LABEL.setBackground(newColour);
            targetStyle.setBackground(newColour);
            if(!isForeground & TCC != null)
                TCC.setColor(newColour);
        }
    }

    public ActionEvent createSaveEvent()
    {
        return new ActionEvent(SAVE_BUTTON, listenerList.getListenerList().length - 1, TOOL_TIP_TEXT_KEY);
    }

    public void addSaveListener (ActionListener actionListener)
    {
        listenerList.add(ActionListener.class, actionListener);
    }

    protected void fireSaveListeners ()
    {
        Object[] listeners = listenerList.getListenerList();

        // Reverse order
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == ActionListener.class)
            {
                if (this.actionEvent == null)
                {
                    this.actionEvent = new ActionEvent(SAVE_BUTTON, i, TOOL_TIP_TEXT_KEY);
                }

                ((ActionListener) listeners[i + 1]).actionPerformed(this.actionEvent);
            }
        }
    }

    public void loadStyle ()
    {
        targetStyle = URPreferencesUtil.loadStyle(defaultStyle, settingsPath);

        PREVIEW_LABEL.setFont(targetStyle.getFont());

        targetStyle.getForeground().ifPresent(fg -> setPreviewColour(fg, true));
        targetStyle.getBackground().ifPresent(bg -> setPreviewColour(bg, false));
    }

    public URStyle getStyle ()
    {
        return targetStyle;
    }

    public void setDefaultStyle (final URStyle newStyle)
    {
        defaultStyle = newStyle.clone();
        loadStyle();
    }

    public void setStyle (URStyle newStyle)
    {
        newStyle.getBackground().ifPresent(bg -> {
            TCC.setColor(bg);
            setPreviewColour(bg, false);
        });

        newStyle.getForeground().ifPresent(fg -> {
            TCC.setColor(fg);
            setPreviewColour(fg, true);
    });

        selectedColor = TCC.getColor();
    }

    public void stateChanged (ChangeEvent e)
    {
        selectedColor = TCC.getColor();

        setPreviewColour(selectedColor, isForeground);
    }
}
