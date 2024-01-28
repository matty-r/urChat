package urChatBasic.frontend.components;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.font.TextAttribute;
import java.util.Hashtable;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import urChatBasic.frontend.dialogs.ColourDialog;
import urChatBasic.backend.utils.URPreferencesUtil;
import urChatBasic.backend.utils.URStyle;
import urChatBasic.base.Constants;

public class FontPanel extends JPanel
{
    /**
     *
     */
    // private static final long serialVersionUID = 4044242988594083226L;
    private final JLabel TEXT_PREVIEW = new JLabel("A quick brown fox 0123456789");
    private final String[] FONT_LIST = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    private final JComboBox<String> FONT_COMBO_BOX = new JComboBox<String>(FONT_LIST);
    private final Integer[] FONT_SIZES = {8, 10, 11, 12, 14, 16, 18, 20, 24, 30, 36, 40, 60, 72};
    private final JComboBox<Integer> SIZES_COMBO_BOX = new JComboBox<>(FONT_SIZES);
    private final JCheckBox MAKE_BOLD = new JCheckBox("BOLD");
    private final JCheckBox MAKE_UNDERLINE = new JCheckBox("UNDERLINE");
    private final JCheckBox MAKE_ITALIC = new JCheckBox("ITALIC");
    private final JButton RESET_BUTTON = new JButton("Reset");
    private final JButton SAVE_BUTTON = new JButton("Save");
    private final JButton COLOUR_BUTTON = new JButton("Colour");
    protected ColourDialog colourDialog = null;
    private URStyle defaultStyle;
    private URStyle targetStyle;

    protected EventListenerList listenerList = new EventListenerList();
    protected transient ActionEvent actionEvent = null;

    private Preferences settingsPath;

