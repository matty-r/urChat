package urChatBasic.frontend.components;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import urChatBasic.base.Constants;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.dialogs.MessageDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;

public class ProfilePicker extends JPanel
{
    private List<String> allProfiles;
    private JComboBox<String> profileComboBox;
    private final JButton saveProfile = new JButton("Save");

    public ProfilePicker (String initialProfile, Boolean showSaveButton)
    {
        if (profileComboBox == null)
        {
            profileComboBox = new JComboBox<String>();
            loadProfiles(initialProfile);
            profileComboBox.setEditable(true);
        }

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
            String selectedString = profileComboBox.getSelectedItem().toString();
            if (DriverGUI.gui.getProfileName() != selectedString && !selectedString.isBlank())
            {
                // TODO Show dialog to either rename the existing profile, or create a new profile
                String currentProfile = DriverGUI.gui.getProfileName();

                if (!profileExists(selectedString))
                {
                    // If create new profile selected
                    profileComboBox.addItem(selectedString);

                    // If rename existing profile selected...
                    // TODO
                }

                DriverGUI.gui.setProfileName(selectedString);
            } else if (selectedString.isBlank())
            {

                // TODO Show a confirmation dialog
                if (profileExists(DriverGUI.gui.getProfileName()))
                {
                    profileComboBox.removeItemAt(getProfileIndex(DriverGUI.gui.getProfileName()));
                    DriverGUI.gui.deleteProfile();

                    profileComboBox.setSelectedIndex(0);
                    DriverGUI.gui.setProfileName(profileComboBox.getSelectedItem().toString());
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

    /**
     * Retrieves all profiles that have been created and returns a String[] of the names. If no profiles are available, it creates the "Default" profile and sets it as the
     * default.
     * @return
     */
    public String[] getProfiles ()
    {
        try
        {
            if (Constants.BASE_PREFS.childrenNames().length == 0)
            {
                profileComboBox = new JComboBox<>(new String[] {Constants.DEFAULT_PROFILE_NAME});
                Constants.BASE_PREFS.put(Constants.KEY_DEFAULT_PROFILE_NAME, Constants.DEFAULT_PROFILE_NAME);
                allProfiles = Arrays.asList(new String[] {Constants.DEFAULT_PROFILE_NAME});
            } else {
                allProfiles = Arrays.stream(Constants.BASE_PREFS.childrenNames()).collect(Collectors.toList());
            }
        } catch (BackingStoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Use a Set, then convert to Array to drop any duplicates
        String[] profileNames = (new HashSet<>(allProfiles)).toArray(String[]::new);

        return profileNames;
    }

    private void loadProfiles (String initialProfile)
    {
        profileComboBox = new JComboBox<>(getProfiles());

        if (profileExists(initialProfile))
        {
            profileComboBox.setSelectedItem(initialProfile);
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
