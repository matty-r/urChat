package urChatBasic.base.capabilities;

import java.util.ArrayList;

public class SaslCapType implements CapTypeBase {
    String name;
    ArrayList<CapTypeBase> enabledSubTypes = new ArrayList<CapTypeBase>();

    SaslCapType()
    {
        this.name = "SASL";
    }

    public String getName ()
    {
        return name;
    }

    @Override
    public ArrayList<CapTypeBase> getSubTypes()
    {
        return enabledSubTypes;
    }

    @Override
    public CapTypeBase[] availableSubTypes()
    {
        return SaslCapSubTypes.values();
    }

    @Override
    public void addSubtype(CapTypeBase subType)
    {
        enabledSubTypes.add(subType);
    }

    @Override
    public boolean matches(String typeName)
    {
        if(this.name.equalsIgnoreCase(typeName))
        {
            return true;
        }

        return false;
    }
}
