package urChatBasic.frontend.components;

import javax.swing.JButton;
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
    private JTextField profileName = new JTextField("Default profile");
    private JCheckBox setAsDefault = new JCheckBox("", true);
    private JButton createNewProfile = new JButton("Create new");
    private JButton saveProfile = new JButton("Save");

    public ProfilePanel (MainOptionsPanel optionsPanel)
    {
        super(PANEL_DISPLAY_NAME, optionsPanel);

        Panels.addToPanel(this, profileName, "Profile Name", Placement.DEFAULT, Size.SMALL);


        Panels.addToPanel(this, saveProfile, null, Placement.RIGHT, null);
        Panels.addToPanel(this, createNewProfile, null, Placement.RIGHT, null);

        setAsDefault.setEnabled(false);
        Panels.addToPanel(this, setAsDefault, "Use as default", Placement.DEFAULT, null);

        UserGUI.addProfileChangeListener(e -> {
            profileName.setText(DriverGUI.gui.getProfileName());
        });
    }
}
