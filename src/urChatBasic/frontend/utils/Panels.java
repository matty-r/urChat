package urChatBasic.frontend.utils;

import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import urChatBasic.base.Constants.Size;

public class Panels {

    public static void addToPanel (JPanel targetPanel, Component newComponent, String label, Size targetSize)
    {

        int topSpacing = 6;
        final int TOP_ALIGNED = 0;
        final int LEFT_ALIGNED = 0;
        final int LEFT_SPACING = 6;

        if (null != label && !label.isBlank())
        {
            Panels.addToPanel(targetPanel, new JLabel(label + ":"), null, targetSize);
            // There is a label, so we want the added component to be aligned with the label
            topSpacing = 0;
        }

        if (targetPanel.getLayout().getClass() != SpringLayout.class)
        {
            targetPanel.setLayout(new SpringLayout());
        }

        SpringLayout layout = (SpringLayout) targetPanel.getLayout();
        Component[] components = targetPanel.getComponents();

        if (components.length > 0)
        {
            Component previousComponent = components[components.length - 1];

            // Add newComponent to the targetPanel
            targetPanel.add(newComponent);

            // Set constraints for newComponent
            layout.putConstraint(SpringLayout.NORTH, newComponent, topSpacing, SpringLayout.SOUTH, previousComponent);
            layout.putConstraint(SpringLayout.WEST, newComponent, LEFT_ALIGNED, SpringLayout.WEST, previousComponent);

            if (null != targetSize && newComponent instanceof JTextField)
                ((JTextField) newComponent).setColumns(12);
        } else
        {
            // If it's the first component, align it against the targetPanel
            targetPanel.add(newComponent);

            // Set constraints for newComponent when it's the first component
            layout.putConstraint(SpringLayout.NORTH, newComponent, topSpacing * 2, SpringLayout.NORTH, targetPanel);
            layout.putConstraint(SpringLayout.WEST, newComponent, LEFT_SPACING * 2, SpringLayout.WEST, targetPanel);

            if (null != targetSize && newComponent instanceof JTextField)
                ((JTextField) newComponent).setColumns(12);
        }
    }
}
