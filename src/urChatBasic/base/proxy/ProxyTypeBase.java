package urChatBasic.base.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public interface ProxyTypeBase
{


    /**
     * Returns the name of the type of proxy.
     * @return
     */
    public abstract String getName();

    /**
     * Returns the newly created Proxy Socket
     * @param hostName
     * @param port
     * @return
     */
    public abstract ProxyTypeBase createProxy (String hostName, int port);

    /**
     * Endpoint to connect to through the proxy
     * @param endPoint
     */
    public abstract Socket connectThroughProxy (InetSocketAddress endPoint) throws IOException;
}
