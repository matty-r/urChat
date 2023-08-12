package urChatBasic.base.capabilities;

import java.util.ArrayList;

public enum SaslCapSubTypes implements CapTypeBase {
    PLAIN("PLAIN"),
    // EXTERNAL("EXTERNAL"), -- TODO: Needs to be implemented
    // SCRAM("ECDSA-NIST256P-CHALLENGE") -- TODO: Needs to be implemented
    ;

    String name;
    ArrayList<CapTypeBase> enabledSubTypes;
    CapTypeBase.Category category = CapTypeBase.Category.AUTHENTICATION;

    SaslCapSubTypes(String name)
    {
        this.name = name;
    }

    public String getName ()
    {
        return name;
    }

    @Override
    public String toString ()
    {
        return "SASL-"+super.toString();
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
        return null;
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
