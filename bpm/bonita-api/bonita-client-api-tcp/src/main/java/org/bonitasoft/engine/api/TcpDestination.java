package org.bonitasoft.engine.api;

public class TcpDestination {

    private final String host;
    private final int port;
    
    public TcpDestination(final String host, final int port) {
        this.host = host;
        this.port = port;
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "TcpDestination [host=" + host + ", port=" + port + "]";
    }
    
}
