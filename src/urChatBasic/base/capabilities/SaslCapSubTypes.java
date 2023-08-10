package urChatBasic.base.capabilities;

import java.util.ArrayList;

public enum SaslCapSubTypes implements CapTypeBase {
    PLAIN("PLAIN"),
    EXTERNAL("EXTERNAL"),
    SCRAM("ECDSA-NIST256P-CHALLENGE")
    ;

    String name;
    ArrayList<CapTypeBase> enabledSubTypes;

    SaslCapSubTypes(String name)
    {
        this.name = name;
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
}
