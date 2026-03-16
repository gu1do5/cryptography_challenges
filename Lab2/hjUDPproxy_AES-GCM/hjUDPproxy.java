/*
 * hjUDPproxy.java
 * UDP proxy: receives AES-GCM encrypted packets from the streaming server,
 * decrypts them, and forwards cleartext frames to the media player
 */

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class hjUDPproxy {

    // same pre-shared key as the server
    static final byte[] KEY_BYTES = {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10
    };

    public static void main(String[] args) throws Exception {
        InputStream inputStream = new FileInputStream("config.properties");
        Properties properties = new Properties();
        properties.load(inputStream);
        String remote = properties.getProperty("remote");
        String destinations = properties.getProperty("localdelivery");

        SocketAddress inSocketAddress = parseSocketAddress(remote);
        Set<SocketAddress> outSocketAddressSet = Arrays.stream(destinations.split(","))
                .map(s -> parseSocketAddress(s)).collect(Collectors.toSet());

        DatagramSocket inSocket = new DatagramSocket(inSocketAddress);
        DatagramSocket outSocket = new DatagramSocket();
        byte[] buffer = new byte[4 * 1024];

        SecretKeySpec key = new SecretKeySpec(KEY_BYTES, "AES");

        while (true) {
            DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
            inSocket.receive(inPacket);

            int off = inPacket.getOffset();
            int len = inPacket.getLength();

            // extract IV and ciphertext from the packet
            byte[] iv = Arrays.copyOfRange(buffer, off, off + 12);
            byte[] ciphertext = Arrays.copyOfRange(buffer, off + 12, off + len);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] decrypted = cipher.doFinal(ciphertext);

            System.out.print(".");
            for (SocketAddress outAddr : outSocketAddressSet) {
                outSocket.send(new DatagramPacket(decrypted, decrypted.length, outAddr));
            }
        }
    }

    private static InetSocketAddress parseSocketAddress(String socketAddress) {
        String[] split = socketAddress.split(":");
        return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
    }
}
