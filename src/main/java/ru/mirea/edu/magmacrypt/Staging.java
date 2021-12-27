package ru.mirea.edu.magmacrypt;


import java.util.Arrays;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.nio.file.StandardOpenOption;

public class Staging {
    static final int SIZE = 4;
    static final int BlockLength = 8;
    static final byte[][] P = {
            {(byte) 0x01, (byte) 0x07, (byte) 0x14, (byte) 0x13, (byte) 0x00, (byte) 0x05, (byte) 0x08, (byte) 0x03, (byte) 0x04, (byte) 0x15, (byte) 0x10, (byte) 0x06, (byte) 0x09, (byte) 0x12, (byte) 0x11, (byte) 0x02},
            {(byte) 0x08, (byte) 0x14, (byte) 0x02, (byte) 0x05, (byte) 0x06, (byte) 0x09, (byte) 0x01, (byte) 0x12, (byte) 0x15, (byte) 0x04, (byte) 0x11, (byte) 0x00, (byte) 0x13, (byte) 0x10, (byte) 0x03, (byte) 0x07},// 8 на 16
            {(byte) 0x05, (byte) 0x13, (byte) 0x15, (byte) 0x06, (byte) 0x09, (byte) 0x02, (byte) 0x12, (byte) 0x10, (byte) 0x11, (byte) 0x07, (byte) 0x08, (byte) 0x01, (byte) 0x04, (byte) 0x03, (byte) 0x14, (byte) 0x00},
            {(byte) 0x07, (byte) 0x15, (byte) 0x05, (byte) 0x10, (byte) 0x08, (byte) 0x01, (byte) 0x06, (byte) 0x13, (byte) 0x00, (byte) 0x09, (byte) 0x03, (byte) 0x14, (byte) 0x11, (byte) 0x04, (byte) 0x02, (byte) 0x12},
            {(byte) 0x12, (byte) 0x08, (byte) 0x02, (byte) 0x01, (byte) 0x13, (byte) 0x04, (byte) 0x15, (byte) 0x06, (byte) 0x07, (byte) 0x00, (byte) 0x10, (byte) 0x05, (byte) 0x03, (byte) 0x14, (byte) 0x09, (byte) 0x11},
            {(byte) 0x11, (byte) 0x03, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x15, (byte) 0x10, (byte) 0x13, (byte) 0x14, (byte) 0x01, (byte) 0x07, (byte) 0x04, (byte) 0x12, (byte) 0x09, (byte) 0x06, (byte) 0x00},
            {(byte) 0x06, (byte) 0x08, (byte) 0x02, (byte) 0x03, (byte) 0x09, (byte) 0x10, (byte) 0x05, (byte) 0x12, (byte) 0x01, (byte) 0x14, (byte) 0x04, (byte) 0x07, (byte) 0x11, (byte) 0x13, (byte) 0x00, (byte) 0x15},
            {(byte) 0x12, (byte) 0x04, (byte) 0x06, (byte) 0x02, (byte) 0x10, (byte) 0x05, (byte) 0x11, (byte) 0x09, (byte) 0x14, (byte) 0x08, (byte) 0x13, (byte) 0x07, (byte) 0x00, (byte) 0x03, (byte) 0x15, (byte) 0x01}
    };
    static final byte[][] subKey = new byte[32][4];

    static private byte[] Magma_XOR(byte[] block_1, byte[] block_2) {
        byte[] output = new byte[SIZE];
        for (int i = 0; i < SIZE; i++)
            output[i] = (byte) (block_1[i] ^ block_2[i]);
        return output;
    }

    static private byte[] XOR(byte[] block_1, byte[] block_2) {
        byte[] output = new byte[8];
        for (int i = 0; i < 8; i++)
            output[i] = (byte) (block_1[i] ^ block_2[i]);
        return output;
    }

    public static byte[] Magma_mod32(byte[] block, byte[] subKey) {
        int i;
        int temp = 0;
        byte[] c = new byte[SIZE];
        for (i = 3; i >= 0; i--) {
            temp = block[i] + subKey[i] + (temp >> 8);
            c[i] = (byte) (temp & 0xff);
        }
        return c;
    }

    public static byte[] Magma_T(byte[] block) {
        byte[] output = new byte[4];
        byte second_block;
        byte first_block;
        int i;
        for (i = 0; i < SIZE; i++) {
            second_block = (byte) ((block[i] & 0xf0) >> 4);
            first_block = (byte) (block[i] & 0x0f);
            second_block = P[2 * i][second_block];
            first_block = P[2 * i + 1][first_block];
            output[i] = (byte) ((first_block << 4) | second_block);
        }
        return output;
    }

