package urChatBasic.frontend.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import urChatBasic.base.Constants.Size;

public class URComponent extends JPanel {
    private JLabel label;
    private JComponent component;
    private Size size;

    public URComponent(String labelText, JComponent component, Size size) {
        this.label = new JLabel(labelText);
        this.component = component;
        this.size = size;
        init();
    }

    private int convertSize()
    {
        switch (size) {
            case LARGE:
                return 240;
            case MEDIUM:
                return 160;
            case SMALL:
                return 80;
            default:
                return 60;
        }
    }

    private void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        label.setAlignmentX( Component.LEFT_ALIGNMENT);
        label.setPreferredSize( new Dimension(convertSize(), label.getPreferredSize().height));

        component.setAlignmentX( Component.LEFT_ALIGNMENT);
        component.setPreferredSize( new Dimension(convertSize(), component.getPreferredSize().height));
        add(label);
        add(component);
    }

    public JComponent getComponent() {
        return component;
    }

    public JTextField getAsTextField()
    {
        return (JTextField) getComponent();
    }

    public void setComponent(JComponent component) {
        this.component = component;
    }
}
