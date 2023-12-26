package urChatBasic.frontend.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.UserGUI;

public class OptionsPanel extends JPanel
{

    private DefaultListModel<String> optionsArray = new DefaultListModel<String>();
    private JList<String> optionsList = new JList<String>(optionsArray);
    private JPanel optionsLeftPanel = new JPanel();

    private JPanel optionsRightPanel = new JPanel();
    private URVersionLabel urVersionLabel;

    private ProfilePicker profilePicker;

    public URVersionLabel getUrVersionLabel ()
    {
        return urVersionLabel;
    }


    public ProfilePicker getProfilePicker ()
    {
        return profilePicker;
    }

    public void setupOptionsPanel ()
    {
        setLayout(new BorderLayout());

        optionsArray.addElement("Connection");
        optionsArray.addElement("Interface");
        optionsArray.addElement("Appearance");

        setupLeftOptionsPanel();
        ListSelectionModel listSelectionModel = optionsList.getSelectionModel();
        listSelectionModel.addListSelectionListener(new OptionsListSelectionHandler());

        // optionsRightPanel.setBackground(Color.BLACK);
        optionsRightPanel.setLayout(new CardLayout());


        add(optionsLeftPanel, BorderLayout.LINE_START);
        add(optionsRightPanel, BorderLayout.CENTER);
        optionsList.setSelectedIndex(0);

        optionsRightPanel.add(UserGUI.connectionScroller, "Connection");
        optionsRightPanel.add(UserGUI.interfaceScroller, "Interface");
        optionsRightPanel.add(UserGUI.appearanceScroller, "Appearance");
    }

    /**
     * Houses the options list
     */
    private void setupLeftOptionsPanel ()
    {
        optionsLeftPanel.setBackground(optionsList.getBackground());
        optionsLeftPanel.setPreferredSize(new Dimension(100, 0));
        optionsLeftPanel.setLayout(new BorderLayout());

        optionsLeftPanel.add(optionsList, BorderLayout.NORTH);

        JPanel extrasPanel = new JPanel(new BorderLayout());
        extrasPanel.setBackground(optionsLeftPanel.getBackground());

        urVersionLabel = new URVersionLabel(extrasPanel);
        profilePicker = new ProfilePicker(extrasPanel, DriverGUI.gui.getProfileName());

        extrasPanel.add(profilePicker, BorderLayout.NORTH);
        extrasPanel.add(urVersionLabel, BorderLayout.SOUTH);

        optionsLeftPanel.add(extrasPanel, BorderLayout.SOUTH);
    }

    /**
     * Used to change which panel to show when you choose an option under the Options Tab.
     *
     * @author Matt
     *
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
