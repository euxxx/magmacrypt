package ru.mirea.edu.magmacrypt.cipher;

import java.util.Arrays;

public abstract class Algorithm {
    // TLS_GOSTR341112_256_WITH_28147_CNT_IMIT
    private final byte[][] sBox = {
            { 0x4, 0xA, 0x9, 0x2, 0xD, 0x8, 0x0, 0xE, 0x6, 0xB, 0x1, 0xC, 0x7, 0xF, 0x5, 0x3 },
            { 0xE, 0xB, 0x4, 0xC, 0x6, 0xD, 0xF, 0xA, 0x2, 0x3, 0x8, 0x1, 0x0, 0x7, 0x5, 0x9 },
            { 0x5, 0x8, 0x1, 0xD, 0xA, 0x3, 0x4, 0x2, 0xE, 0xF, 0xC, 0x7, 0x6, 0x0, 0x9, 0xB },
            { 0x7, 0xD, 0xA, 0x1, 0x0, 0x8, 0x9, 0xF, 0xE, 0x4, 0x6, 0xC, 0xB, 0x2, 0x5, 0x3 },
            { 0x6, 0xC, 0x7, 0x1, 0x5, 0xF, 0xD, 0x8, 0x4, 0xA, 0x9, 0xE, 0x0, 0x3, 0xB, 0x2 },
            { 0x4, 0xB, 0xA, 0x0, 0x7, 0x2, 0x1, 0xD, 0x3, 0x6, 0x8, 0x5, 0x9, 0xC, 0xF, 0xE },
            { 0xD, 0xB, 0x4, 0x1, 0x3, 0xF, 0x5, 0x9, 0x0, 0xA, 0xE, 0x7, 0x6, 0x8, 0x2, 0xC },
            { 0x1, 0xF, 0xD, 0x0, 0x5, 0x7, 0xA, 0x4, 0x9, 0x2, 0x3, 0xE, 0x6, 0xB, 0x8, 0xC }
    };

    public Algorithm() {
    }

    private int getRoundKey(int w, byte[] keySet) {
        return keySet[w * 4 + 3] << 24
                | (keySet[w * 4 + 2] & 0xFF) << 16
                | (keySet[w * 4 + 1] & 0xFF) << 8
                | (keySet[w * 4] & 0xFF);
    }

    private int shiftLeftBy11Ranks(int a) {
        int shift = 11;
        a = (a >>> (32 - shift)) | a << shift;
        return a;
    }

    private byte[] mergeArrays(int n1, int n2) {
        byte[] bytes = new byte[8];

        for (int j = 0; j < 4; j++) {
            bytes[j] = (byte) ((n1 >> 24 - (j * 8)) & 0xFF);
        }

        for (int j = 4; j < 8; j++) {
            bytes[j] = (byte) ((n2 >> 24 - (j * 8)) & 0xFF);
        }

        return bytes;
    }

    private int substituteUsingSBox(int n) {
        int xTest = 0;

        for (int i = 0, j = 0; i <= 28; i += 4, j++) {
            xTest += (sBox[j][(byte) ((n >> i) & 0xF)]) << (i);
        }

        return xTest;
    }

    private void performRound(int startIndex, byte[] input, int roundKey) {
        int n2 = input[input.length - startIndex - 1] << 24
                | (input[input.length - startIndex - 2] & 0xFF) << 16
                | (input[input.length - startIndex - 3] & 0xFF) << 8
                | (input[input.length - startIndex - 4] & 0xFF);
        int n1 = input[input.length - startIndex - 5] << 24
                | (input[input.length - startIndex - 6] & 0xFF) << 16
                | (input[input.length - startIndex - 7] & 0xFF) << 8
                | (input[input.length - startIndex - 8] & 0xFF);
        int s = ((n1 + roundKey) & 0xFFFFFFFF);

        s = substituteUsingSBox(s);
        s = shiftLeftBy11Ranks(s);
        s = s ^ n2;

        byte[] n1s = mergeArrays(n1, s);

        for (int j = 7; j >= 0; j--) {
            input[input.length - 1 - (startIndex + j)] = n1s[j];
        }
    }

    protected byte[] encrypt(byte[] input, byte[] keySet) {
        byte[] output = Arrays.copyOf(input, input.length);

        for (int i = 0; i <= input.length - 8; i += 8) {
            for (int q = 0; q < 3; q++) {
                for (int w = 0; w < 8; w++) {
                    int roundKey = getRoundKey(w, keySet);
                    performRound(i, output, roundKey);
                }
            }
            for (int w = 7; w >= 0; w--) {
                int roundKey = getRoundKey(w, keySet);
                performRound(i, output, roundKey);
                if (w == 0) {
                    byte[] lastArray = new byte[8];
                    System.arraycopy(output, output.length - i - 8, lastArray, 4, 4);
                    System.arraycopy(output, output.length - i - 4, lastArray, 0, 4);
                    System.arraycopy(lastArray, 0, output, output.length - i - 8, 8);
                }
            }
        }

        return output;
    }

    protected byte[] decrypt(byte[] input, byte[] keySet) {
        byte[] output = Arrays.copyOf(input, input.length);
        int w;

        for (int i = 0; i <= input.length - 8; i += 8) {
            for (w = 0; w < 8; w++) {
                int roundKey = getRoundKey(w, keySet);
                performRound(i, output, roundKey);
            }
            for (int q = 0; q < 3; q++) {
                for (w = 7; w >= 0; w--) {
                    int roundKey = getRoundKey(w, keySet);
                    performRound(i, output, roundKey);
                }
                if (w == -1 && q == 2) {
                    byte[] lastArray = new byte[8];
                    System.arraycopy(output, output.length - i - 8, lastArray, 4, 4);
                    System.arraycopy(output, output.length - i - 4, lastArray, 0, 4);
                    System.arraycopy(lastArray, 0, output, output.length - i - 8, 8);
                }
            }
        }

        return output;
    }

    public abstract byte[] perform();
}
