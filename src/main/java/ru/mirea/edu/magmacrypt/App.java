package ru.mirea.edu.magmacrypt;

import ru.mirea.edu.magmacrypt.cipher.Encryptor;
import ru.mirea.edu.magmacrypt.cipher.Decryptor;

import ru.mirea.edu.magmacrypt.aux.Data;
import ru.mirea.edu.magmacrypt.aux.KeyGenerator;
import ru.mirea.edu.magmacrypt.aux.Payload;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

public class App {
    public final static String OUTPUT_FILE_PATH = "ENCRYPTED";
    public final static String KEY_FILE_OUTPUT_PATH = "KEY";

    public static void encrypt(String path, String outputPath) throws IOException, NoSuchAlgorithmException {
        byte[] keySet = KeyGenerator.generateKey();
        Data.writeBytesToFileByPath(outputPath + KEY_FILE_OUTPUT_PATH, keySet);
        byte[] bytes;

        if (Data.checkIfPathIsFile(path)) {
            bytes = Data.readFileBytes(path);
        } else {
            bytes = Data.packDirectory(path);
            path = "RESTORED.ZIP";
        }

        Payload payload = new Payload(path, bytes);
        byte[] payloadBytes = Data.addPadding(Data.serializeObjectToBytes(payload));
        byte[] enc = new Encryptor(payloadBytes, keySet).perform();
        Data.writeBytesToFileByPath(outputPath + OUTPUT_FILE_PATH, enc);
    }

    public static void decrypt(String encryptedFilePath, String keyFilePath, String outputPath)
            throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
        byte[] keySet = Data.readFileBytes(keyFilePath);
        byte[] enc = Data.readFileBytes(encryptedFilePath);
        byte[] dec = new Decryptor(enc, keySet).perform();
        byte[] cut = Arrays.copyOfRange(dec, 1, dec.length - dec[0] + 1);
        Payload pl = Data.deserializeBytesToObject(cut);
        String fileName = pl.getFileName();

        if (!fileName.equalsIgnoreCase("RESTORED.ZIP")) {
            int lastSlashIndex = fileName.lastIndexOf("/") + 1;
            fileName = fileName.substring(lastSlashIndex);
        }

        Data.writeBytesToFileByPath(outputPath + fileName, pl.getBytes());
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
        Scanner scanner = new Scanner(System.in);

        if (args.length == 0) {
            scanner.close();
            throw new InvalidParameterException(
                    "Specify application operating mode: -e to encrypt or -d to decrypt data!");
        }

        switch (args[0]) {
            case "-e" -> {
                System.out.print("Specify path to data: ");
                String sourcePath = scanner.nextLine();
                // scanner.close();
                if (Data.checkIfPathNonExists(sourcePath)) {
                    scanner.close();
                    throw new FileNotFoundException("Specified file does not exist!");
                }

                System.out.print("Where place encrypted data: ");
                String outputPath = scanner.nextLine();
                if (Data.checkIfPathNonExists(outputPath)) {
                    scanner.close();
                    throw new FileNotFoundException("Specified path does not exist!");
                }

                encrypt(sourcePath, outputPath);
            }
            case "-d" -> {
                System.out.print("Specify path to encrypted file: ");
                String encryptedPath = scanner.nextLine();
                if (Data.checkIfPathNonExists(encryptedPath)) {
                    scanner.close();
                    throw new FileNotFoundException("Specified file does not exist!");
                }

                System.out.print("Specify path to key file: ");
                String keyPath = scanner.nextLine();

                if (Data.checkIfPathNonExists(keyPath)) {
                    scanner.close();
                    throw new FileNotFoundException("Specified file does not exist!");
                }

                System.out.print("Where place decrypted data: ");
                String outputPath = scanner.nextLine();
                if (Data.checkIfPathNonExists(outputPath)) {
                    scanner.close();
                    throw new FileNotFoundException("Specified path does not exist!");
                }

                decrypt(encryptedPath, keyPath, outputPath);
            }
            default -> System.out.println("Run with -e arg to encrypt or -d to decrypt!");
        }

        scanner.close();
    }
}
