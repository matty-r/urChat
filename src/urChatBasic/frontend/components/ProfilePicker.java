package urChatBasic.frontend.components;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.base.Constants.EventType;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.UserGUI;
import urChatBasic.frontend.dialogs.MessageDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

public class ProfilePicker extends JPanel
{
    private JComboBox<String> profileComboBox = new JComboBox<>(URProfilesUtil.getProfiles());
    private ActionListener changeListener = new ProfileChangeListener();
    private final JButton saveProfile = new JButton("Save");

    public ProfilePicker (String initialProfile, Boolean showSaveButton)
    {
        // profileComboBox = new JComboBox<String>();
        loadProfiles(initialProfile);
        profileComboBox.setEditable(false);

        // setBackground(Color.BLUE);
        setLayout(new GridLayout(1, 1));

        // Add components to the panel
        add(profileComboBox);

        if (showSaveButton)
        {
            setLayout(new GridLayout(2, 1));
            add(saveProfile);
        }

        profileComboBox.addActionListener(changeListener);

        URProfilesUtil.addListener(EventType.CREATE, e -> {
            loadProfiles(initialProfile);
        });

        URProfilesUtil.addListener(EventType.DELETE, e -> {
            loadProfiles(initialProfile);
        });

        saveProfile.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent arg0)
            {
                DriverGUI.gui.setClientSettings();
            }
        });
    }

    public JComboBox<String> getProfileComboBox ()
    {
        return profileComboBox;
    }

    private class ProfileChangeListener implements ActionListener
    {

        @Override
        public void actionPerformed (ActionEvent arg0)
        {
            String profileString = "";
            if(profileComboBox.getSelectedItem() != null)
                profileString = profileComboBox.getSelectedItem().toString();

            // if(profileString.isEmpty() && profileComboBox.getSelectedIndex() == -1 && profileComboBox.getComponentCount() > 0)
            // {
            //     profileString = profileComboBox.getItemAt(selectedIndex);
            // } else {
            //     selectedIndex = profileComboBox.getSelectedIndex();
            // }

            if (URProfilesUtil.getActiveProfileName() != profileString && !profileString.isEmpty())
            {
                // TODO Show dialog to either rename the existing profile, or create a new profile
                String currentProfile = URProfilesUtil.getActiveProfileName();

                if (!URProfilesUtil.profileExists(profileString))
                {
                    // If create new profile selected
                    profileComboBox.addItem(profileString);

                    // If rename existing profile selected...
                    // TODO
                }

                URProfilesUtil.setActiveProfileName(profileString);
            }
        }

    }

    @Override
    public void setEnabled (boolean enable)
    {
        profileComboBox.setEnabled(enable);
    }

    private void loadProfiles (String initialProfile)
    {
        profileComboBox.removeAllItems();

        profileComboBox.removeActionListener(changeListener);

        for (String profileName : URProfilesUtil.getProfiles()) {
            profileComboBox.addItem(profileName);
        }

        // Don't readd the listener if the gui hasn't been initialized

        if (URProfilesUtil.profileExists(initialProfile))
        {
            profileComboBox.setSelectedItem(initialProfile);

            if(DriverGUI.gui != null)
                profileComboBox.addActionListener(changeListener);
        } else
        {
            // TODO: Change this to a YesNoDialog to ask to create the profile
            SwingUtilities.invokeLater(new Runnable()
            {

                @Override
                public void run ()
                {
                    MessageDialog dialog = new MessageDialog("Initial Profile: [" + initialProfile + "] doesn't exist.", "Missing Profile", JOptionPane.ERROR_MESSAGE);

                    if(DriverGUI.frame.isShowing())
                        dialog.setVisible(true);
                }

            });

            Constants.LOGGER.error("Initial Profile: [" + initialProfile + "] doesn't exist.");
        }
    }
}
