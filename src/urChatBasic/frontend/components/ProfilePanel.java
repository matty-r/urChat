package urChatBasic.frontend.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import urChatBasic.base.Constants.Placement;
import urChatBasic.base.Constants.Size;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.UserGUI;
import urChatBasic.frontend.panels.UROptionsPanel;
import urChatBasic.frontend.utils.Panels;

public class ProfilePanel extends UROptionsPanel
{
    public static final String PANEL_DISPLAY_NAME = "Profiles";
    private JTextField profileName = new JTextField(UserGUI.getDefaultProfile());
    private JCheckBox setAsDefault = new JCheckBox("", true);

    public ProfilePanel (MainOptionsPanel optionsPanel)
    {
        super(PANEL_DISPLAY_NAME, optionsPanel);

        profileName.setEnabled(false);
        Panels.addToPanel(this, profileName, "Current Profile", Placement.DEFAULT, Size.SMALL);

        setAsDefault.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent arg0)
            {

                UserGUI.setDefaultProfile(profileName.getText());
                setDefaultCheckboxState();
            }
        });

        Panels.addToPanel(this, setAsDefault, "Set as default", Placement.DEFAULT, null);

        UserGUI.addProfileChangeListener(e -> {
            profileName.setText(DriverGUI.gui.getProfileName());
            setDefaultCheckboxState();
        });
    }

    private void setDefaultCheckboxState ()
    {
        if (UserGUI.getDefaultProfile().equals(profileName.getText()))
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
