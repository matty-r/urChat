package urChatBasic.frontend.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.UserGUI;

public class MainOptionsPanel extends JPanel
{

    private DefaultListModel<String> optionsArray = new DefaultListModel<String>();
    private JList<String> optionsList = new JList<String>(optionsArray);
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
        listSelectionModel.addListSelectionListener(new OptionsListSelectionHandler());

        optionsRightPanel.setLayout(new CardLayout());

        add(optionsLeftPanel, BorderLayout.LINE_START);
        add(optionsRightPanel, BorderLayout.CENTER);
        optionsList.setSelectedIndex(0);

        addToOptions("Connection", UserGUI.connectionScroller);
        addToOptions("Interface", UserGUI.interfaceScroller);
        addToOptions("Appearance", UserGUI.appearanceScroller);

        optionsLeftPanel.setBackground(optionsList.getBackground());
        optionsLeftPanel.setPreferredSize(new Dimension(100, 0));
        optionsLeftPanel.setLayout(new BorderLayout());

        optionsLeftPanel.add(optionsList, BorderLayout.NORTH);
        optionsLeftPanel.add(extrasPanel, BorderLayout.SOUTH);

        // Extras panel is below the options list, and just contains the profile switcher and the version label
        extrasPanel.add(urVersionLabel, BorderLayout.SOUTH);
    }

    public void setupOptionsPanel ()
    {
        profilePicker = new ProfilePicker(extrasPanel, DriverGUI.gui.getProfileName());
                extrasPanel.add(profilePicker, BorderLayout.NORTH);
    }

    public void addToOptions (String displayName, JScrollPane displayComponent)
    {
        optionsArray.addElement(displayName);
        optionsRightPanel.add(displayComponent, displayName);
    }

    /**
     * Used to change which panel to show when you choose an option under the Options Tab.
     */
    class OptionsListSelectionHandler implements ListSelectionListener
    {
        public void valueChanged (ListSelectionEvent e)
        {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            if (!(lsm.isSelectionEmpty()))
            {
                // Find out which indexes are selected.
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++)
                {
                    if (lsm.isSelectedIndex(i))
                    {
                        CardLayout cl = (CardLayout) (optionsRightPanel.getLayout());
                        cl.show(optionsRightPanel, (String) optionsArray.getElementAt(i));
                    }
                }
            }
        }
    }
}
