package ru.mirea.edu.magmacrypt;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.junit.Test;

import ru.mirea.edu.magmacrypt.auxiliary.Data;
import ru.mirea.edu.magmacrypt.auxiliary.KeyGen;
import ru.mirea.edu.magmacrypt.cipher.CBCDecryptor;
import ru.mirea.edu.magmacrypt.cipher.CBCEncryptor;

import static org.junit.Assert.assertTrue;

import static ru.mirea.edu.magmacrypt.App.decrypt;
import static ru.mirea.edu.magmacrypt.App.encrypt;

public class AppTest {
    @Test
    public void algorithmTest() throws NoSuchAlgorithmException {
        final byte[] testKey = KeyGen.generateRandomBytesSequence(32);
        final byte[] testIV = KeyGen.generateRandomBytesSequence(8);

        byte[] testPayloadRaw = {107, 21, 1, 19, -119, 39, 116, 91, 11, 106, 35, 110, -83, 78, -98, 61, 60, 114, -100, -80, -33, 34, 109, -28, 73, -80, 65, -126, -2, 19, -19, 117, 121, -60, -51, -44, 93, 85, 91, 107, -59, -60, 7, -23, -89, 125, 66, 116, -86, -91, 41, 20, 3, 97, 15, 42, 45, 12, -108, 100, -58, -83, 125, 103, 60, 47, -77, 79, -15, -29, -61, 91, -68, -14, -100, 119, 32, -111, -14, 88, 52, -126, -14, 29, -126, -21, -8, -81, 44, -67, 5, 100};

        byte[] testPayload = Data.addPadding(testPayloadRaw);

        byte[] enc = new CBCEncryptor(testPayload, testIV, testKey).perform();
        byte[] dec = new CBCDecryptor(enc, testIV, testKey).perform();

        byte[] cut = Arrays.copyOfRange(dec, 1, dec.length - dec[0] + 1);
        assertTrue("Algorithm work check pass: ", Arrays.equals(testPayloadRaw, cut));
    }

    @Test
    public void generalTest() throws NoSuchAlgorithmException, IOException, ClassNotFoundException {

        final String TEST_DATA_RESOURCES_PATH = "./TEST_DATA/RESOURCES/";
        final String TEST_DATA_OUTPUT_PATH = "./TEST_DATA/OUTPUT/";

        // ? - SINGLE FILE TEST
        encrypt(TEST_DATA_RESOURCES_PATH + "SINGLE_BINARY_FILE.jpeg", TEST_DATA_OUTPUT_PATH);
        decrypt(TEST_DATA_OUTPUT_PATH + "ENCRYPTED", TEST_DATA_OUTPUT_PATH + "KEY", TEST_DATA_OUTPUT_PATH + "IV", TEST_DATA_OUTPUT_PATH);

        byte[] decryptedZipBytes = Data.readFileBytes(TEST_DATA_OUTPUT_PATH + "SINGLE_BINARY_FILE.jpeg");

        byte[] testPayload = Data.readFileBytes(TEST_DATA_OUTPUT_PATH + "SINGLE_BINARY_FILE.jpeg");

        boolean fileTestResult = Arrays.equals(decryptedZipBytes, testPayload);;

        // ? - DIRECTORY TEST
        encrypt(TEST_DATA_RESOURCES_PATH, TEST_DATA_OUTPUT_PATH);
        decrypt(TEST_DATA_OUTPUT_PATH + "ENCRYPTED", TEST_DATA_OUTPUT_PATH + "KEY", TEST_DATA_OUTPUT_PATH + "IV", TEST_DATA_OUTPUT_PATH);

        decryptedZipBytes = Data.readFileBytes(TEST_DATA_OUTPUT_PATH + "RESTORED.ZIP");

        testPayload = Data.packDirectory(TEST_DATA_RESOURCES_PATH);

        boolean dirTestResult = Arrays.equals(decryptedZipBytes, testPayload);

        assertTrue("General application check pass: ", dirTestResult && fileTestResult);
    }
}
