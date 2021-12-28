package ru.mirea.edu.magmacrypt.cipher;

@Deprecated
public class Decryptor extends Algorithm {
    private final byte[] input;
    private final byte[] keySet;

    public Decryptor(byte[] input, byte[] keySet) {
        this.input = input;
        this.keySet = keySet;
    }
    
    @Override
    public byte[] perform() {
        return decrypt(this.input, this.keySet);
    }
}
