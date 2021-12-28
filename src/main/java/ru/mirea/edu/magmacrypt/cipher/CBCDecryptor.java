package ru.mirea.edu.magmacrypt.cipher;

public class CBCDecryptor extends CBC {

    public CBCDecryptor(byte[] input, byte[] initializationVector, byte[] key) {
        super(input, initializationVector, key);
        //TODO Auto-generated constructor stub
    }
    
    public byte[] perform() {
        return super.perform(false);
    }
}
