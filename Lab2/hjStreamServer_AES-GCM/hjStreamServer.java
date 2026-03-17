/*
 * hjStreamServer.java 
 * Streaming server: streams encoded video frames in UDP packets
 * with AES-GCM authenticated encryption
 */

import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class hjStreamServer {

	// shared 128-bit AES key (pre-shared with the proxy)
	static final byte[] KEY_BYTES = {
		0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
		0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10
	};

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

		SecretKeySpec key = new SecretKeySpec(KEY_BYTES, "AES");
		SecureRandom random = new SecureRandom();

		while (g.available() > 0) {
			size = g.readShort();
			csize += size;
			time = g.readLong();
			if (count == 0) q0 = time;
			count++;
			g.readFully(buff, 0, size);

			long t = System.nanoTime();
			Thread.sleep(Math.max(0, ((time - q0) - (t - t0)) / 1000000));

			// encrypt frame with AES-GCM (fresh IV per frame)
			byte[] iv = new byte[12];
			random.nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
			byte[] encrypted = cipher.doFinal(buff, 0, size);

			// packet = IV(12) || ciphertext+tag
			byte[] payload = new byte[12 + encrypted.length];
			System.arraycopy(iv, 0, payload, 0, 12);
			System.arraycopy(encrypted, 0, payload, 12, encrypted.length);

			DatagramPacket p = new DatagramPacket(payload, payload.length, addr);
			s.send(p);
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