    public static void genSubKey(byte[] key) {
        subKey[0] = Arrays.copyOfRange(key, 0, 4);
        subKey[1] = Arrays.copyOfRange(key, 4, 8);
        subKey[2] = Arrays.copyOfRange(key, 8, 12);
        subKey[3] = Arrays.copyOfRange(key, 12, 16);
        subKey[4] = Arrays.copyOfRange(key, 16, 20);
        subKey[5] = Arrays.copyOfRange(key, 20, 24);
        subKey[6] = Arrays.copyOfRange(key, 24, 28);
        subKey[7] = Arrays.copyOfRange(key, 28, 32);

        subKey[8] = Arrays.copyOfRange(key, 0, 4);
        subKey[9] = Arrays.copyOfRange(key, 4, 8);
        subKey[10] = Arrays.copyOfRange(key, 8, 12);
        subKey[11] = Arrays.copyOfRange(key, 12, 16);
        subKey[12] = Arrays.copyOfRange(key, 16, 20);
        subKey[13] = Arrays.copyOfRange(key, 20, 24);
        subKey[14] = Arrays.copyOfRange(key, 24, 28);
        subKey[15] = Arrays.copyOfRange(key, 28, 32);

        subKey[16] = Arrays.copyOfRange(key, 0, 4);
        subKey[17] = Arrays.copyOfRange(key, 4, 8);
        subKey[18] = Arrays.copyOfRange(key, 8, 12);
        subKey[19] = Arrays.copyOfRange(key, 12, 16);
        subKey[20] = Arrays.copyOfRange(key, 16, 20);
        subKey[21] = Arrays.copyOfRange(key, 20, 24);
        subKey[22] = Arrays.copyOfRange(key, 24, 28);
        subKey[23] = Arrays.copyOfRange(key, 28, 32);

        subKey[24] = Arrays.copyOfRange(key, 28, 32);
        subKey[25] = Arrays.copyOfRange(key, 24, 28);
        subKey[26] = Arrays.copyOfRange(key, 20, 24);
        subKey[27] = Arrays.copyOfRange(key, 16, 20);
        subKey[28] = Arrays.copyOfRange(key, 12, 16);
        subKey[29] = Arrays.copyOfRange(key, 8, 12);
        subKey[30] = Arrays.copyOfRange(key, 4, 8);
        subKey[31] = Arrays.copyOfRange(key, 0, 4);
    }

    public static byte[] g(byte[] key, byte[] block) {
        byte[] temp;
        int output32;
        byte[] output = new byte[SIZE];

        temp = Magma_mod32(block, key);

        temp = Magma_T(temp);

        output32 = temp[0];
        output32 = (output32 << 8) + temp[1];
        output32 = (output32 << 8) + temp[2];
        output32 = (output32 << 8) + temp[3];

        output32 = (output32 << 11) | (output32 >> 21);

        output[3] = (byte) output32;
        output[2] = (byte) (output32 >> 8);
        output[1] = (byte) (output32 >> 16);
        output[0] = (byte) (output32 >> 24);
        return output;
    }

    public static byte[] G(byte[] key, byte[] block) {
        byte[] block_right = new byte[SIZE];
        byte[] block_left = new byte[SIZE];
        byte[] G;
        byte[] output = new byte[8];
        int i;

        for (i = 0; i < 4; i++) {
            block_right[i] = block[4 + i];
            block_left[i] = block[i];
        }

        G = g(key, block_right);
        G = Magma_XOR(block_left, G);

        for (i = 0; i < 4; i++) {
            block_left[i] = block_right[i];
            block_right[i] = G[i];
        }

        for (i = 0; i < 4; i++) {
            output[i] = block_left[i];
            output[4 + i] = block_right[i];
        }
        return output;
    }


    public static byte[] GFin(byte[] key, byte[] block) {
        byte[] block_left = new byte[SIZE];
        byte[] block_right = new byte[SIZE];
        byte[] G;
        byte[] output = new byte[8];
        int i;

        for (i = 0; i < 4; i++) {
            block_left[i] = block[4 + i];
            block_right[i] = block[i];
        }

        G = g(key, block_left);
        G = Magma_XOR(block_right, G);

        for (i = 0; i < 4; i++)
            block_right[i] = G[i];

        for (i = 0; i < 4; i++) {
            output[i] = block_right[i];
            output[4 + i] = block_left[i];
        }
        return output;
    }

    // Режим шифрования CBC
    static public byte[] modeEncryptCBC(byte[] block, byte[] iv) {
        block = XOR(block, iv);
        return Encrypt(block);
    }

