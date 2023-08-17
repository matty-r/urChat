package urChatBasic.base;

import javax.swing.*;
import java.awt.*;

public class DialogBase extends JDialog
{
    protected JPanel dialogPanel;

    public DialogBase(Frame parent, String title, Boolean isModal)
    {
        super(parent, title, isModal);
    }

    protected static Icon getIconForMessageType(int messageType)
    {
        switch (messageType)
        {
            case JOptionPane.ERROR_MESSAGE:
                return UIManager.getIcon("OptionPane.errorIcon");
            case JOptionPane.INFORMATION_MESSAGE:
                return UIManager.getIcon("OptionPane.informationIcon");
            case JOptionPane.WARNING_MESSAGE:
                return UIManager.getIcon("OptionPane.warningIcon");
            case JOptionPane.QUESTION_MESSAGE:
                return UIManager.getIcon("OptionPane.questionIcon");
            default:
                return null;
        }
    }
}
