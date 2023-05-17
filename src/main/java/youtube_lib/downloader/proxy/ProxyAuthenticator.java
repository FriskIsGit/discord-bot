package youtube_lib.downloader.proxy;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyAuthenticator extends Authenticator {

    private static volatile ProxyAuthenticator instance;

    private final Map<String, PasswordAuthentication> credentials;

    public ProxyAuthenticator(Map<String, PasswordAuthentication> credentials) {
        this.credentials = credentials;
    }
    public ProxyAuthenticator() {
        this.credentials = new ConcurrentHashMap<>();
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        String key = getRequestingHost() + ":" + getRequestingPort();
        return credentials.get(key);
    }

    public static synchronized void setDefault(ProxyAuthenticator authenticator) {
        instance = authenticator;
        Authenticator.setDefault(instance);
    }

    public static synchronized ProxyAuthenticator getDefault() {
        return instance;
    }

    public static void addAuthentication(String host, int port, String userName, String password) {
        if (instance == null) {
            throw new NullPointerException("ProxyAuthenticator instance is null. Use ProxyAuthenticator.setDefault() to init");
        }
        instance.credentials.put(host + ":" + port, new PasswordAuthentication(userName, password.toCharArray()));
    }

}
