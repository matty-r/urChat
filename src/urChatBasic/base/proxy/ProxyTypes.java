package urChatBasic.base.proxy;

import java.util.Arrays;
import java.util.List;
public enum ProxyTypes
{
    NONE(new NoProxyType()), SOCKS(new SocksType()), HTTP(new HttpType());

    ProxyTypeBase type;

    ProxyTypes (ProxyTypeBase baseType)
    {
        this.type = baseType;
    };

    public ProxyTypeBase getType ()
    {
        return type;
    }

    @Override
    public String toString ()
    {
        return type.getName();
    }

    public static ProxyTypeBase getProxyType (String proxyTypeName)
    {
        List<ProxyTypeBase> allProxyTypes = Arrays.asList(getValues());

        for (ProxyTypeBase proxyType : allProxyTypes)
        {
            if (proxyType.toString().equals(proxyTypeName))
            {
                return proxyType;
            }
        }

        return null;
    }

    public static ProxyTypeBase[] getValues ()
    {
        return Arrays.stream(ProxyTypes.values())
            .map(ProxyTypes::getType)
            .toArray(ProxyTypeBase[]::new);
    }
}
