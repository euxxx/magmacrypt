package ru.mirea.edu.magmacrypt.cipher;

import java.util.Arrays;

public abstract class CBC {
    private final byte[] INPUT;
    private final byte[] INITIALIZATION_VECTOR;
    private final byte[] KEY;

    public abstract byte[] perform();

    private final byte[][] SUBSTITUTION_BOX = {
            { 0x4, 0xA, 0x9, 0x2, 0xD, 0x8, 0x0, 0xE, 0x6, 0xB, 0x1, 0xC, 0x7, 0xF, 0x5, 0x3 },
            { 0xE, 0xB, 0x4, 0xC, 0x6, 0xD, 0xF, 0xA, 0x2, 0x3, 0x8, 0x1, 0x0, 0x7, 0x5, 0x9 },
            { 0x5, 0x8, 0x1, 0xD, 0xA, 0x3, 0x4, 0x2, 0xE, 0xF, 0xC, 0x7, 0x6, 0x0, 0x9, 0xB },
            { 0x7, 0xD, 0xA, 0x1, 0x0, 0x8, 0x9, 0xF, 0xE, 0x4, 0x6, 0xC, 0xB, 0x2, 0x5, 0x3 },
            { 0x6, 0xC, 0x7, 0x1, 0x5, 0xF, 0xD, 0x8, 0x4, 0xA, 0x9, 0xE, 0x0, 0x3, 0xB, 0x2 },
            { 0x4, 0xB, 0xA, 0x0, 0x7, 0x2, 0x1, 0xD, 0x3, 0x6, 0x8, 0x5, 0x9, 0xC, 0xF, 0xE },
            { 0xD, 0xB, 0x4, 0x1, 0x3, 0xF, 0x5, 0x9, 0x0, 0xA, 0xE, 0x7, 0x6, 0x8, 0x2, 0xC },
            { 0x1, 0xF, 0xD, 0x0, 0x5, 0x7, 0xA, 0x4, 0x9, 0x2, 0x3, 0xE, 0x6, 0xB, 0x8, 0xC }
    };

    public CBC(byte[] input, byte[] initializationVector, byte[] key) {
        this.INPUT = input;
        this.INITIALIZATION_VECTOR = initializationVector;
        this.KEY = key;
    }

    private byte[][] buildRoundKeySet(byte[] sourceKey) {
        byte[][] output = new byte[32][4];

        int k = 0;

        for (int i = 0; i < 24; i += 8) {
            for (int j = 0; j < 8; j++) {
                output[i + j] = Arrays.copyOfRange(sourceKey, k, k + 4);
                k += 4;
            }
            k = 0;
        }

        k = 32;

        for (int n = 24; n < 32; n++) {
            output[n] = Arrays.copyOfRange(sourceKey, k - 4, k);
            k -= 4;
        }

        return output;
    }

    private byte[] xor2ByteArrays(byte[] a, byte[] b, byte outputLength) {
        byte[] output = new byte[outputLength];

        for (int i = 0; i < outputLength; i++) {
            output[i] = (byte) (a[i] ^ b[i]);
        }

        return output;
    }

    private byte[] addition2ByteArraysMod32(byte[] a, byte[] b) {
        byte[] output = new byte[4];

        int temp = 0;

        for (int i = 3; i >= 0; i--) {
            temp = a[i] + b[i] + (temp >> 8);
            output[i] = (byte) (temp & 0xFF); 
        }

        return output;
    }

    private byte[] substituteUsingSBox(byte[] block) {
        byte[] output = new byte[4];

        byte a, b;

        for (int i = 0; i < 4; i++) {
            b = (byte) ((block[i] & 0xFF) >> 4);
            a = (byte) (block[i] & 0xFF);

            b = SUBSTITUTION_BOX[2 * i][b];
            a = SUBSTITUTION_BOX[2 * i + 1][a];

            output[i] = (byte) ((a << 4) | b);
        }

        return output;
    }

    private byte[] performRound(byte[] input, byte[] roundKey, boolean isLast) {
        byte[] output = new byte[8];

        byte[] leftBlock = new byte[4];
        byte[] rightBlock = new byte[4];

        return output;
    }
}
