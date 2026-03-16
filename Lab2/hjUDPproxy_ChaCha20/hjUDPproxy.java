/*
 * hjUDPproxy.java
 * UDP proxy with ChaCha20-Poly1305 decryption
 */

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class hjUDPproxy {

    // same pre-shared 256-bit key as the server
    static final byte[] KEY_BYTES = {
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
        0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
        0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20
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

        SecretKeySpec key = new SecretKeySpec(KEY_BYTES, "ChaCha20");

        while (true) {
            DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
            inSocket.receive(inPacket);

            int off = inPacket.getOffset();
            int len = inPacket.getLength();

            byte[] nonce = Arrays.copyOfRange(buffer, off, off + 12);
            byte[] ciphertext = Arrays.copyOfRange(buffer, off + 12, off + len);

            Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(nonce));
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
