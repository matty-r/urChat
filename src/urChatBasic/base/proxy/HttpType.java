package urChatBasic.base.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

public class HttpType implements ProxyTypeBase
{
    String name = "HTTP";
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
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostName, port));
        proxySocket = new Socket(proxy);

        return this;
    }

    @Override
    public Socket connectThroughProxy (InetSocketAddress endPoint) throws IOException
    {
        proxySocket.connect(endPoint, 5000);
        return proxySocket;
    }

}
