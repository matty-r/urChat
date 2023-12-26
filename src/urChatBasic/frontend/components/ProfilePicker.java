package urChatBasic.frontend.components;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import urChatBasic.base.Constants;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.dialogs.MessageDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProfilePicker extends JPanel
{
    private JComboBox<String> profileComboBox = new JComboBox<String>();
    private JLabel selectProfileLabel = new JLabel("Select Profile:");
    private final JButton saveProfile = new JButton("Save");

    public ProfilePicker (JPanel parentPanel, String initialProfile)
    {
        loadProfiles(initialProfile);

        profileComboBox.setEditable(true);
        setBackground(parentPanel.getBackground());
        setLayout(new GridLayout(3, 1));

        // Add components to the panel
        add(selectProfileLabel);
        add(profileComboBox);
        add(saveProfile);

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

    private String[] getProfiles ()
    {
        ArrayList<String> currentProfiles = new ArrayList<String>();

        for (int i = 0; i < profileComboBox.getItemCount(); i++)
        {
            currentProfiles.add(profileComboBox.getItemAt(i).toString());
        }

        return currentProfiles.toArray(String[]::new);
    }

    private void loadProfiles (String initialProfile)
    {
        try
        {
            if (Constants.BASE_PREFS.childrenNames().length == 0)
            {
                profileComboBox = new JComboBox<>(new String[] {Constants.DEFAULT_PROFILE_NAME});
                Constants.BASE_PREFS.put(Constants.KEY_DEFAULT_PROFILE_NAME, Constants.DEFAULT_PROFILE_NAME);
            }
            // Collect them into a list
            List<String> allProfiles =
                    Stream.concat(Arrays.stream(getProfiles()), Arrays.stream(Constants.BASE_PREFS.childrenNames()))
                            .collect(Collectors.toList());

            // Use a Set, then convert to Array to drop any duplicates
            String[] profileNames = (new HashSet<>(allProfiles)).toArray(String[]::new);
            profileComboBox = new JComboBox<>(profileNames);

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
                        MessageDialog dialog =
                                new MessageDialog("Initial Profile: [" + initialProfile + "] doesn't exist.",
                                        "Missing Profile", JOptionPane.ERROR_MESSAGE);
                        dialog.setVisible(!DriverGUI.isTesting);
                    }

                });

                Constants.LOGGER.log(Level.WARNING, "Initial Profile: [" + initialProfile + "] doesn't exist.");
            }
        } catch (BackingStoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
