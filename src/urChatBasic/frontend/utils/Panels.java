package urChatBasic.frontend.utils;

import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import urChatBasic.base.Constants.Placement;
import urChatBasic.base.Constants.Size;

public class Panels {

    public static void addToPanel (JPanel targetPanel, Component newComponent, String label, Placement alignment, Size targetSize)
    {

        int topSpacing = 6;
        int leftSpacing = 6;
        final int TOP_ALIGNED = 0;
        final int LEFT_ALIGNED = 0;

        if (null != label && !label.isBlank())
        {
            Panels.addToPanel(targetPanel, new JLabel(label + ":"), null, alignment, targetSize);
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
            // Add newComponent to the targetPanel
            targetPanel.add(newComponent);


            // Used for Top Alignment
            Component previousTopComponent = components[components.length - 1];

            // Used for Left Alignment (default aligned with the previous component)
            Component previousLeftComponent = previousTopComponent;

            // Default puts the curent component left aligned and below the previous component
            String newComponentTopAlign = SpringLayout.NORTH;
            String previousTopAlign = SpringLayout.SOUTH;
            String newComponentLeftAlign = SpringLayout.WEST;
            String previousLeftAlign = SpringLayout.WEST;

            // Set constraints for newComponent
            switch (alignment) {
                case RIGHT:
                        // attach the new component inline and to the right of the previous
                        topSpacing = 0;
                        newComponentTopAlign = SpringLayout.NORTH;
                        previousTopAlign = SpringLayout.NORTH;
                        newComponentLeftAlign = SpringLayout.WEST;
                        previousLeftAlign = SpringLayout.EAST;
                    break;
                default:
                        // Aligned with the first component
                        previousLeftComponent = components[0];
                        newComponentTopAlign = SpringLayout.NORTH;
                        previousTopAlign = SpringLayout.SOUTH;
                        newComponentLeftAlign = SpringLayout.WEST;
                        previousLeftAlign = SpringLayout.WEST;
                    break;
            }

            layout.putConstraint(newComponentTopAlign, newComponent, topSpacing, previousTopAlign, previousTopComponent);
            layout.putConstraint(newComponentLeftAlign, newComponent, LEFT_ALIGNED, previousLeftAlign, previousLeftComponent);

            if (null != targetSize && newComponent instanceof JTextField)
                ((JTextField) newComponent).setColumns(12);
        } else
        {
            // If it's the first component, align it against the targetPanel
            targetPanel.add(newComponent);

            // Set constraints for newComponent when it's the first component
            layout.putConstraint(SpringLayout.NORTH, newComponent, topSpacing * 2, SpringLayout.NORTH, targetPanel);
            layout.putConstraint(SpringLayout.WEST, newComponent, leftSpacing * 2, SpringLayout.WEST, targetPanel);

            if (null != targetSize && newComponent instanceof JTextField)
                ((JTextField) newComponent).setColumns(12);
        }
    }
}
