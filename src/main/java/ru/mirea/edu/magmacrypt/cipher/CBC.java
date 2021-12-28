package ru.mirea.edu.magmacrypt.cipher;

import java.util.Arrays;

public abstract class CBC {
    private byte[] input;
    private byte[] initializationVector;
    private final byte[][] KEY_SET;

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
        this.input = input;
        this.initializationVector = initializationVector;
        this.KEY_SET = buildRoundKeySet(key);
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

    private byte[] xor2ByteArrays(byte[] a, byte[] b, int outputLength) {
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
            b = (byte) ((block[i] & 0xF0) >> 4);
            a = (byte) (block[i] & 0x0F);

            b = SUBSTITUTION_BOX[2 * i][b];
            a = SUBSTITUTION_BOX[2 * i + 1][a];

            output[i] = (byte) ((a << 4) | b);
        }

        return output;
    }

    private byte[] prepareRound(byte[] block, byte[] roundKey) {
        byte[] output = new byte[4];

        byte[] substituted = substituteUsingSBox(addition2ByteArraysMod32(block, roundKey));

        int temp = 0;

        for (int i = 0; i < 4; i++) {
            temp = (i == 0) ? substituted[0] : (temp << 8) + substituted[i];
        }

        temp = (temp << 11) | (temp >> 21);

        for (int j = 0; j < 4; j++) {
            output[3 - j] = (j == 0) ? (byte) temp : (byte) (temp >> (8 * j));
        }

        return output;
    }

    private byte[] performRound(byte[] originalBlock, byte[] roundKey, boolean isLast) {
        byte[] output = new byte[8];

        byte[] leftBlock = new byte[4];
        byte[] rightBlock = new byte[4];

        for (int i = 0; i < 4; i++) {
            rightBlock[i] = !isLast ? originalBlock[i + 4] : originalBlock[i];
            leftBlock[i] = !isLast ? originalBlock[i] : originalBlock[i + 4];
        }

        byte[] round = xor2ByteArrays(!isLast ? leftBlock : rightBlock, prepareRound(roundKey, !isLast ? rightBlock : leftBlock), 4);

        for (int j = 0; j < 4; j++) {
            if (!isLast) {
                leftBlock[j] = rightBlock[j];
            }
            rightBlock[j] = round[j];
        }

        for (int k = 0; k < 4; k++) {
            output[k] = !isLast ? leftBlock[k] : rightBlock[k];
            output[k + 4] = !isLast ? rightBlock[k] : leftBlock[k];
        }

        return output;
    }

    private byte[] encryptBlock(byte[] block) {
        block = xor2ByteArrays(block, initializationVector, 8);

        block = performRound(block, KEY_SET[0], false);

        for (int i = 1; i < 31; i++) {
            block = performRound(block, KEY_SET[i], false);
        }

        block = performRound(block, KEY_SET[31], true);

        return block;
    }

    private byte[] decryptBlock(byte[] block) {
        block = performRound(block, KEY_SET[31], false);

        for (int i = 30; i > 0; i--) {
            block = performRound(block, KEY_SET[i], false);
        }

        block = performRound(block, KEY_SET[0], true);

        return xor2ByteArrays(block, initializationVector, 8);
    }

    protected byte[] perform(boolean mode) {
        byte[] block, temp, output;

        output = new byte[this.input.length];

        for (int i = 0; i < input.length; i += 8) {
            block = Arrays.copyOfRange(input, i, i + 8);
            temp = !mode ? block : null;
            block = mode ? encryptBlock(block) : decryptBlock(block);
            System.arraycopy(block, 0, output, i, 8);
            this.initializationVector = mode ? block : temp;
        }

        return output;
    }
}