package Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * @author: mingzhangyong
 * @create: 2021-09-02 16:20
 **/
public class HttpsDownLoad {
    public static void main(String[] args)  {
        SSLContext context = null;
        try {
            context = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            context.init(null, null, null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        SSLSocketFactory factory = (SSLSocketFactory) context.getSocketFactory();
        SSLSocket socket = null;
        try {
            socket = (SSLSocket) factory.createSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] protocols = socket.getSupportedProtocols();

        System.out.println("Supported Protocols: " + protocols.length);
        for (int i = 0; i < protocols.length; i++) {
            System.out.println(" " + protocols[i]);
        }

        protocols = socket.getEnabledProtocols();

        System.out.println("Enabled Protocols: " + protocols.length);
        for (int i = 0; i < protocols.length; i++) {
            System.out.println(" " + protocols[i]);
        }

    }

}
