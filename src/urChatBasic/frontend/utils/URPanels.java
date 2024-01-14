package urChatBasic.frontend.utils;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import urChatBasic.backend.utils.URPreferencesUtil;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.base.Constants.Placement;
import urChatBasic.base.Constants.Size;

public class URPanels
{
    /**
     * Used to keep track of keys associated with specific components
     */
    private static Map<String, Map<Integer, String>> keyComponentAssociations = new HashMap<>();

    /**
     * Used to keep track of newly created labels against components
     */
    private static Map<String, Map<Integer, Integer>> componentLabelAssociations = new HashMap<>();

    public static Component addToPanel (JPanel targetPanel, Component newComponent, String label, Placement alignment,
            Size targetSize, String preferenceKey)
    {
        // TODO: add a visibility listener somehow for when a component is hidden, it's associated label is also hidden

        Class<? extends LayoutManager> layoutClass = targetPanel.getLayout().getClass();

        if (layoutClass == BorderLayout.class)
        {
            addToBorderPanel(targetPanel, newComponent, label, alignment, targetSize);
        } else
        {
            if (targetPanel.getLayout().getClass() != SpringLayout.class)
                targetPanel.setLayout(new SpringLayout());

            addToSpringPanel(targetPanel, newComponent, label, alignment, targetSize);
        }

        addKeyAssociation(targetPanel, newComponent, preferenceKey);

        return newComponent;
    }

    public static void addKeyAssociation (JPanel targetPanel, Component targetComponent, String preferenceKey)
    {
        if(keyComponentAssociations.get(targetPanel.toString()) == null)
            keyComponentAssociations.put(targetPanel.toString(), new HashMap<>());

        if(preferenceKey != null && !preferenceKey.isBlank())
            keyComponentAssociations.get(targetPanel.toString()).put(targetComponent.hashCode(), preferenceKey);
    }

    public static void addLabelAssociation (JPanel targetPanel, Component targetComponent, JLabel targetLabel)
    {
        if(componentLabelAssociations.get(targetPanel.toString()) == null)
            componentLabelAssociations.put(targetPanel.toString(), new HashMap<>());

        if(targetComponent != null && targetLabel != null)
            componentLabelAssociations.get(targetPanel.toString()).put(targetComponent.hashCode(), targetLabel.hashCode());
    }

    /**
     * Sets values on the interface components based on their associated Key and it's Default value. For the currently active Profile.
     * @param targetPanel
     */
    public static void getPreferences (JPanel targetPanel)
    {
        Preferences settingsPath = URProfilesUtil.getActiveProfilePath();

        Map<Integer, String> panelSettings = keyComponentAssociations.get(targetPanel.toString());
        Map<Integer, Component> panelComponents = Arrays.stream(targetPanel.getComponents())
                .collect(Collectors.toMap(Component::hashCode, component -> component, (v1, v2)-> v2));

        if(panelSettings != null)
        for (Integer componentHashcode : panelSettings.keySet())
        {
            String componentKeyString = panelSettings.get(componentHashcode);


            // No association, skip.
            if(componentKeyString == null)
                continue;

            Component targetComponent = panelComponents.get(componentHashcode);

            if(targetComponent instanceof JCheckBox)
            {
                ((JCheckBox) targetComponent).setSelected((boolean) URPreferencesUtil.getPref(componentKeyString, Constants.ConfigKeys.getDefault(componentKeyString), settingsPath));
            } else if(targetComponent instanceof JTextField || targetComponent instanceof JPasswordField)
            {
                ((JTextField) targetComponent).setText((String) URPreferencesUtil.getPref(componentKeyString, Constants.ConfigKeys.getDefault(componentKeyString), settingsPath));
            } else if(targetComponent instanceof JSlider)
            {
                ((JSlider) targetComponent).setValue((Integer) URPreferencesUtil.getPref(componentKeyString, Constants.ConfigKeys.getDefault(componentKeyString), settingsPath));
            } else if(targetComponent instanceof JComboBox)
            {
                ((JComboBox<?>) targetComponent).setSelectedItem((String) URPreferencesUtil.getPref(componentKeyString, Constants.ConfigKeys.getDefault(componentKeyString), settingsPath));
            }
        }
    }

    /**
     * Saves the preferences to the settingsPath of the currently active profile.
     * @param targetPanel
     */
    public static void putPreferences (JPanel targetPanel)
    {
        Preferences settingsPath = URProfilesUtil.getActiveProfilePath();

        Map<Integer, String> panelSettings = keyComponentAssociations.get(targetPanel.toString());
        Map<Integer, Component> panelComponents = Arrays.stream(targetPanel.getComponents())
                .collect(Collectors.toMap(Component::hashCode, component -> component, (v1, v2)-> v2));

        for (Integer componentHashcode : panelSettings.keySet())
        {
            String componentKeyString = panelSettings.get(componentHashcode);

            // No association, skip.
            if(componentKeyString == null)
                continue;

            Component targetComponent = panelComponents.get(componentHashcode);

            if(targetComponent instanceof JCheckBox)
            {
                URPreferencesUtil.putPref(componentKeyString, ((JCheckBox) targetComponent).isSelected(), settingsPath);
            } else if(targetComponent instanceof JTextField)
            {
                URPreferencesUtil.putPref(componentKeyString, ((JTextField) targetComponent).getText(), settingsPath);
            } else if(targetComponent instanceof JSlider)
            {
                URPreferencesUtil.putPref(componentKeyString, ((JSlider) targetComponent).getValue(), settingsPath);
            } else if(targetComponent instanceof JPasswordField)
            {
                String passwordString = "";
                passwordString = new String(((JPasswordField) targetComponent).getPassword());
                URPreferencesUtil.putPref(componentKeyString, passwordString, settingsPath);
            }
        }
    }

