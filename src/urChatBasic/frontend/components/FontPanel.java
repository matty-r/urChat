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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.*;
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

    /**
     * TODO: This will be used instead for creating the action listeners: private Map<JButton,
     * EventListenerList> actionList = new HashMap<>();
     *
     * @see ColourPanel.fireSaveListeners()
     */
    private List<ActionListener> actionListeners = new ArrayList<>();

    private Preferences settingsPath;

    public FontPanel (String styleName, URStyle defaultStyle, Preferences settingsPath)
    {
        setLayout(new GridBagLayout());
        setSettingsPath(settingsPath);
        targetStyle = new URStyle(styleName, defaultStyle.getFont());
        this.defaultStyle = defaultStyle;
        setDefaultFont(targetStyle.getFont());
        colourDialog = new ColourDialog(styleName, defaultStyle, settingsPath);

        RESET_BUTTON.addActionListener(new ResetListener());
        COLOUR_BUTTON.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed (ActionEvent arg0)
            {
                if (colourDialog == null)
                {
                    ;

                    colourDialog.getColourPanel().addSaveListener(e -> {
                        // URPreferencesUtil.saveStyle(targetStyle, settingsPath);
                        Constants.LOGGER.log(Level.INFO, "Font Panel says: Save Colour pressed");
                    });

                    for (ActionListener actionListener : actionListeners)
                    {
                        colourDialog.getColourPanel().addSaveListener(actionListener);
                    }
                }

                colourDialog.setVisible(true);
            }
        });

        addActionListener(SAVE_BUTTON, new SaveListener());
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

    public void setDefaultFont (Font f)
    {
        // defaultFont = f;
        defaultStyle.setFont(f);
        loadStyle();
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
        setStyle(URPreferencesUtil.loadStyle(targetStyle, settingsPath));
        // setFont(URPreferencesUtil.loadStyle(targetStyle, settingsPath).getFont(), false);
    }

    @Override
    public void setFont (Font f)
    {
        super.setFont(f);

        if (TEXT_PREVIEW != null)
            TEXT_PREVIEW.setFont(f);
    }

    public void setStyle (URStyle newStyle)
    {
        targetStyle = newStyle.clone();

        setFont(targetStyle, false);
    }

    public void setFont (URStyle newStyle, Boolean saveToSettings)
    {
        Font newFont = newStyle.getFont();

        if (getFont() != newFont || saveToSettings)
        {
            MAKE_BOLD.setSelected(newFont.isBold());
            MAKE_ITALIC.setSelected(newFont.isItalic());
            MAKE_UNDERLINE.setSelected(newStyle.isUnderline());
            FONT_COMBO_BOX.setSelectedItem(newFont.getFamily());
            SIZES_COMBO_BOX.setSelectedItem(newFont.getSize());

            targetStyle.setFont(newFont);
            if (saveToSettings)
            {
                URStyle colourPanelStyle = colourDialog.getColourPanel().getStyle();
                targetStyle.setForeground(colourPanelStyle.getForeground());
                targetStyle.setBackground(colourPanelStyle.getBackground());
                targetStyle.save(settingsPath);
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

        setFont(new Font(FONT_COMBO_BOX.getSelectedItem().toString(), Font.PLAIN,
                Integer.parseInt(SIZES_COMBO_BOX.getSelectedItem().toString())).deriveFont(fontMap));

        targetStyle.setFont(getFont());
    }

    // Override the addActionListener method to keep track of added listeners
    public void addActionListener (JButton targetButton, ActionListener listener)
    {
        actionListeners.add(listener);
        targetButton.addActionListener(listener);
    }


    // Method to retrieve all added listeners
    public List<ActionListener> getActionListeners ()
    {
        return actionListeners;
    }

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

    class SaveListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent e)
        {
            // FontPanel.this.setFont(TEXT_PREVIEW.getFont(), true);
            setFont(targetStyle, true);
        }
    }

    class ResetListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent e)
        {
            FontPanel.this.resetFont();
        }
    }

    public void setSettingsPath (Preferences settingsPath)
    {
        this.settingsPath = settingsPath;
    }

    public Preferences getSettingsPath ()
    {
        return settingsPath;
    }

}
