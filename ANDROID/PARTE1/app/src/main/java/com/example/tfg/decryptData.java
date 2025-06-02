package com.example.tfg;


import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class decryptData {
    private static final String KEY_ALIAS = "MyKeyAlias";
    public static String desencriptar(byte[] encryptedData, byte[] iv) {

        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            // Desencriptar los datos
            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }
}