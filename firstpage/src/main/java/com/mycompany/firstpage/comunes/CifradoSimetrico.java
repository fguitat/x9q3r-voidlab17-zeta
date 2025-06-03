/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.firstpage.comunes;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author ferran
 */
public class CifradoSimetrico {
    private static final String ALGORITMO = "AES";
    private static final String FORMATO = "AES/GCM/NoPadding";
    private static final int AES_LENGTH_BIT = 256; //AES-256
    private static final int TAG_LENGTH_BIT = 128; // 16 bytes
    private static final int IV_LENGTH_BYTE = 12; // 12 bytes es lo recomendado para GCM

    public static String cifrador(String plaintext, String clave) throws Exception {
        
        byte[] keyBytes = Arrays.copyOf(clave.getBytes(StandardCharsets.UTF_8), AES_LENGTH_BIT/8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITMO);

        // Generar IV aleatorio
        byte[] iv = new byte[IV_LENGTH_BYTE];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(FORMATO);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

        // Unimos IV + Ciphertext+Tag
        byte[] decryptedBytes = new byte[IV_LENGTH_BYTE + ciphertext.length];
        System.arraycopy(iv, 0, decryptedBytes, 0, IV_LENGTH_BYTE);
        System.arraycopy(ciphertext, 0, decryptedBytes, IV_LENGTH_BYTE, ciphertext.length);

        return Base64.getEncoder().encodeToString(decryptedBytes);
    }
    public static String descifrador(String textoCifrado, String clave) throws Exception {
        byte[] decryptedBytes = Base64.getDecoder().decode(textoCifrado);
        byte[] keyBytes = Arrays.copyOf(clave.getBytes(StandardCharsets.UTF_8), AES_LENGTH_BIT/8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITMO);

        // Extraer IV
        byte[] iv = new byte[IV_LENGTH_BYTE];
        System.arraycopy(decryptedBytes, 0, iv, 0, IV_LENGTH_BYTE);

        // Extraer Ciphertext+Tag
        int ctLength = decryptedBytes.length - IV_LENGTH_BYTE;
        byte[] ciphertext = new byte[ctLength];
        System.arraycopy(decryptedBytes, IV_LENGTH_BYTE, ciphertext, 0, ctLength);

        Cipher cipher = Cipher.getInstance(FORMATO);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] decrypted = cipher.doFinal(ciphertext);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
