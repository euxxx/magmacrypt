package ru.mirea.edu.magmacrypt.cipher;

@Deprecated
public class Encryptor extends Algorithm {
    private final byte[] input;
    private final byte[] keySet;

    public Encryptor(byte[] input, byte[] keySet) {
        this.input = input;
        this.keySet = keySet;
    }

    @Override
    public byte[] perform() {
        return encrypt(this.input, this.keySet);
    }
}
