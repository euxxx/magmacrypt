package ru.mirea.edu.magmacrypt.aux;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class KeyGen {
    public static byte[] generateKey() throws NoSuchAlgorithmException {
        byte[] output = new byte[64];

        for (int i = 0; i < 64; i++) {
            output[i] = (byte) (SecureRandom.getInstanceStrong().nextInt(255) - 128);
        }

        return output;
    }
}
