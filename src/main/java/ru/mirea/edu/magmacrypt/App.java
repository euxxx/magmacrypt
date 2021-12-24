package ru.mirea.edu.magmacrypt;

import ru.mirea.edu.magmacrypt.cipher.Encryptor;
import ru.mirea.edu.magmacrypt.cipher.Decryptor;

import ru.mirea.edu.magmacrypt.aux.DataProcessing;
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
        DataProcessing.writeBytesToFileByPath(outputPath + KEY_FILE_OUTPUT_PATH, keySet);
        byte[] bytes;

        if (DataProcessing.checkIfPathIsFile(path)) {
            bytes = DataProcessing.readFileBytes(path);
        } else {
            bytes = DataProcessing.packDirectory(path);
            path = "RESTORED.ZIP";
        }

        Payload payload = new Payload(path, bytes);
        byte[] payloadBytes = DataProcessing.addPadding(DataProcessing.serializeObjectToBytes(payload));
        byte[] enc = new Encryptor(payloadBytes, keySet).perform();
        DataProcessing.writeBytesToFileByPath(outputPath + OUTPUT_FILE_PATH, enc);
    }

    public static void decrypt(String encryptedFilePath, String keyFilePath, String outputPath)
            throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
        byte[] keySet = DataProcessing.readFileBytes(keyFilePath);
        byte[] enc = DataProcessing.readFileBytes(encryptedFilePath);
        byte[] dec = new Decryptor(enc, keySet).perform();
        byte[] cut = Arrays.copyOfRange(dec, 1, dec.length - dec[0] + 1);
        Payload pl = DataProcessing.deserializeBytesToObject(cut);
        String fileName = pl.getFileName();

        if (!fileName.equalsIgnoreCase("RESTORED.ZIP")) {
            int lastSlashIndex = fileName.lastIndexOf("/") + 1;
            fileName = fileName.substring(lastSlashIndex);
        }

        DataProcessing.writeBytesToFileByPath(outputPath + fileName, pl.getBytes());
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
                if (DataProcessing.checkIfPathNonExists(sourcePath)) {
                    scanner.close();
                    throw new FileNotFoundException("Specified file does not exist!");
                }

                System.out.print("Where place encrypted data: ");
                String outputPath = scanner.nextLine();
                if (DataProcessing.checkIfPathNonExists(outputPath)) {
                    scanner.close();
                    throw new FileNotFoundException("Specified path does not exist!");
                }

                encrypt(sourcePath, outputPath);
            }
            case "-d" -> {
                System.out.print("Specify path to encrypted file: ");
                String encryptedPath = scanner.nextLine();
                if (DataProcessing.checkIfPathNonExists(encryptedPath)) {
                    scanner.close();
                    throw new FileNotFoundException("Specified file does not exist!");
                }

                System.out.print("Specify path to key file: ");
                String keyPath = scanner.nextLine();

                if (DataProcessing.checkIfPathNonExists(keyPath)) {
                    scanner.close();
                    throw new FileNotFoundException("Specified file does not exist!");
                }

                System.out.print("Where place decrypted data: ");
                String outputPath = scanner.nextLine();
                if (DataProcessing.checkIfPathNonExists(outputPath)) {
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
