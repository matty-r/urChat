package urChatBasic.frontend.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import urChatBasic.base.DialogBase;
import urChatBasic.frontend.DriverGUI;

public class MessageDialog extends DialogBase {
    private JLabel messageLabel;
    private JButton closeButton;

    public MessageDialog (String message, String title, int messageType)
    {
        super(DriverGUI.frame, title, true);
        setSize(300, 150);
        setResizable(false);
        setMaximumSize(new Dimension(300, 150));
        setLocationRelativeTo(DriverGUI.frame);

        messageLabel = new JLabel("<html><body style='width: 150px'>" + message + "</body></html>");
        messageLabel.setIcon(DialogBase.getIconForMessageType(messageType));
        messageLabel.setIconTextGap(15);
        messageLabel.setHorizontalAlignment(SwingConstants.LEFT);
        messageLabel.setVerticalAlignment(SwingConstants.TOP); // Top alignment for wrapping text


        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose()); // Close the dialog

        dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(messageLabel, BorderLayout.CENTER);
        dialogPanel.add(closeButton, BorderLayout.SOUTH);

        add(dialogPanel);
    }
}
