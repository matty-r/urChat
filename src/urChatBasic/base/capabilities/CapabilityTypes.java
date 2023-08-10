package urChatBasic.base.capabilities;

public enum CapabilityTypes {
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
}
