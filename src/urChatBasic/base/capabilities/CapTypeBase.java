package urChatBasic.base.capabilities;

import java.util.ArrayList;

public interface CapTypeBase {
    ArrayList<CapTypeBase> getSubTypes();
    String getName();

    boolean matches (String typeName);
    void addSubtype (CapTypeBase subType);
    CapTypeBase[] availableSubTypes();
}