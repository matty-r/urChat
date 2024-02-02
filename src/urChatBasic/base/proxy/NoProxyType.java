package urChatBasic.base.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NoProxyType implements ProxyTypeBase {
    String name = "None";
    Socket proxySocket;

    @Override
    public String getName ()
    {
        return name;
    }

    @Override
    public String toString ()
    {
        return getName();
    }


    @Override
    public ProxyTypeBase createProxy (String hostName, int port)
    {
        return this;
    }

    @Override
    public Socket connectThroughProxy (InetSocketAddress endPoint) throws IOException
    {
        throw new IOException("No Proxy Needed");
    }

}
