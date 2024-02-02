package urChatBasic.frontend.components;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class FontPreview extends JPanel
{

    private JTextPane previewTextArea = new JTextPane();
    private JScrollPane previewScroll = new JScrollPane(previewTextArea);

    public FontPreview ()
    {

    }
}
