package ru.mirea.edu.magmacrypt;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.junit.Test;

import ru.mirea.edu.magmacrypt.aux.KeyGen;
import ru.mirea.edu.magmacrypt.cipher.Decryptor;
import ru.mirea.edu.magmacrypt.cipher.Encryptor;
import ru.mirea.edu.magmacrypt.aux.Data;

import static org.junit.Assert.assertTrue;

import static ru.mirea.edu.magmacrypt.App.decrypt;
import static ru.mirea.edu.magmacrypt.App.encrypt;

public class AppTest {
    @Test
    public void algorithmTest() throws NoSuchAlgorithmException {
        final byte[] testKey = KeyGen.generateKey();

        byte[] testPayloadRaw = { 92, 93, -51, 14, -59, -46, 4, 116, 19, -107, -90, -59, 23, 38, 97, 1, 50, 120, 49,
                -83,
                106, -67, 75, 70, 25, -54, -124, 73, -29, 31, 69, 74, -102, -47, -27, -35, -60, 50, 86, -38, -125, -64,
                -89, 91, 52, -107, -117, -41, 111, 82, 5, 109, -42, 97, 71, -2, 2, -113, -51, 78, -28, 121, 8, 84, 54,
                -2, -57, -123, -84, 89 };

        byte[] testPayload = Data.addPadding(testPayloadRaw);

        byte[] enc = new Encryptor(testPayload, testKey).perform();
        byte[] dec = new Decryptor(enc, testKey).perform();

        byte[] cut = Arrays.copyOfRange(dec, 1, dec.length - dec[0] + 1);
        assertTrue("Algorithm work check pass: ", Arrays.equals(testPayloadRaw, cut));
    }

    @Test
    public void generalTest() throws NoSuchAlgorithmException, IOException, ClassNotFoundException {

        final String TEST_DATA_RESOURCES_PATH = "./TEST_DATA/RESOURCES/";
        final String TEST_DATA_OUTPUT_PATH = "./TEST_DATA/OUTPUT/";

        // ? - SINGLE FILE TEST
        encrypt(TEST_DATA_RESOURCES_PATH + "SINGLE_BINARY_FILE.jpeg", TEST_DATA_OUTPUT_PATH);
        decrypt(TEST_DATA_OUTPUT_PATH + "ENCRYPTED", TEST_DATA_OUTPUT_PATH + "KEY", TEST_DATA_OUTPUT_PATH);

        byte[] decryptedZipBytes = Data.readFileBytes(TEST_DATA_OUTPUT_PATH + "SINGLE_BINARY_FILE.jpeg");

        byte[] testPayload = Data.readFileBytes(TEST_DATA_OUTPUT_PATH + "SINGLE_BINARY_FILE.jpeg");

        boolean fileTestResult = Arrays.equals(decryptedZipBytes, testPayload);;

        // ? - DIRECTORY TEST
        encrypt(TEST_DATA_RESOURCES_PATH, TEST_DATA_OUTPUT_PATH);
        decrypt(TEST_DATA_OUTPUT_PATH + "ENCRYPTED", TEST_DATA_OUTPUT_PATH + "KEY", TEST_DATA_OUTPUT_PATH);

        decryptedZipBytes = Data.readFileBytes(TEST_DATA_OUTPUT_PATH + "RESTORED.ZIP");

        testPayload = Data.packDirectory(TEST_DATA_RESOURCES_PATH);

        boolean dirTestResult = Arrays.equals(decryptedZipBytes, testPayload);

        assertTrue(dirTestResult && fileTestResult);
    }
}
