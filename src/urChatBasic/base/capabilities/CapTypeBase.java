package urChatBasic.base.capabilities;

public interface CapTypeBase {
    enum Category {
        AUTHENTICATION
    }

    CapTypeBase[] getSubTypes();
    String getName();
    boolean matches (String typeName);
    void addSubtype (CapTypeBase subType);
    CapTypeBase[] availableSubTypes();
    Category getCategory();
}