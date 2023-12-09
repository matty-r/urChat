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

    protected JColorChooser tcc;
    public Color selectedColor;
    private URStyle targetStyle;
    private URStyle defaultStyle;
    private Preferences settingsPath;
    private JPanel bottomPanel;
    private boolean isForeground = true;
    JButton saveButton;
    JLabel previewLabel;

    public ColourPanel(String styleName, URStyle defaultStyle, Preferences settingsPath)
    {
        super(new BorderLayout());
        saveButton = new JButton("Apply & Save");
        previewLabel = new JLabel("Preview Text");
        this.settingsPath = settingsPath;
        this.defaultStyle = defaultStyle.clone();
        targetStyle = defaultStyle.clone();
        loadStyle();

        bottomPanel = createBottomPanel();
        // Set up color chooser for setting text color
        tcc = new JColorChooser(targetStyle.getForeground());
        tcc.setPreviewPanel(bottomPanel);
        tcc.getSelectionModel().addChangeListener(this);
        // bottomPanel.setPreferredSize(new Dimension(tcc.getPreferredSize().width, 56));
        add(tcc, BorderLayout.PAGE_END);
    }

    public JPanel createBottomPanel()
    {
        JPanel bottomPanel = new JPanel();
        JButton foregroundButton = new JButton("Toggle Background");
        JButton autoColour = new JButton("Suggest colour");
        JButton resetButton = new JButton("Reset colour");


        bottomPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        bottomPanel.add(previewLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bottomPanel.add(foregroundButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        bottomPanel.add(autoColour, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        bottomPanel.add(resetButton, gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        bottomPanel.add(saveButton, gbc);

        foregroundButton.addActionListener(e -> {
            isForeground = !isForeground;
            foregroundButton.setText(isForeground ? "Toggle Background" : "Toggle Foreground");
        });

        resetButton.addActionListener(e -> {
            // URPreferencesUtil.deleteStyleColours(targetStyle, settingsPath);
            // defaultStyle.load(settingsPath);
            previewLabel.setFont(defaultStyle.getFont());
            setPreviewColour(defaultStyle.getForeground(), true);
            setPreviewColour(defaultStyle.getBackground(), false);
        });

        autoColour.addActionListener(e -> {
            if (isForeground)
            {
                tcc.setColor(URColour.getInvertedColour(previewLabel.getBackground()));
            } else
            {
                tcc.setColor(URColour.getInvertedColour(previewLabel.getForeground()));
            }
        });

        saveButton.addActionListener(e -> {
            // Save the style first
            if(targetStyle.equals(defaultStyle))
                URPreferencesUtil.deleteStyleColours(targetStyle, settingsPath);
            else
                URPreferencesUtil.saveStyle(targetStyle, settingsPath);

            // now fire the rest of the save listeners
            fireSaveListeners();
        });

        return bottomPanel;
    }

    private void setPreviewColour (Color newColour, boolean setForeground)
    {
        if(setForeground)
        {
            previewLabel.setForeground(newColour);
            targetStyle.setForeground(newColour);
        } else
        {
            if(newColour != targetStyle.getBackground())
                previewLabel.setOpaque(true);
            else
                previewLabel.setOpaque(false);

            previewLabel.setBackground(newColour);
            targetStyle.setBackground(newColour);
        }
    }

    public void addSaveListener (ActionListener actionListener)
    {
        listenerList.add(ActionListener.class, actionListener);
    }

    protected void fireSaveListeners()
    {
        Object[] listeners = this.listenerList.getListenerList();

        // Reverse order
        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                if (this.actionEvent == null) {
                    this.actionEvent = new ActionEvent(saveButton, i, TOOL_TIP_TEXT_KEY);
                }

                ((ActionListener)listeners[i + 1]).actionPerformed(this.actionEvent);
            }
        }
    }

    public void loadStyle()
    {
        targetStyle = URPreferencesUtil.loadStyle(targetStyle, settingsPath);

        // defaultBackground = colourMap.get(Constants.KEY_FONT_BACKGROUND);
        // defaultForeground = colourMap.get(Constants.KEY_FONT_FOREGROUND);
        // TODO: Should also be underlined etc..
        previewLabel.setFont(targetStyle.getFont());
        setPreviewColour(targetStyle.getForeground(), true);
        setPreviewColour(targetStyle.getBackground(), false);
    }

    public URStyle getStyle()
    {
        return targetStyle;
    }

    public void stateChanged(ChangeEvent e)
    {
        selectedColor = tcc.getColor();

        setPreviewColour(selectedColor, isForeground);
    }
}
