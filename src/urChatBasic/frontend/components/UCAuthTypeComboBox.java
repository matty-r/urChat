package urChatBasic.frontend.components;

import java.util.Arrays;
import javax.swing.JComboBox;
import urChatBasic.base.capabilities.CapTypeBase;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.base.capabilities.NickServType;
import urChatBasic.base.capabilities.SaslCapSubTypes;

public class UCAuthTypeComboBox extends JComboBox<CapTypeBase>
{
    private String passwordFieldName = "Password";

    public UCAuthTypeComboBox()
    {
        initialize();
    }

    private void initialize()
    {
        CapTypeBase[] authTypes = CapabilityTypes.getCategory(CapTypeBase.Category.AUTHENTICATION);

        if (authTypes != null)
        {
            Arrays.stream(authTypes).forEach(this::addItem);
        }
    }

    public String getPasswordFieldName()
    {
        return passwordFieldName;
    }

    private void setPasswordFieldName(String passwordFieldName)
    {
        this.passwordFieldName = passwordFieldName;
    }

    public void runChangeListener()
    {
        CapTypeBase selectedAuthType = (CapTypeBase) getSelectedItem();

        // Set the password field name based on the selected authentication type
        if (selectedAuthType.equals(SaslCapSubTypes.PLAIN) || selectedAuthType.getClass().equals(NickServType.class))
        {
            setPasswordFieldName("Password");
        } else
        {
            setPasswordFieldName("Path to certificate"); // No matching authentication type
        }
    }
}
