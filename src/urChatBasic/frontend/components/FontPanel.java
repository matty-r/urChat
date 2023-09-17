package urChatBasic.frontend.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
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
    private static final long serialVersionUID = 4044242988594083226L;
    private final JLabel TEXT_PREVIEW = new JLabel("A quick brown fox 0123456789");
    private final String[] FONT_LIST = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    private final JComboBox<String> FONT_COMBO_BOX = new JComboBox<String>(FONT_LIST);
    private final Integer[] FONT_SIZES = {8, 10, 11, 12, 14, 16, 18, 20, 24, 30, 36, 40, 60, 72};
    private final JComboBox<Integer> SIZES_COMBO_BOX = new JComboBox<>(FONT_SIZES);
    private final JCheckBox MAKE_BOLD = new JCheckBox("BOLD");
    private final JCheckBox MAKE_ITALIC = new JCheckBox("ITALIC");
    private final JButton SAVE_BUTTON = new JButton("Save Font");
    private Font defaultFont;
    // private final JButton CANCEL_BUTTON = new JButton("Cancel");

    private Preferences settingsPath;

    public FontPanel(Font defaultFont, Preferences settingsPath)
    {
        setPreferredSize(new Dimension(0, 50));
        setLayout(new GridLayout(2, 6));

        setSettingsPath(settingsPath);
        setDefaultFont(defaultFont);
        loadFont();

        add(FONT_COMBO_BOX);
        add(SIZES_COMBO_BOX);
        add(MAKE_BOLD);
        add(MAKE_ITALIC);
        add(TEXT_PREVIEW);
        add(SAVE_BUTTON);



        SAVE_BUTTON.addActionListener(new SaveListener());
        FONT_COMBO_BOX.addItemListener(new FontSelectionChange());
        SIZES_COMBO_BOX.addItemListener(new FontSelectionChange());
        MAKE_BOLD.addActionListener(new CheckListener());
        MAKE_ITALIC.addActionListener(new CheckListener());
    }

    public JButton getSaveButton()
    {
        return SAVE_BUTTON;
    }

    public void setDefaultFont(Font f)
    {
        defaultFont = f;
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

    public void setSettingsPath(Preferences settingsPath)
    {
        this.settingsPath = settingsPath;
    }

    public Preferences getSettingsPath()
    {
        return settingsPath;
    }

}
