package urChatBasic.frontend.utils;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    private static Map<String, Map<Integer, String>> keyComponentAssociations = new HashMap<>();

    public static void addToPanel (JPanel targetPanel, Component newComponent, String label, Placement alignment,
            Size targetSize, String preferenceKey)
    {

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

        if(keyComponentAssociations.get(targetPanel.toString()) == null)
            keyComponentAssociations.put(targetPanel.toString(), new HashMap<>());

        keyComponentAssociations.get(targetPanel.toString()).put(newComponent.hashCode(), preferenceKey);
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
            } else if(targetComponent instanceof JTextField)
            {
                ((JTextField) targetComponent).setText((String) URPreferencesUtil.getPref(componentKeyString, Constants.ConfigKeys.getDefault(componentKeyString), settingsPath));
            } else if(targetComponent instanceof JSlider)
            {
                ((JSlider) targetComponent).setValue((Integer) URPreferencesUtil.getPref(componentKeyString, Constants.ConfigKeys.getDefault(componentKeyString), settingsPath));
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
            }
        }
    }

    private static void addToSpringPanel (JPanel targetPanel, Component newComponent, String label, Placement alignment,
            Size targetSize)
    {
        boolean labelAdded = false;
        int topSpacing = 6;
        int leftSpacing = 6;
        final int TOP_ALIGNED = 0;
        final int LEFT_ALIGNED = 0;

        // Only add it now if the label would be "next" in the components list
        if (null != label && !label.isBlank() && alignment == Placement.DEFAULT || alignment == Placement.BOTTOM)
        {
            URPanels.addToPanel(targetPanel, new JLabel(label + ":"), null, alignment, targetSize, null);
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
            switch (alignment)
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

            // Add the label above the newComponent, then reorder the newComponent to be the next logical
            // component
            if (null != label && !label.isBlank() && !labelAdded)
            {
                URPanels.addToPanel(targetPanel, new JLabel(label + ":"), null, Placement.TOP, targetSize, null);
                targetPanel.setComponentZOrder(newComponent, targetPanel.getComponentZOrder(newComponent) + 1);
            }

            if (null != targetSize)
            {
                if(newComponent instanceof JTextField)
                    ((JTextField) newComponent).setColumns(12);
                else if(targetSize == Size.CUSTOM)
                    newComponent.setPreferredSize(targetSize.getDimension());
            }

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

    private static void addToBorderPanel (JPanel targetPanel, Component newComponent, String label, Placement alignment,
            Size targetSize)
    {
        if (null != label && !label.isBlank())
        {
            JPanel newPanelWithLabel = new JPanel(new BorderLayout());
            URPanels.addToPanel(newPanelWithLabel, new JLabel(label + ":"), null, Placement.TOP, targetSize, null);
            URPanels.addToPanel(newPanelWithLabel, newComponent, null, Placement.BOTTOM, targetSize, null);
            newComponent = newPanelWithLabel;
        }

        switch (alignment)
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

        if (null != targetSize && newComponent instanceof JTextField)
            ((JTextField) newComponent).setColumns(12);

        // layout.putConstraint(newComponentTopAlign, newComponent, topSpacing, previousTopAlign,
        // previousTopComponent);
        // layout.putConstraint(newComponentLeftAlign, newComponent, LEFT_ALIGNED, previousLeftAlign,
        // previousLeftComponent);
    }
}
