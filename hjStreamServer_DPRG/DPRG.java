/*
 * DPRG - Deterministic Pseudorandom Generator
 * Uses HMAC-SHA256 as a PRF to produce a reproducible keystream.
 * Given a key, seed, and frame sequence number, generates a deterministic
 * keystream used for XOR encryption/decryption.
 */

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

public class DPRG {

    private final SecretKeySpec hmacKey;
    private final byte[] seed;

    public DPRG(byte[] key, byte[] seed) {
        this.hmacKey = new SecretKeySpec(key, "HmacSHA256");
        this.seed = seed;
    }

    // generates keystream bytes for a given frame sequence number
    public byte[] generateKeystream(int seqNum, int length) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(hmacKey);

        byte[] keystream = new byte[length];
        int offset = 0;
        int blockIndex = 0;

        // each HMAC call gives 32 bytes; chain with incrementing blockIndex
        while (offset < length) {
            ByteBuffer input = ByteBuffer.allocate(seed.length + 4 + 4);
            input.put(seed);
            input.putInt(seqNum);
            input.putInt(blockIndex);

            byte[] block = mac.doFinal(input.array());
            int toCopy = Math.min(block.length, length - offset);
            System.arraycopy(block, 0, keystream, offset, toCopy);
            offset += toCopy;
            blockIndex++;
        }
        return keystream;
    }

    // XOR data with the keystream — works for both encryption and decryption
    public byte[] xorWithKeystream(byte[] data, int offset, int length, int seqNum) throws Exception {
        byte[] keystream = generateKeystream(seqNum, length);
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (data[offset + i] ^ keystream[i]);
        }
        return result;
    }
}
