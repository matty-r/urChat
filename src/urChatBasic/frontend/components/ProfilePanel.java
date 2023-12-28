package urChatBasic.frontend.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants.Placement;
import urChatBasic.base.Constants.Size;
import urChatBasic.frontend.UserGUI;
import urChatBasic.frontend.panels.UROptionsPanel;
import urChatBasic.frontend.utils.Panels;

public class ProfilePanel extends UROptionsPanel
{
    public static final String PANEL_DISPLAY_NAME = "Profiles";
    private static final DefaultListModel<String> profilesListModel = new DefaultListModel<String>();
    private static final JList<String> profilesList = new JList<String>(profilesListModel);
    protected JScrollPane profileScroller = new JScrollPane(profilesList);

    private JTextField profileName = new JTextField(URProfilesUtil.getDefaultProfile());
    private JCheckBox setAsDefault = new JCheckBox("", true);

    public ProfilePanel (MainOptionsPanel optionsPanel)
    {
        super(PANEL_DISPLAY_NAME, optionsPanel);
        profilesListModel.addAll(Arrays.asList(URProfilesUtil.getProfiles()));
        profileName.setEditable(false);
        Panels.addToPanel(this, profileScroller, "Available Profiles", Placement.DEFAULT, Size.CUSTOM.customSize(200, 200));
        Panels.addToPanel(this, profileName, "Selected Profile", Placement.DEFAULT, Size.SMALL);

        setAsDefault.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent arg0)
            {

                URProfilesUtil.setDefaultProfile(profileName.getText());
                setDefaultCheckboxState();
            }
        });

        ListSelectionModel listSelectionModel = profilesList.getSelectionModel();
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionModel.addListSelectionListener(new ProfilesListSelectionHandler());

        Panels.addToPanel(this, setAsDefault, "Set as default", Placement.DEFAULT, null);

        UserGUI.addProfileChangeListener(e -> {
            String selectedProfileName = URProfilesUtil.getActiveProfileName();

            profilesList.setSelectedIndex(IntStream.range(0, profilesListModel.toArray().length)
                    .filter(i -> profilesListModel.toArray()[i].equals(selectedProfileName)).findFirst().orElse(-1));
        });
    }

    class ProfilesListSelectionHandler implements ListSelectionListener
    {
        public void valueChanged (ListSelectionEvent e)
        {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            if (!(lsm.isSelectionEmpty()) && !e.getValueIsAdjusting())
            {
                profileName.setText(profilesList.getSelectedValue());
                setDefaultCheckboxState();
            }
        }
    }

    /**
     * If a profile is set to be the default, don't let the checkbox to be unchecked. This forces
     * the selecting another profile then checking Set as Default.
     */
    private void setDefaultCheckboxState ()
    {
        if (URProfilesUtil.getDefaultProfile().equals(profileName.getText()))
        {
            setAsDefault.setEnabled(false);
            setAsDefault.setSelected(true);
        } else
        {
            setAsDefault.setEnabled(true);
            setAsDefault.setSelected(false);
        }
    }

}
