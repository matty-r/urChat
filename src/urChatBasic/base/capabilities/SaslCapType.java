package urChatBasic.base.capabilities;

import java.util.ArrayList;

public class SaslCapType implements CapTypeBase {
    String name;
    ArrayList<CapTypeBase> enabledSubTypes = new ArrayList<CapTypeBase>();
    CapTypeBase.Category category = CapTypeBase.Category.AUTHENTICATION;

    SaslCapType()
    {
        this.name = "SASL";
    }

    public String getName ()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public CapTypeBase[] getSubTypes()
    {
        CapTypeBase[] enabledSubTypesArray = new CapTypeBase[enabledSubTypes.size()];
        enabledSubTypes.toArray(enabledSubTypesArray);
        return enabledSubTypesArray;
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

    @Override
    public Category getCategory()
    {
        return category;
    }
}
