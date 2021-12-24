package ru.mirea.edu.magmacrypt.auxiliary;

import java.io.Serializable;

public class Payload implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String fileName;
    private final byte[] bytes;

    public Payload(String fileName, byte[] bytes) {
        this.fileName = fileName;
        this.bytes = bytes;
    }

    public Payload() {
        this.fileName = null;
        this.bytes = null;
    }

    public String getFileName() {
        return this.fileName;
    }

    public byte[] getBytes() {
        return this.bytes;
    }
}