package urChatBasic.frontend;

import javax.swing.*;
import java.awt.*;

public class MessageDialog extends JDialog {
    private JLabel messageLabel;
    private JButton closeButton;

    public MessageDialog(Frame parent, String message, String title, int messageType) {
        super(parent, title, true);
        setSize(300, 150);
        setResizable(false);
        setMaximumSize(new Dimension(300, 150));
        setLocationRelativeTo(parent);

        messageLabel = new JLabel("<html><body style='width: 150px'>" + message + "</body></html>");
        messageLabel.setIcon(getIconForMessageType(messageType));
        messageLabel.setIconTextGap(15);
        messageLabel.setHorizontalAlignment(SwingConstants.LEFT);
        messageLabel.setVerticalAlignment(SwingConstants.TOP); // Top alignment for wrapping text


        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose()); // Close the dialog

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(closeButton, BorderLayout.SOUTH);

        add(panel);
    }

    private Icon getIconForMessageType(int messageType) {
        switch (messageType) {
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
