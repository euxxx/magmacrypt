package ru.mirea.edu.magmacrypt.auxiliary;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class KeyGen {
    public static byte[] generateRandomBytesSequence(int length) throws NoSuchAlgorithmException {
        byte[] output = new byte[length];

        for (int i = 0; i < length; i++) {
            output[i] = (byte) (SecureRandom.getInstanceStrong().nextInt(255) - 128);
        }

        return output;
    }
}
