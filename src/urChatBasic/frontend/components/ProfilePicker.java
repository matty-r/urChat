package urChatBasic.frontend.components;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.UserGUI;
import urChatBasic.frontend.dialogs.MessageDialog;
import urChatBasic.frontend.dialogs.YesNoDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class ProfilePicker extends JPanel
{
    private List<String> allProfiles;
    private JComboBox<String> profileComboBox;
    private final JButton saveProfile = new JButton("Save");
    private int selectedIndex = 0;

    public ProfilePicker (String initialProfile, Boolean showSaveButton)
    {
        profileComboBox = new JComboBox<String>();
        loadProfiles(initialProfile);
        profileComboBox.setEditable(true);

        // setBackground(Color.BLUE);
        setLayout(new GridLayout(1, 1));

        // Add components to the panel
        add(profileComboBox);

        if (showSaveButton)
        {
            setLayout(new GridLayout(2, 1));
            add(saveProfile);
        }

        profileComboBox.addActionListener(e -> {
            String profileString = profileComboBox.getSelectedItem().toString();
            boolean deleteProfile = false;
            // Did we have an item selected, but we just deleted the text?
            if(profileString.isEmpty() && profileComboBox.getSelectedIndex() == -1 && profileComboBox.getComponentCount() > 0)
            {
                deleteProfile = true;
                profileString = profileComboBox.getItemAt(selectedIndex);
            } else {
                selectedIndex = profileComboBox.getSelectedIndex();
            }

            if (DriverGUI.gui.getProfileName() != profileString && !deleteProfile)
            {
                // TODO Show dialog to either rename the existing profile, or create a new profile
                String currentProfile = DriverGUI.gui.getProfileName();

                if (!profileExists(profileString))
                {
                    // If create new profile selected
                    profileComboBox.addItem(profileString);

                    // If rename existing profile selected...
                    // TODO
                }

                DriverGUI.gui.setProfileName(profileString);
            } else if (deleteProfile)
            {

                // TODO Show a confirmation dialog
                if (profileExists(profileString))
                {
                    if(profileString.equals(UserGUI.getDefaultProfile()))
                    {
                        MessageDialog cantDelete = new MessageDialog("Can't delete the default profile. Select another profile as default and try again.", "Delete Profile", JOptionPane.INFORMATION_MESSAGE);
                        cantDelete.setVisible(true);
                    } else {
                        AtomicBoolean confirmDelete = new AtomicBoolean(false);

                        YesNoDialog deleteProfileDialog = new YesNoDialog("Delete the '" + profileString + "' profile?", "Delete Profile", JOptionPane.WARNING_MESSAGE,
                                dialog -> {
                                confirmDelete.set(dialog.getActionCommand().equalsIgnoreCase("Yes"));
                            });

                        deleteProfileDialog.setVisible(true);

                        if(confirmDelete.get())
                        {
                            profileComboBox.removeItemAt(selectedIndex);
                            DriverGUI.gui.deleteProfile(profileString);

                            profileComboBox.setSelectedIndex(getProfileIndex(UserGUI.getDefaultProfile()));
                            DriverGUI.gui.setProfileName(profileComboBox.getSelectedItem().toString());
                        }
                    }
                }
            }
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

    private int getProfileIndex (String profileName)
    {
        if (profileExists(profileName))
        {
            for (int i = 0; i < profileComboBox.getItemCount(); i++)
            {
                if (profileComboBox.getItemAt(i).toString().equalsIgnoreCase(profileName))
                {
                    return i;
                }
            }
        }

        return -1;
    }

    public boolean profileExists (String profileName)
    {
        for (int i = 0; i < profileComboBox.getItemCount(); i++)
        {
            if (profileComboBox.getItemAt(i).toString().equalsIgnoreCase(profileName))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void setEnabled (boolean enable)
    {
        profileComboBox.setEnabled(enable);
    }

    private void loadProfiles (String initialProfile)
    {
        profileComboBox = new JComboBox<>(URProfilesUtil.getProfiles());

        if (profileExists(initialProfile))
        {
            profileComboBox.setSelectedItem(initialProfile);
            selectedIndex = profileComboBox.getSelectedIndex();
        } else
        {
            // TODO: Change this to a YesNoDialog to ask to create the profile
            SwingUtilities.invokeLater(new Runnable()
            {

                @Override
                public void run ()
                {
                    MessageDialog dialog = new MessageDialog("Initial Profile: [" + initialProfile + "] doesn't exist.", "Missing Profile", JOptionPane.ERROR_MESSAGE);
                    dialog.setVisible(!DriverGUI.isTesting);
                }

            });

            Constants.LOGGER.log(Level.WARNING, "Initial Profile: [" + initialProfile + "] doesn't exist.");
        }
    }
}
