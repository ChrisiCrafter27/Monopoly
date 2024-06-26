package monopol.common.utils;

import java.net.*;
import java.util.*;

public class ServerProperties {
    private ServerSettings serverSettings;
    private int port1;
    private int port2;
    private Inet4Address ip;

    public ServerProperties() {
        this(new ServerSettings(true, false), 25565, 1199, ip(Objects.requireNonNull(defaultNetworkInterface())));
    }

    public ServerProperties(ServerSettings serverSettings, int port1, int port2, Inet4Address ip) {
        this.serverSettings = serverSettings;
        this.port1 = port1;
        this.port2 = port2;
        this.ip = ip;
        System.setProperty("java.rmi.server.hostname", this.ip.getHostName());
    }

    public ServerSettings getServerSettings() {
        return serverSettings;
    }

    public int getMainPort() {
        return port1;
    }

    public int getUnicastPort() {
        return port2;
    }

    public Inet4Address getLocalIp() {
        return ip;
    }

    public void set(ServerSettings serverSettings, int port1, int port2, String ip) {
        this.serverSettings = serverSettings;
        this.port1 = port1;
        this.port2 = port2;
        this.ip = Optional.ofNullable(ip(networkInterfaces().get(ip))).orElse(this.ip);
        System.setProperty("java.rmi.server.hostname", this.ip.getHostName());
    }

    public String currentAdapterName() {
        for(Map.Entry<String, NetworkInterface> entry : networkInterfaces().entrySet()) {
            if(ip.equals(ip(entry.getValue()))) return entry.getKey();
        }
        return null;
    }

    public static Inet4Address ip(NetworkInterface networkInterface) {
        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress addr = addresses.nextElement();
            if (addr instanceof Inet4Address) {
                return (Inet4Address) addr;
            }
        }
        return null;
    }

    public static Map<String, NetworkInterface> networkInterfaces() {
        Map<String, NetworkInterface> networkInterfaces = new HashMap<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;
                boolean hasValidInetAddress = false;
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && !inetAddress.isAnyLocalAddress() && inetAddress instanceof Inet4Address) {
                        hasValidInetAddress = true;
                        break;
                    }
                }
                if(hasValidInetAddress) networkInterfaces.put(networkInterface.getDisplayName(), networkInterface);
            }
        } catch (SocketException ignored) {}
        return networkInterfaces;
    }

    public static NetworkInterface defaultNetworkInterface() {
        try {
            List<NetworkInterface> networkInterfaces = networkInterfaces().values().stream().toList();
            return networkInterfaces.size() > 0 ? networkInterfaces.get(0) : NetworkInterface.getByInetAddress(InetAddress.getByName("localhost"));
        } catch (SocketException | UnknownHostException ignored) {
            return null;
        }
    }
}
