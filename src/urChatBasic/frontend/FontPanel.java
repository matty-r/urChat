package urChatBasic.frontend;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

public class FontPanel extends JPanel
{
    /**
     *
     */
    private static final long serialVersionUID = 4044242988594083226L;
    private final JLabel TEXT_PREVIEW = new JLabel("A quick brown fox 0123456789");
    private final String[] FONT_LIST = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    private final JComboBox<String> FONT_COMBO_BOX = new JComboBox<String>(FONT_LIST);
    private final String[] FONT_SIZES =
            {"8", "10", "11", "12", "14", "16", "18", "20", "24", "30", "36", "40", "48", "60", "72"};
    private final JComboBox<String> SIZES_COMBO_BOX = new JComboBox<String>(FONT_SIZES);
    private final JCheckBox MAKE_BOLD = new JCheckBox("BOLD");
    private final JCheckBox MAKE_ITALIC = new JCheckBox("ITALIC");
    private final JButton SAVE_BUTTON = new JButton("Save Font");
    // private final JButton CANCEL_BUTTON = new JButton("Cancel");
    private Font myFont;
    private Font savedFont;
    private JPanel myPanel;

    public FontPanel(JPanel tempPanel)
    {
        myPanel = tempPanel;
        myFont = tempPanel.getFont();
        // myPanel = panel;
        this.setPreferredSize(new Dimension(0, 50));
        this.setLayout(new GridLayout(2, 6));
        //
        this.add(FONT_COMBO_BOX);
        FONT_COMBO_BOX.addItemListener(new FontSelectionChange());
        FONT_COMBO_BOX.setSelectedItem(myFont.getFamily());
        //
        this.add(SIZES_COMBO_BOX);
        SIZES_COMBO_BOX.addItemListener(new FontSelectionChange());
        SIZES_COMBO_BOX.setSelectedItem(String.valueOf(myFont.getSize()));
        //
        this.add(MAKE_BOLD);
        MAKE_BOLD.addActionListener(new CheckListener());
        MAKE_BOLD.setSelected(myFont.isBold());
        //
        this.add(MAKE_ITALIC);
        MAKE_ITALIC.addActionListener(new CheckListener());
        MAKE_ITALIC.setSelected(myFont.isItalic());

        this.add(TEXT_PREVIEW);
        this.add(SAVE_BUTTON);
        SAVE_BUTTON.addActionListener(new SaveListener());
    }

    public Font getFont()
    {
        return savedFont;
    }

    private void previewFont()
    {

        int boldItalic = 0;

        if (MAKE_BOLD.isSelected())
            boldItalic = Font.BOLD;
        if (MAKE_ITALIC.isSelected())
            boldItalic |= Font.ITALIC;

        savedFont = new Font(FONT_COMBO_BOX.getSelectedItem().toString(), boldItalic,
                Integer.parseInt(SIZES_COMBO_BOX.getSelectedItem().toString()));
        TEXT_PREVIEW.setFont(savedFont);
    }

    class CheckListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            previewFont();
        }

    }

    class SaveListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            // FontPanel.super.setFont(savedFont);
            myPanel.setFont(savedFont);
            // FontPanel.this.setVisible(false);
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

}
