package urChatBasic.base.capabilities;

import java.util.ArrayList;
import java.util.Arrays;

public enum CapabilityTypes {
    NONE(new NoAuthType()),
    NICKSERV(new NickServType()),
    SASL(new SaslCapType());

    CapTypeBase type;

    CapabilityTypes(CapTypeBase baseType)
    {
        this.type = baseType;
    };

    public CapTypeBase getType()
    {
        return type;
    }

    public static CapTypeBase[] getCategory(CapTypeBase.Category category)
    {
        ArrayList<CapTypeBase> allSubTypes = new ArrayList<CapTypeBase>();
        for (CapabilityTypes capabilityType : CapabilityTypes.values()) {
            if(capabilityType.getType().getCategory().equals(category))
            {
                allSubTypes.addAll(Arrays.asList(capabilityType.getType().availableSubTypes()));
            }
        }
        CapTypeBase[] allSubTypesArray = new CapTypeBase[allSubTypes.size()];
        allSubTypes.toArray(allSubTypesArray);
        return allSubTypesArray;
    }

    public static CapTypeBase getCapType(String capTypeName)
    {
        ArrayList<CapTypeBase> allSubTypes = new ArrayList<CapTypeBase>();
        for (CapabilityTypes capabilityType : CapabilityTypes.values()) {
            allSubTypes.addAll(Arrays.asList(capabilityType.getType().availableSubTypes()));
        }
        CapTypeBase[] allSubTypesArray = new CapTypeBase[allSubTypes.size()];
        allSubTypes.toArray(allSubTypesArray);

        for (CapTypeBase capType : allSubTypes) {
            if(capType.toString().equals(capTypeName))
            {
                return capType;
            }
        }

        return null;
    }
}
