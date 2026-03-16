/*
 * hjUDPproxy.java
 * UDP proxy with DPRG-based stream cipher decryption
 * Reads seqNum from each packet, regenerates the keystream, XORs to decrypt
 */

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

class hjUDPproxy {

    // same pre-shared key and seed as the server
    static final byte[] SECRET_KEY = {
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
        0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
        0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20
    };
    static final byte[] SEED = "StreamCipherSeed2024".getBytes();

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

        DPRG dprg = new DPRG(SECRET_KEY, SEED);

        while (true) {
            DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
            inSocket.receive(inPacket);

            int off = inPacket.getOffset();
            int len = inPacket.getLength();

            // extract sequence number (first 4 bytes)
            int seqNum = ((buffer[off] & 0xFF) << 24)
                       | ((buffer[off + 1] & 0xFF) << 16)
                       | ((buffer[off + 2] & 0xFF) << 8)
                       | (buffer[off + 3] & 0xFF);

            // decrypt with the same keystream
            byte[] decrypted = dprg.xorWithKeystream(buffer, off + 4, len - 4, seqNum);

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
