package com.securefile.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    @Value("${encryption.key}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;

    public byte[] encrypt(byte[] data) throws Exception {
        SecretKey key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] iv = generateIV();
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        
        byte[] encryptedData = cipher.doFinal(data);
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
        
        return combined;
    }

    public byte[] decrypt(byte[] encryptedData) throws Exception {
        SecretKey key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        
        byte[] iv = new byte[16];
        System.arraycopy(encryptedData, 0, iv, 0, iv.length);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        byte[] data = new byte[encryptedData.length - iv.length];
        System.arraycopy(encryptedData, iv.length, data, 0, data.length);
        
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return cipher.doFinal(data);
    }

    private SecretKey generateKey() throws Exception {
        byte[] keyBytes = encryptionKey.getBytes("UTF-8");
        // Ensure the key is 16, 24, or 32 bytes for AES
        byte[] key = new byte[32];
        System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, key.length));
        return new SecretKeySpec(key, "AES");
    }

    private byte[] generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
} 