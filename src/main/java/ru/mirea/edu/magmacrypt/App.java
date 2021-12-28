package ru.mirea.edu.magmacrypt;

import ru.mirea.edu.magmacrypt.cipher.CBCEncryptor;
import ru.mirea.edu.magmacrypt.auxiliary.Data;
import ru.mirea.edu.magmacrypt.auxiliary.KeyGen;
import ru.mirea.edu.magmacrypt.auxiliary.Payload;
import ru.mirea.edu.magmacrypt.cipher.CBCDecryptor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

public class App {
    public final static String OUTPUT_FILE_PATH = "ENCRYPTED";
    public final static String KEY_FILE_OUTPUT_PATH = "KEY";
    public final static String IV_FILE_OUTPUT_PATH = "IV";


    public static void encrypt(String inputPath, String outputPath) throws IOException, NoSuchAlgorithmException {
        byte[] keySet = KeyGen.generateRandomBytesSequence(32);
        byte[] initializationVector = KeyGen.generateRandomBytesSequence(8);
        Data.writeBytesToFileByPath(outputPath + KEY_FILE_OUTPUT_PATH, keySet);
        Data.writeBytesToFileByPath(outputPath + IV_FILE_OUTPUT_PATH, initializationVector);
        byte[] bytes;

        if (Data.checkIfPathIsFile(inputPath)) {
            bytes = Data.readFileBytes(inputPath);
        } else {
            bytes = Data.packDirectory(inputPath);
            inputPath = "RESTORED.ZIP";
        }

        Payload payload = new Payload(inputPath, bytes);
        byte[] payloadBytes = Data.addPadding(Data.serializeObjectToBytes(payload));
        byte[] enc = new CBCEncryptor(payloadBytes, initializationVector ,keySet).perform();
        Data.writeBytesToFileByPath(outputPath + OUTPUT_FILE_PATH, enc);
    }

    public static void decrypt(String encryptedFilePath, String keyFilePath, String initVectorFilePath, String outputPath)
            throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
        byte[] keySet = Data.readFileBytes(keyFilePath);
        byte[] initVector = Data.readFileBytes(initVectorFilePath);
        byte[] enc = Data.readFileBytes(encryptedFilePath);
        byte[] dec = new CBCDecryptor(enc, initVector, keySet).perform();
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

        args = new String[1];

        args[0] = "-d";

        if (args.length == 0) {
            scanner.close();
            throw new InvalidParameterException(
                    "Specify application operating mode: -e to encrypt or -d to decrypt data!");
        }

        String outputPath = "";
        
        if (args[0].equalsIgnoreCase("-e")) {
            System.out.print("Specify path to data: ");
            String sourcePath = scanner.nextLine();
            if (Data.checkIfPathNonExists(sourcePath)) {
                scanner.close();
                throw new FileNotFoundException("Specified file does not exist!");
            }

            System.out.print("Where place encrypted data: ");
            outputPath = scanner.nextLine();
            if (Data.checkIfPathNonExists(outputPath)) {
                scanner.close();
                throw new FileNotFoundException("Specified path does not exist!");
            }

            encrypt(sourcePath, outputPath);
        } else {
            if (args[0].equalsIgnoreCase("-d")) {
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

                System.out.print("Specify path to initialization vector file: ");
                String initVectorFIlePath = scanner.nextLine();

                if (Data.checkIfPathNonExists(initVectorFIlePath)) {
                    scanner.close();
                    throw new FileNotFoundException("Specified file does not exist!");
                }

                System.out.print("Where place decrypted data: ");
                outputPath = scanner.nextLine();
                if (Data.checkIfPathNonExists(outputPath)) {
                    scanner.close();
                    throw new FileNotFoundException("Specified path does not exist!");
                }

                decrypt(encryptedPath, keyPath, initVectorFIlePath, outputPath);
            } else {
                System.out.println("Run with -e arg to encrypt or -d to decrypt!");
            }
        }

        scanner.close();
    }
}