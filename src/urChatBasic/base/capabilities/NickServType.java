package urChatBasic.base.capabilities;

import java.util.ArrayList;

public class NickServType implements CapTypeBase {
    String name;
    ArrayList<CapTypeBase> enabledSubTypes = new ArrayList<CapTypeBase>();
    CapTypeBase.Category category = CapTypeBase.Category.AUTHENTICATION;

    NickServType()
    {
        this.name = "NickServ";
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
        CapTypeBase[] enabledSubTypesArray = new CapTypeBase[1];
        enabledSubTypesArray[0] = this;
        return enabledSubTypesArray;
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
