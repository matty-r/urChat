package urChatBasic.frontend.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants.EventType;
import urChatBasic.base.Constants.Placement;
import urChatBasic.base.Constants.Size;
import urChatBasic.frontend.UserGUI;
import urChatBasic.frontend.dialogs.MessageDialog;
import urChatBasic.frontend.dialogs.YesNoDialog;
import urChatBasic.frontend.panels.UROptionsPanel;
import urChatBasic.frontend.utils.Panels;

public class ProfilePanel extends UROptionsPanel
{
    public static final String PANEL_DISPLAY_NAME = "Profiles";
    private static final DefaultTableModel profilesTableModel = new DefaultTableModel(new Object[] {"Profiles"}, 0);
    private static final JTable profilesTable = new JTable(profilesTableModel);
    protected JScrollPane profileScroller = new JScrollPane(profilesTable);
    private JButton cloneProfile = new JButton("Clone");
    private JButton createProfile = new JButton("Create");
    private JLabel profileName = new JLabel(URProfilesUtil.getDefaultProfile());
    private JCheckBox setAsDefault = new JCheckBox("", true);

    public ProfilePanel (MainOptionsPanel optionsPanel)
    {
        super(PANEL_DISPLAY_NAME, optionsPanel);

        loadProfiles();
        profilesTable.setTableHeader(null);
        profilesTable.setShowGrid(false);

        URProfilesUtil.addListener(EventType.DELETE, e -> {
            loadProfiles();
        });

        URProfilesUtil.addListener(EventType.CREATE, e -> {
            loadProfiles();
        });

        Panels.addToPanel(this, profileScroller, "Available Profiles", Placement.DEFAULT, Size.CUSTOM.customSize(200, 200));
        Panels.addToPanel(this, cloneProfile, null, Placement.DEFAULT, null);
        Panels.addToPanel(this, createProfile, null, Placement.RIGHT, null);

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

        ListSelectionModel listSelectionModel = profilesTable.getSelectionModel();
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionModel.addListSelectionListener(new ProfilesListSelectionHandler());

        Panels.addToPanel(this, setAsDefault, "Set as default", Placement.DEFAULT, null);

        // Add KeyListener to the table for key events
        profilesTable.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed (KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                {
                    int selectedRow = profilesTable.getSelectedRow();
                    if (selectedRow >= 0)
                    {
                        String profileString = profilesTableModel.getValueAt(selectedRow, 0).toString();
                        if (URProfilesUtil.profileExists(profileString))
                        {
                            if (profileString.equals(URProfilesUtil.getDefaultProfile()))
                            {
                                MessageDialog cantDelete =
                                        new MessageDialog("Can't delete the default profile. Select another profile as default and try again.",
                                                "Delete Profile", JOptionPane.INFORMATION_MESSAGE);
                                cantDelete.setVisible(true);
                            } else
                            {
                                AtomicBoolean confirmDelete = new AtomicBoolean(false);

                                YesNoDialog deleteProfileDialog = new YesNoDialog("Delete the '" + profileString + "' profile?", "Delete Profile",
                                        JOptionPane.WARNING_MESSAGE, dialog -> {
                                            confirmDelete.set(dialog.getActionCommand().equalsIgnoreCase("Yes"));
                                        });

                                deleteProfileDialog.setVisible(true);

                                if (confirmDelete.get())
                                {
                                    profilesTableModel.removeRow(selectedRow);
                                    URProfilesUtil.deleteProfile(profileString);

                                    int profileIndex = getProfileIndex(URProfilesUtil.getDefaultProfile());
                                    profilesTable.setRowSelectionInterval(profileIndex, profileIndex);
                                }
                            }
                        }
                    }
                }
            }
        });

        profilesTableModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged (TableModelEvent e)
            {
                int selectedRow = profilesTable.getSelectedRow();

                if (selectedRow >= 0)
                {
                    if (e.getType() == TableModelEvent.UPDATE) {
                        String oldValue = profileName.getText();
                        String newValue = profilesTableModel.getValueAt(selectedRow, 0).toString();

                        if(!oldValue.equals(newValue))
                        {
                            URProfilesUtil.cloneProfile(oldValue, Optional.of(newValue));
                            URProfilesUtil.deleteProfile(oldValue);
                        }
                        // Add your handling code here for the cell change event
                    }
                }
            }
        });

        UserGUI.addProfileChangeListener(e -> {
            String selectedProfileName = URProfilesUtil.getActiveProfileName();

            int profileIndex = getProfileIndex(selectedProfileName);
            profilesTable.setRowSelectionInterval(profileIndex, profileIndex);
        });

        cloneProfile.addActionListener(e -> {
            int selectedRow = profilesTable.getSelectedRow();

            if (selectedRow >= 0)
            {
                String profileString = profilesTableModel.getValueAt(selectedRow, 0).toString();
                URProfilesUtil.cloneProfile(profileString, Optional.empty());
            }
        });

        createProfile.addActionListener(e -> {
            URProfilesUtil.createProfile("New Profile");
        });
    }

    private void loadProfiles ()
    {
        // delete all the loaded profiles
        while (profilesTableModel.getRowCount() > 0)
        {
            profilesTableModel.removeRow(profilesTableModel.getRowCount() - 1);
        }

        // add them all back in
        for (String profile : URProfilesUtil.getProfiles())
        {
            profilesTableModel.addRow(new Object[] {profile});
        }
    }

    private int getProfileIndex (String profileName)
    {
        if (URProfilesUtil.profileExists(profileName))
        {
            for (int i = 0; i < profilesTableModel.getRowCount(); i++)
            {
                if (profilesTableModel.getValueAt(i, 0).toString().equals(profileName))
                {
                    return i;
                }
            }
        }

        return -1;
    }



    class ProfilesListSelectionHandler implements ListSelectionListener
    {
        public void valueChanged (ListSelectionEvent e)
        {
            if (!e.getValueIsAdjusting())
            {
                int selectedRow = profilesTable.getSelectedRow();
                if (selectedRow >= 0)
                {
                    profileName.setText(profilesTableModel.getValueAt(selectedRow, 0).toString());
                    setDefaultCheckboxState();
                }
            }
        }
    }

    /**
     * If a profile is set to be the default, don't let the checkbox be unchecked.
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
