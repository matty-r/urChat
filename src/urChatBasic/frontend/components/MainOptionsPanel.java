package urChatBasic.frontend.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Optional;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants.Placement;
import urChatBasic.frontend.panels.UROptionsPanel;
import urChatBasic.frontend.utils.Panels;

public class MainOptionsPanel extends JPanel
{
    private DefaultListModel<UROptionsPanel> optionsArray = new DefaultListModel<UROptionsPanel>();
    private JList<UROptionsPanel> optionsList = new JList<UROptionsPanel>(optionsArray);

    private JPanel optionsLeftPanel = new JPanel();
    private JPanel extrasPanel = new JPanel(new BorderLayout());
    private JPanel optionsRightPanel = new JPanel();
    private URVersionLabel urVersionLabel = new URVersionLabel(extrasPanel);


    private ProfilePicker profilePicker;

    public URVersionLabel getUrVersionLabel ()
    {
        return urVersionLabel;
    }

    public ProfilePicker getProfilePicker ()
    {
        return profilePicker;
    }

    public MainOptionsPanel ()
    {
        setLayout(new BorderLayout());

        ListSelectionModel listSelectionModel = optionsList.getSelectionModel();
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionModel.addListSelectionListener(new OptionsListSelectionHandler());

        optionsRightPanel.setLayout(new CardLayout());

        add(optionsLeftPanel, BorderLayout.LINE_START);
        add(optionsRightPanel, BorderLayout.CENTER);

        optionsLeftPanel.setBackground(optionsList.getBackground());
        optionsLeftPanel.setPreferredSize(new Dimension(100, 0));
        optionsLeftPanel.setLayout(new BorderLayout());

        optionsLeftPanel.add(optionsList, BorderLayout.NORTH);
        optionsLeftPanel.add(extrasPanel, BorderLayout.SOUTH);

        // optionsLeftPanel.setBackground(Color.yellow);
        extrasPanel.setBackground(Color.green);
        // Extras panel is below the options list, and just contains the profile switcher and the version
        // label
        Panels.addToPanel(extrasPanel, urVersionLabel, null, Placement.BOTTOM, null);

        profilePicker = new ProfilePicker(URProfilesUtil.getActiveProfileName(), true);

        Panels.addToPanel(extrasPanel, profilePicker, "Active Profile", Placement.TOP, null);

        // extrasPanel.add(urVersionLabel, BorderLayout.SOUTH);
    }

    public void addToOptions (String displayName, UROptionsPanel displayComponent, Optional<Integer> index)
    {
        if (index.isEmpty() || index.get() >= optionsArray.size())
        {
            optionsArray.addElement(displayComponent);
        }
        else
        {
            optionsArray.add(index.get(), displayComponent);
        }

        optionsRightPanel.add(displayComponent.getScroller(), displayName);

        optionsList.setSelectedIndex(0);
    }

    /**
     * Used to change which panel to show when you choose an option under the Options Tab. ActionListeners will be fired as appropriate i.e. the display
     * listeners will be fired when a selection is made, and the hide listeners will be fired for all others.
     */
    class OptionsListSelectionHandler implements ListSelectionListener
    {
        public void valueChanged (ListSelectionEvent e)
        {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            if (!(lsm.isSelectionEmpty()) && !e.getValueIsAdjusting())
            {
                // Find out which indexes are selected.
                int selectedIndex = lsm.getLeadSelectionIndex();
                CardLayout cl = (CardLayout) (optionsRightPanel.getLayout());


                for (int i = 0; i < optionsArray.size(); i++)
                {
                    UROptionsPanel currentPanel = optionsArray.getElementAt(i);

                    if (i == selectedIndex)
                    {
                        cl.show(optionsRightPanel, currentPanel.toString());
                        currentPanel.fireListeners(true);
                    } else
                    {
                        currentPanel.fireListeners(false);
                    }
                }
            }
        }
    }
}
