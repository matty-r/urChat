package urChatBasic.frontend.components;

import java.util.Arrays;
import javax.swing.JComboBox;
import urChatBasic.base.proxy.ProxyTypeBase;
import urChatBasic.base.proxy.ProxyTypes;

public class URProxyTypeComboBox extends JComboBox<ProxyTypeBase>
{
    public URProxyTypeComboBox ()
    {
        initialize();
    }

    private void initialize ()
    {
        ProxyTypeBase[] proxyTypes = ProxyTypes.getValues();

        if (proxyTypes != null)
        {
            Arrays.stream(proxyTypes).forEach(this::addItem);
        }
    }

    public void runChangeListener ()
    {
        // ProxyTypeBase selectedAuthType = getSelectedItem();

        // do nothing
    }

    public ProxyTypeBase getSelectedItem ()
    {
        return (ProxyTypeBase) super.getSelectedItem();
    }

    @Override
    public void setSelectedItem (Object newObject)
    {
        ProxyTypeBase newTypeBase = null;

        if (newObject instanceof ProxyTypeBase)
            newTypeBase = (ProxyTypeBase) newObject;
        else
            newTypeBase = ProxyTypes.getProxyType((String) newObject);


        super.setSelectedItem(newTypeBase);
    }
}
