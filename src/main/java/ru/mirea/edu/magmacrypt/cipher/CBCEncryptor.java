package ru.mirea.edu.magmacrypt.cipher;

public class CBCEncryptor extends CBC {

    public CBCEncryptor(byte[] input, byte[] initializationVector, byte[] key) {
        super(input, initializationVector, key);
    }
    
    public byte[] perform() {
        return super.perform(true);
    }
}