    private static void addToSpringPanel (JPanel targetPanel, Component newComponent, String label, Placement placement,
            Size targetSize)
    {
        boolean labelAdded = false;
        int topSpacing = 6;
        int leftSpacing = 6;
        final int TOP_ALIGNED = 0;
        final int LEFT_ALIGNED = 0;

        // Only add it now if the label would be "next" in the components list
        if (null != label && !label.isBlank() && placement == Placement.DEFAULT)
        {
            // Add the label, alignment should be the same,
            JLabel newLabel = (JLabel) URPanels.addToPanel(targetPanel, new JLabel(label + ":"), null, Placement.DEFAULT, null, null);

            addLabelAssociation(targetPanel, newComponent, newLabel);

            // There is a label, so we want the added component to be aligned with the label
            topSpacing = 0;
            labelAdded = true;
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
            switch (placement)
            {
                case RIGHT:
                    // attach the new component inline and to the right of the previous
                    topSpacing = 0;
                    newComponentTopAlign = SpringLayout.NORTH;
                    previousTopAlign = SpringLayout.NORTH;
                    newComponentLeftAlign = SpringLayout.WEST;
                    previousLeftAlign = SpringLayout.EAST;
                    break;
                case TOP:
                    // attach the new component inline and to the right of the previous
                    topSpacing = 0;
                    newComponentTopAlign = SpringLayout.SOUTH;
                    previousTopAlign = SpringLayout.NORTH;
                    newComponentLeftAlign = SpringLayout.WEST;
                    previousLeftAlign = SpringLayout.WEST;
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

            layout.putConstraint(newComponentTopAlign, newComponent, topSpacing, previousTopAlign,
                    previousTopComponent);
            layout.putConstraint(newComponentLeftAlign, newComponent, LEFT_ALIGNED, previousLeftAlign,
                    previousLeftComponent);

            JLabel newLabel = null;
            // Add the label above the newComponent, then reorder the newComponent to be the next logical
            // component
            if (label != null && !label.isBlank() && !labelAdded)
            {
                newLabel = (JLabel) URPanels.addToPanel(targetPanel, new JLabel(label + ":"), null, Placement.TOP, targetSize, null);
                targetPanel.setComponentZOrder(newComponent, targetPanel.getComponentZOrder(newComponent) + 1);

                addLabelAssociation(targetPanel, newComponent, newLabel);
            }

            // components = targetPanel.getComponents();

            if (null != targetSize)
            {
                targetSize.setComponentSize(newComponent);
            }
            else if(newComponent instanceof JCheckBox && placement == Placement.RIGHT)
            {
                layout.putConstraint(SpringLayout.EAST, newComponent, 0, SpringLayout.EAST, newLabel);
                layout.putConstraint(SpringLayout.SOUTH, newComponent, 0, SpringLayout.SOUTH, previousLeftComponent);
            }

        } else
        {
            // If it's the first component, align it against the targetPanel
            targetPanel.add(newComponent);

            // Set constraints for newComponent when it's the first component
            layout.putConstraint(SpringLayout.NORTH, newComponent, topSpacing * 2, SpringLayout.NORTH, targetPanel);
            layout.putConstraint(SpringLayout.WEST, newComponent, leftSpacing * 2, SpringLayout.WEST, targetPanel);

            if (null != targetSize)
                targetSize.setComponentSize(newComponent);
        }
    }

    private static void addToBorderPanel (JPanel targetPanel, Component newComponent, String label, Placement placement,
            Size targetSize)
    {
        if (null != label && !label.isBlank())
        {
            JPanel newPanelWithLabel = new JPanel(new BorderLayout());
            JLabel newLabel = (JLabel) URPanels.addToPanel(newPanelWithLabel, new JLabel(label + ":"), null, Placement.TOP, targetSize, null);

            addLabelAssociation(targetPanel, newComponent, newLabel);

            URPanels.addToPanel(newPanelWithLabel, newComponent, null, Placement.BOTTOM, targetSize, null);
            newComponent = newPanelWithLabel;
        }

        switch (placement)
        {
            case BOTTOM:
                targetPanel.add(newComponent, BorderLayout.SOUTH);
                break;
            case TOP:
                targetPanel.add(newComponent, BorderLayout.NORTH);
                break;
            default:
                targetPanel.add(newComponent);
                break;
        }

        if (null != targetSize)
            targetSize.setComponentSize(newComponent);

        // layout.putConstraint(newComponentTopAlign, newComponent, topSpacing, previousTopAlign,
        // previousTopComponent);
        // layout.putConstraint(newComponentLeftAlign, newComponent, LEFT_ALIGNED, previousLeftAlign,
        // previousLeftComponent);
    }

    public static JLabel getLabelForComponent (JPanel targetPanel, Component targetComponent)
    {
        Component[] components = targetPanel.getComponents();


        // First check the component-label associations.
        if(componentLabelAssociations.get(targetPanel.toString()) != null)
        {
            if(componentLabelAssociations.get(targetPanel.toString()).get(targetComponent.hashCode()) != null)
            {
                int labelHashCode = componentLabelAssociations.get(targetPanel.toString()).get(targetComponent.hashCode());

                for (Component component : components) {
                    if(component.hashCode() == labelHashCode && component instanceof JLabel)
                        return (JLabel) component;
                }
            }
        }


        // otherwise, just iterate over the components and find the closest Jlabel
        int componentIndex = -1;

        for (int i = 0; i < components.length; i++) {
            if(components[i] == targetComponent)
                componentIndex = i;
        }

        for (int x = componentIndex; x <= 0; x--) {
            if(components[x] instanceof JLabel)
                return (JLabel) components[x];
        }

        return null;
    }
}
