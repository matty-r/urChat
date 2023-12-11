package urChatBasic.frontend.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import urChatBasic.base.DialogBase;
import urChatBasic.frontend.DriverGUI;

public class YesNoDialog extends DialogBase {
    private JLabel messageLabel;
    private JButton yesButton;
    private JButton noButton;

    public YesNoDialog (String message, String title, int messageType, Consumer<ActionEvent> returnFunction)
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


        yesButton = new JButton("Yes");
        noButton = new JButton("No");

        yesButton.addActionListener(e -> {
            if (returnFunction != null) {
                returnFunction.accept(e);
            }
            dispose();
        });

        noButton.addActionListener(e -> {
            if (returnFunction != null) {
                returnFunction.accept(e);
            }
            dispose();
        });

        dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(messageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(noButton);
        buttonPanel.add(yesButton);

        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);



        add(dialogPanel);
    }

    public JButton getNoButton()
    {
        return noButton;
    }

    public JButton getYesButton()
    {
        return yesButton;
    }
}
