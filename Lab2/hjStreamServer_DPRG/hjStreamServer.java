/*
 * hjStreamServer.java 
 * Streaming server with DPRG-based stream cipher (XOR encryption)
 * Packet format: seqNum(4 bytes) || XOR-encrypted frame
 */

import java.io.*;
import java.net.*;

class hjStreamServer {

	// pre-shared 256-bit secret key
	static final byte[] SECRET_KEY = {
		0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
		0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
		0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
		0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20
	};
	static final byte[] SEED = "StreamCipherSeed2024".getBytes();

	static public void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out.println("Use: hjStreamServer <movie> <ip-address> <port>");
			System.exit(-1);
		}

		int size;
		int csize = 0;
		int count = 0;
		long time;
		DataInputStream g = new DataInputStream(new FileInputStream(args[0]));
		byte[] buff = new byte[4096];

		DatagramSocket s = new DatagramSocket();
		InetSocketAddress addr = new InetSocketAddress(args[1], Integer.parseInt(args[2]));
		long t0 = System.nanoTime();
		long q0 = 0;

		DPRG dprg = new DPRG(SECRET_KEY, SEED);
		int seqNum = 0;

		while (g.available() > 0) {
			size = g.readShort();
			csize += size;
			time = g.readLong();
			if (count == 0) q0 = time;
			count++;
			g.readFully(buff, 0, size);

			long t = System.nanoTime();
			Thread.sleep(Math.max(0, ((time - q0) - (t - t0)) / 1000000));

			// XOR frame with DPRG keystream
			byte[] encrypted = dprg.xorWithKeystream(buff, 0, size, seqNum);

			// prepend sequence number for synchronization
			byte[] payload = new byte[4 + encrypted.length];
			payload[0] = (byte) (seqNum >> 24);
			payload[1] = (byte) (seqNum >> 16);
			payload[2] = (byte) (seqNum >> 8);
			payload[3] = (byte) seqNum;
			System.arraycopy(encrypted, 0, payload, 4, encrypted.length);

			DatagramPacket p = new DatagramPacket(payload, payload.length, addr);
			s.send(p);
			seqNum++;
			System.out.print(":");
		}

		long tend = System.nanoTime();
		System.out.println();
		System.out.println("DONE! frames sent: " + count);
		long duration = (tend - t0) / 1000000000;
		System.out.println("Duration: " + duration + " s");
		System.out.println("Throughput: " + count / duration + " fps");
		System.out.println("Throughput: " + (8 * csize / duration) / 1000 + " Kbps");
	}
}
