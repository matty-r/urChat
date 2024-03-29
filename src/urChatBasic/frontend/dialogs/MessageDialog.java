package urChatBasic.frontend.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
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
        setupDialog(message, title, messageType);
        setActionListener(closeButton, null);
    }

    public MessageDialog (String message, String title, int messageType, Consumer<ActionEvent> actionFunction)
    {
        super(DriverGUI.frame, title, true);
        setupDialog(message, title, messageType);
        setActionListener(closeButton, actionFunction);
    }

    public void setupDialog(String message, String title, int messageType)
    {
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

        dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(messageLabel, BorderLayout.CENTER);
        dialogPanel.add(closeButton, BorderLayout.SOUTH);

        add(dialogPanel);
    }

    private void setActionListener(JButton targetButton, Consumer<ActionEvent> actionFunction )
    {
        targetButton.addActionListener(e -> {
            if (actionFunction != null) {
                actionFunction.accept(e);
            }
            dispose();
        });
    }

    public JButton getCloseButton()
    {
        return closeButton;
    }
}