    // Режим расшифрования CBC
    static public byte[] modeDecryptCBC(byte[] block, byte[] iv) {
        block = Decrypt(block);
        return XOR(block, iv);
    }

    // Функция шифрования
    public static byte[] Encrypt(byte[] block) {
        int i;
        byte[] output;
        output = G(subKey[0], block);
        for (i = 1; i < 31; i++)
            output = G(subKey[i], output);
        output = GFin(subKey[31], output);
        return output;
    }

    public static byte[] Decrypt(byte[] block) {
        int i;
        byte[] output;
        output = G(subKey[31], block);
        for (i = 30; i > 0; i--)
            output = G(subKey[i], output);
        output = GFin(subKey[0], output);
        return output;
    }

    static void EncryptFile(String Plaintext, String EncText, byte[] IV, byte[] key) {
        genSubKey(key);
        File P = new File(Plaintext);
        byte[] text = new byte[1];
        byte[] block;
        byte[] iv = IV;
        try {
            text = Files.readAllBytes(Paths.get(Plaintext));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        int lastBytes = text.length % BlockLength;
        byte[] newText = new byte[text.length + (BlockLength - lastBytes)];
        for (int i = 0; i < text.length; i += BlockLength) {
            block = Arrays.copyOfRange(text, i, i + BlockLength);
            block = modeEncryptCBC(block, iv);
            System.arraycopy(block, 0, newText, i, BlockLength);
            iv = block;
        }

        System.arraycopy(newText, 0, text, 0, text.length);
        try {
            Files.write(Paths.get(EncText), text, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  P.delete();
    }

    static void DecryptFile(String Plaintext, String EncText, byte[] IV, byte[] key) {
        genSubKey(key);
        File P = new File("iv" + EncText + ".txt");
        File E = new File(EncText);
        byte[] text = new byte[1];
        byte[] block;
        byte[] temp;
        byte[] iv = IV;
        try {
            text = Files.readAllBytes(Paths.get(EncText));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        int lastBytes = text.length % BlockLength;
        byte[] newText = new byte[text.length + (BlockLength - lastBytes)];
        for (int i = 0; i < text.length; i += BlockLength) {
            block = Arrays.copyOfRange(text, i, i + BlockLength);
            temp = block;
            block = modeDecryptCBC(block, iv);
            System.arraycopy(block, 0, newText, i, BlockLength);
            iv = temp;
        }

        System.arraycopy(newText, 0, text, 0, text.length);

        try {
            Files.write(Paths.get(Plaintext), text, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //  P.delete();
        //  E.delete();
    }

    // Функция выработки ключа
    public static byte[] KEY(String key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key.getBytes());
        byte[] key_mas = md.digest();
        return key_mas;
    }

    // Функция выработки синхропосылки
    public static byte[] Vector() {
        SecureRandom r = new SecureRandom();
        byte[] IV = new byte[8];
        r.nextBytes(IV);
        return IV;
    }

    //запись синхропосылки
    public static void writeVector(byte[] iv, String str) {
        try {
            Files.write(Paths.get("iv" + str + ".txt"), iv, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //чтение синхропосылки
    public static byte[] readVector(String str) {
        byte[] iv = new byte[8];
        try {
            iv = Files.readAllBytes(Paths.get("iv" + str + ".txt"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return iv;
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {
        Scanner in = new Scanner(System.in);
        System.out.println("Для расшифровки файла введите - D, для шифрования файла введите - E ");
        String mode = in.nextLine();
        if (mode.equals("E")) {
            System.out.println("Введите название файла для шифрования в формате name.txt");
            String Plaintext = in.nextLine();
            System.out.println("Введите название файла куда записать зашифрованный текст в формате name.txt");
            String Enc_text = in.nextLine();
            System.out.println("Введите пароль ");
            String key = in.nextLine();
            byte[] r_IV;
            byte[] key_mas;
            key_mas = KEY(key);
            r_IV = Vector();
            writeVector(r_IV, Enc_text);
            EncryptFile(Plaintext, Enc_text, r_IV, key_mas);
        } else if (mode.equals("D")) {
            System.out.println("Введите название файла для расшифрования в формате name.txt");
            String Enc_text = in.nextLine();
            System.out.println("Введите название файла куда записать расшифрованный текст в формате name.txt");
            String Plaintext = in.nextLine();
            System.out.println("Введите пароль ");
            String key = in.nextLine();
            byte[] r_IV;
            byte[] key_mas;
            key_mas = KEY(key);
            r_IV = readVector(Enc_text);
            DecryptFile(Plaintext, Enc_text, r_IV, key_mas);
        } else {
            System.out.println("Wrong mode");
        }
    }
}