    public FontPanel (String styleName, URStyle defaultStyle, Preferences settingsPath)
    {
        setLayout(new GridBagLayout());
        targetStyle = defaultStyle.clone();
        this.defaultStyle = defaultStyle;
        colourDialog = new ColourDialog(styleName, defaultStyle, settingsPath);
        setSettingsPath(settingsPath);
        setDefaultStyle(defaultStyle);

        COLOUR_BUTTON.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed (ActionEvent arg0)
            {
                colourDialog = new ColourDialog(styleName, getDefaultStyle(), getSettingsPath());

                colourDialog.getColourPanel().addSaveListener(e -> {
                    Constants.LOGGER.info( "Font Panel says: Save Colour pressed");
                });

                // Forwards the save listeners to the colour panel save listeners
                Object[] listeners = listenerList.getListenerList();

                for (int i = 0; i < listeners.length; i++)
                {
                    if (listeners[i] == ActionListener.class)
                    {
                        colourDialog.getColourPanel().addSaveListener((ActionListener) listenerList.getListeners(ActionListener.class)[i]);
                    }
                }

                colourDialog.setVisible(true);
            }
        });

        RESET_BUTTON.addActionListener(new ResetListener());

        SAVE_BUTTON.addActionListener(e -> {
            // Save the style first
            setFont(targetStyle, true);

            // now fire the rest of the save listeners
            fireSaveListeners();
        });

        FONT_COMBO_BOX.addItemListener(new FontSelectionChange());
        SIZES_COMBO_BOX.addItemListener(new FontSelectionChange());
        MAKE_BOLD.addActionListener(new CheckListener());
        MAKE_ITALIC.addActionListener(new CheckListener());
        MAKE_UNDERLINE.addActionListener(new CheckListener());

        // Reset the GridBagConstraints for MAIN_PANEL
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START; // Align components to the left
        // Set constraints for MAIN_PANEL
        // c.gridwidth = 6; // Assuming you want MAIN_PANEL to span three columns
        // c.weightx = 1.0;
        // c.weighty = 1.0;

        // First Row
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1.0;
        add(FONT_COMBO_BOX, c);

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        add(SIZES_COMBO_BOX, c);

        c.gridx = 2;
        c.gridy = 0;
        add(COLOUR_BUTTON, c);

        c.gridx = 3;
        c.gridy = 0;
        add(RESET_BUTTON, c);

        c.gridx = 4;
        c.gridy = 0;
        add(SAVE_BUTTON, c);

        // Second Row
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START; // Align components to the left
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3; // Assuming TEXT_PREVIEW spans three columns
        add(TEXT_PREVIEW, c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        add(MAKE_BOLD, c);

        c.gridx = 2;
        c.gridy = 1;
        add(MAKE_ITALIC, c);

        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 2;
        add(MAKE_UNDERLINE, c);
    }

    public JButton getSaveButton ()
    {
        return SAVE_BUTTON;
    }

    public JButton getResetButton ()
    {
        return RESET_BUTTON;
    }

    public void setDefaultStyle (final URStyle f)
    {
        // defaultFont = f;
        defaultStyle = f.clone();
        colourDialog.getColourPanel().setDefaultStyle(defaultStyle);
        loadStyle();
        fireSaveListeners();
    }

    private URStyle getDefaultStyle ()
    {
        return defaultStyle;
    }

    public URStyle getStyle ()
    {
        return targetStyle;
    }

    // Deletes the saved font, then loads the "default" font.
    // The default font for a channel is the Profile Font, the default font
    // the UserGUI is Constants.DEFAULT_FONT
    public void resetFont ()
    {
        URPreferencesUtil.deleteStyleFont(targetStyle, settingsPath);
        setStyle(defaultStyle);
        loadStyle();
    }

    public void loadStyle ()
    {
        // setFont(URPreferencesUtil.loadStyleFont(defaultFont, settingsPath), false);
        setStyle(URPreferencesUtil.loadStyle(defaultStyle, settingsPath));
        // setFont(URPreferencesUtil.loadStyle(targetStyle, settingsPath).getFont(), false);
    }

    @Override
    public void setFont (Font f)
    {
        super.setFont(f);

        if (TEXT_PREVIEW != null)
            TEXT_PREVIEW.setFont(f);
    }

    public void setStyle (final URStyle newStyle)
    {
        targetStyle = newStyle.clone();
        colourDialog.getColourPanel().setStyle(targetStyle);
        setFont(targetStyle, false);
    }

    /**
     * Sets the appropriate options in the FontPanel, i.e Bold checkbox is checked if the newStyle is bold. Will also save the preferences if saveToSettings is
     * set to true.
     *
     * @param newStyle
     * @param saveToSettings
     */
    public void setFont (final URStyle newStyle, Boolean saveToSettings)
    {
        Font newFont = newStyle.getFont();

        if (!getFont().equals(newFont) || saveToSettings)
        {
            newStyle.isBold().ifPresent(bold -> MAKE_BOLD.setSelected(bold));
            newStyle.isItalic().ifPresent(italic -> MAKE_ITALIC.setSelected(italic));
            newStyle.isUnderline().ifPresent(underline -> MAKE_UNDERLINE.setSelected(underline));
            newStyle.getFamily().ifPresent(family -> FONT_COMBO_BOX.setSelectedItem(family));
            newStyle.getSize().ifPresent(size -> SIZES_COMBO_BOX.setSelectedItem(size));

            targetStyle.setFont(newFont);

            if (saveToSettings)
            {
                URStyle colourPanelStyle = colourDialog.getColourPanel().getStyle();
                colourPanelStyle.getForeground().ifPresent(fg -> targetStyle.setForeground(fg));
                colourPanelStyle.getBackground().ifPresent(bg -> targetStyle.setBackground(bg));
                URPreferencesUtil.saveStyle(defaultStyle, targetStyle, settingsPath);
            }

            revalidate();
            repaint();
            previewFont();
        }
    }

    // https://docs.oracle.com/javase/6/docs/api/java/awt/font/TextAttribute.html
    private void previewFont ()
    {

        Map<TextAttribute, Object> fontMap = new Hashtable<TextAttribute, Object>();

        if (MAKE_BOLD.isSelected())
            fontMap.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        if (MAKE_ITALIC.isSelected())
            fontMap.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        if (MAKE_UNDERLINE.isSelected())
            fontMap.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);

        setFont(new Font(FONT_COMBO_BOX.getSelectedItem().toString(), Font.PLAIN, Integer.parseInt(SIZES_COMBO_BOX.getSelectedItem().toString()))
                .deriveFont(fontMap));

        targetStyle.setFont(getFont());
    }

    public void addSaveListener (ActionListener actionListener)
    {
        listenerList.add(ActionListener.class, actionListener);
    }

    protected void fireSaveListeners ()
    {
        Object[] listeners = this.listenerList.getListenerList();

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

    // // Method to retrieve all added listeners
    // public List<ActionListener> getActionListeners ()
    // {
    // return actionListeners;
    // }

    class CheckListener implements ActionListener
    {

        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            previewFont();
        }
    }

    class FontSelectionChange implements ItemListener
    {

        @Override
        public void itemStateChanged (ItemEvent e)
        {
            previewFont();
        }

    }

    class ResetListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent e)
        {
            resetFont();
        }
    }

    public void setSettingsPath (Preferences settingsPath)
    {
        this.settingsPath = settingsPath;
        loadStyle();

        colourDialog.getColourPanel().setSettingsPath(settingsPath);
    }

    public Preferences getSettingsPath ()
    {
        return settingsPath;
    }

}
