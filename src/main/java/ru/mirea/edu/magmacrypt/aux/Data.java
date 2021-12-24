package ru.mirea.edu.magmacrypt.aux;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Data {
    public static byte[] packDirectory(String path) throws IOException {

        ArrayList<String> entries = new ArrayList<String>();

        Files.walk(Paths.get(path))
                .filter(Files::isRegularFile)
                .forEach(_path -> entries.add(_path.toString()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(out);

        for (String entryPath : entries) {
            zip.putNextEntry(new ZipEntry(entryPath));

            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(new File(entryPath)));
            byte[] bytesInput = new byte[4096];
            int readUnit = 0;

            while ((readUnit = bufferedInputStream.read(bytesInput)) != -1) {
                zip.write(bytesInput, 0, readUnit);
            }

            zip.closeEntry();
            bufferedInputStream.close();
        }

        zip.close();
        byte[] output = out.toByteArray();
        out.close();

        return output; 
    }

    public static byte[] readFileBytes(String path) throws IOException {
        return Files.readAllBytes(new File(path).toPath());
    }

    public static void writeBytesToFileByPath(String path, byte[] payload) throws IOException {
        Path _path = Paths.get(path);
        Files.write(_path, payload);
    }

    public static byte[] addPadding(byte[] source) {
        final int paddingRate = 8 - (source.length % 8);

        byte[] output = new byte[source.length + paddingRate];

        output[0] = (byte) paddingRate;

        for (int i = 0; i < source.length; i++) {
            output[i + 1] = source[i];
        }

        if (paddingRate > 2) {
            output[source.length + 1] = 1;
        }

        return output;
    }

    public static boolean checkIfPathIsFile(String path) {
        File file = new File(path);

        return file.isFile();
    }

    public static boolean checkIfPathNonExists(String path) {
        File file = new File(path);

        return Files.notExists(file.toPath());
    }

    public static byte[] serializeObjectToBytes(Payload obj) throws IOException {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        ObjectOutputStream ois = new ObjectOutputStream(boas);
        ois.writeObject(obj);
        ois.flush();
        ois.close();
        return boas.toByteArray();
    }

    public static Payload deserializeBytesToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        InputStream is = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Payload output = (Payload) ois.readObject();
        ois.close();
        is.close();
        return output;
    }
}
