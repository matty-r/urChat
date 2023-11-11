package urChatBasic.frontend.components;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.Preferences;
import javax.swing.*;
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
    private final JCheckBox MAKE_ITALIC = new JCheckBox("ITALIC");
    private final JButton RESET_BUTTON = new JButton("Reset Font");
    private final JButton SAVE_BUTTON = new JButton("Save Font");
    // private String fontType = "New Font:";
    // private JLabel fontTypeLabel = new JLabel("New Font:");
    // private final JPanel TITLE_PANEL = new JPanel(new GridLayout(1,1));
    private final JPanel BUTTON_PANEL = new JPanel(new GridLayout(1, 2));
    private final JPanel MAIN_PANEL = new JPanel(new GridLayout(2, 3));
    private Font defaultFont;
    // private final JButton CANCEL_BUTTON = new JButton("Cancel");
    // TODO: Add colour picker for foreground and background

    private Preferences settingsPath;

    public FontPanel(Font defaultFont, Preferences settingsPath, String fontName)
    {
        // setPreferredSize(new Dimension(0, 100));
        // fontTypeLabel = new JLabel(fontName);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        // c.fill = GridBagConstraints.HORIZONTAL;

        // c.gridx = 0;
        // c.gridy = 0;

        // TITLE_PANEL.add(fontTypeLabel);
        // add(TITLE_PANEL, c);

        setSettingsPath(settingsPath);
        setDefaultFont(defaultFont);
        loadFont();

        MAIN_PANEL.add(FONT_COMBO_BOX);
        MAIN_PANEL.add(SIZES_COMBO_BOX);
        MAIN_PANEL.add(TEXT_PREVIEW);
        MAIN_PANEL.add(MAKE_BOLD);
        MAIN_PANEL.add(MAKE_ITALIC);

        BUTTON_PANEL.add(RESET_BUTTON);
        BUTTON_PANEL.add(SAVE_BUTTON);

        MAIN_PANEL.add(BUTTON_PANEL);


        RESET_BUTTON.addActionListener(new ResetListener());
        SAVE_BUTTON.addActionListener(new SaveListener());
        FONT_COMBO_BOX.addItemListener(new FontSelectionChange());
        SIZES_COMBO_BOX.addItemListener(new FontSelectionChange());
        MAKE_BOLD.addActionListener(new CheckListener());
        MAKE_ITALIC.addActionListener(new CheckListener());

        // Reset the GridBagConstraints for MAIN_PANEL
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;

        // Set constraints for MAIN_PANEL
        c.gridwidth = 1; // Assuming you want MAIN_PANEL to span two columns
        c.weightx = 1.0;
        c.weighty = 1.0;

        add(MAIN_PANEL, c);
    }

    public JButton getSaveButton()
    {
        return SAVE_BUTTON;
    }

    public JButton getResetButton()
    {
        return RESET_BUTTON;
    }

    public void setDefaultFont(Font f)
    {
        defaultFont = f;
        loadFont();
    }

    // Deletes the saved font, then loads the "default" font.
    // The default font for a channel is the Global Font, the default font
    // the UserGUI is Constants.DEFAULT_FONT
    public void resetFont() {
        settingsPath.remove(Constants.KEY_FONT_BOLD);
        settingsPath.remove(Constants.KEY_FONT_ITALIC);
        settingsPath.remove(Constants.KEY_FONT_SIZE);
        settingsPath.remove(Constants.KEY_FONT_FAMILY);

        loadFont();
    }

    public void loadFont()
    {
        Font savedFont = defaultFont;
        int savedFontBoldItalic = 0;

        if (settingsPath.getBoolean(Constants.KEY_FONT_BOLD, defaultFont.isBold()))
            savedFontBoldItalic = Font.BOLD;
        if (settingsPath.getBoolean(Constants.KEY_FONT_ITALIC, defaultFont.isItalic()))
            savedFontBoldItalic |= Font.ITALIC;

        savedFont = new Font(settingsPath.get(Constants.KEY_FONT_FAMILY, defaultFont.getFamily()),
                savedFontBoldItalic, settingsPath.getInt(Constants.KEY_FONT_SIZE, defaultFont.getSize()));

        setFont(savedFont, false);
    }

    @Override
    public void setFont(Font f)
    {
        super.setFont(f);

        if (TEXT_PREVIEW != null)
            TEXT_PREVIEW.setFont(f);
    }

    public void setFont(Font newFont, Boolean saveToSettings)
    {
        if (getFont() != newFont || saveToSettings)
        {
            MAKE_BOLD.setSelected(newFont.isBold());
            if (saveToSettings)
            {
                settingsPath.putBoolean(Constants.KEY_FONT_BOLD, newFont.isBold());
            }

            MAKE_ITALIC.setSelected(newFont.isItalic());
            if (saveToSettings)
            {
                settingsPath.putBoolean(Constants.KEY_FONT_ITALIC, newFont.isItalic());
            }

            FONT_COMBO_BOX.setSelectedItem(newFont.getFamily());

            if (saveToSettings)
            {
                settingsPath.put(Constants.KEY_FONT_FAMILY, newFont.getFamily());
            }

            SIZES_COMBO_BOX.setSelectedItem(newFont.getSize());
            if (saveToSettings)
            {
                settingsPath.putInt(Constants.KEY_FONT_SIZE, newFont.getSize());
            }

            revalidate();
            repaint();
            previewFont();
        }
    }

    private void previewFont()
    {

        int boldItalic = 0;

        if (MAKE_BOLD.isSelected())
            boldItalic = Font.BOLD;
        if (MAKE_ITALIC.isSelected())
            boldItalic |= Font.ITALIC;

        setFont(
            new Font(FONT_COMBO_BOX.getSelectedItem().toString(),
            boldItalic,
            Integer.parseInt(SIZES_COMBO_BOX.getSelectedItem().toString())
        ));
    }

    class CheckListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            previewFont();
        }
    }

    class FontSelectionChange implements ItemListener
    {

        @Override
        public void itemStateChanged(ItemEvent e)
        {
            previewFont();
        }

    }

    class SaveListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            FontPanel.this.setFont(TEXT_PREVIEW.getFont(), true);
        }
    }

    class ResetListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            FontPanel.this.resetFont();
        }
    }

    public void setSettingsPath(Preferences settingsPath)
    {
        this.settingsPath = settingsPath;
    }

    public Preferences getSettingsPath()
    {
        return settingsPath;
    }

}
