package urChatBasic.frontend.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.*;
import urChatBasic.backend.utils.URSettingsLoader;
import urChatBasic.base.Constants;
import urChatBasic.frontend.utils.URColour;

/* ColorChooserDemo.java requires no other files. */
public class ColourPanel extends JPanel implements ChangeListener
{
    protected EventListenerList listenerList = new EventListenerList();
    protected transient ActionEvent actionEvent = null;

    protected JColorChooser tcc;
    public Color selectedColor;
    private Color defaultForeground;
    private Color defaultBackground;
    private Font displayFont;
    private Preferences settingsPath;
    private JPanel bottomPanel;
    private boolean isForeground = true;
    JButton saveButton;
    JLabel previewLabel;

    public ColourPanel(Font displayFont, Preferences settingsPath)
    {
        super(new BorderLayout());
        this.settingsPath = settingsPath;
        this.displayFont = displayFont;
        Map<String,Color> loadedColours = URSettingsLoader.loadFontColours(getForeground(), getBackground(), settingsPath);
        defaultForeground = loadedColours.get(Constants.KEY_FONT_FOREGROUND);
        defaultBackground = loadedColours.get(Constants.KEY_FONT_BACKGROUND);

        bottomPanel = createBottomPanel();
        // Set up color chooser for setting text color
        tcc = new JColorChooser(defaultForeground);
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
        saveButton = new JButton("Apply & Save");
        previewLabel = new JLabel("Preview Text");
        previewLabel.setFont(displayFont);

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
            if (isForeground)
            {
                previewLabel.setForeground(defaultForeground);
            } else
            {
                previewLabel.setOpaque(true);
                previewLabel.setBackground(defaultBackground);
            }
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
            fireSaveListeners();
        });

        return bottomPanel;
    }

    public void addSaveListener (ActionListener actionListener)
    {
        // saveButton.addActionListener(actionListener);
        listenerList.add(ActionListener.class, actionListener);
    }

    protected void fireSaveListeners()
    {
        Object[] listeners = this.listenerList.getListenerList();

        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                if (this.actionEvent == null) {
                    this.actionEvent = new ActionEvent(saveButton, i, TOOL_TIP_TEXT_KEY);
                }

                ((ActionListener)listeners[i + 1]).actionPerformed(this.actionEvent);
            }
        }
    }

    public void loadColours()
    {
        Map<String, Color> colourMap =
                URSettingsLoader.loadFontColours(defaultForeground, defaultBackground, settingsPath);

        defaultBackground = colourMap.get(Constants.KEY_FONT_BACKGROUND);
        defaultForeground = colourMap.get(Constants.KEY_FONT_FOREGROUND);
        // int savedFontBoldItalic = 0;

        // if (settingsPath.getBoolean(Constants.KEY_FONT_BOLD, defaultFont.isBold()))
        // savedFontBoldItalic = Font.BOLD;
        // if (settingsPath.getBoolean(Constants.KEY_FONT_ITALIC, defaultFont.isItalic()))
        // savedFontBoldItalic |= Font.ITALIC;

        // savedFont = new Font(settingsPath.get(Constants.KEY_FONT_FAMILY, defaultFont.getFamily()),
        // savedFontBoldItalic, settingsPath.getInt(Constants.KEY_FONT_SIZE, defaultFont.getSize()));

        // setFont(savedFont, false);
    }

    public void stateChanged(ChangeEvent e)
    {
        selectedColor = tcc.getColor();

        if (isForeground)
        {
            previewLabel.setForeground(selectedColor);
        } else
        {
            previewLabel.setOpaque(true);
            previewLabel.setBackground(selectedColor);
        }
    }
}
