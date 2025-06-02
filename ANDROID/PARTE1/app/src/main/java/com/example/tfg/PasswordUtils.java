package com.example.tfg;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

public class PasswordUtils {
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String TAG = "PasswordUtils";
    public static String obtenerPass(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String ivPasswordBase64 = prefs.getString("ivPassword", "");
        String encryptedPasswordBase64 = prefs.getString("password", "");
        if (ivPasswordBase64.isEmpty() || encryptedPasswordBase64.isEmpty()) {
            Log.e(TAG, "contraseña no encontrados en SharedPreferences");
            return null;
        }
        try {
            byte[] ivPassword = Base64.decode(ivPasswordBase64, Base64.DEFAULT);
            byte[] encryptedPassword = Base64.decode(encryptedPasswordBase64, Base64.DEFAULT);
            return decryptData.desencriptar(encryptedPassword, ivPassword);
        } catch (Exception e) {
            Log.e(TAG, "Error al desencriptar la contraseña", e);
            return null;
        }
    }

    public static String obtenerUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String ivusuarioBase64 = prefs.getString("ivusuario", "");
        String encryptedusuarioBase64 = prefs.getString("usuario", "");

        if (ivusuarioBase64.isEmpty() || encryptedusuarioBase64.isEmpty()) {
            Log.e(TAG, "IV no encontrados en SharedPreferences");
            return null;
        }

        try {
            byte[] ivPassword = Base64.decode(ivusuarioBase64, Base64.DEFAULT);
            byte[] encryptedPassword = Base64.decode(encryptedusuarioBase64, Base64.DEFAULT);

            return decryptData.desencriptar(encryptedPassword, ivPassword);
        } catch (Exception e) {
            Log.e(TAG, "Error al desencriptar la contraseña", e);
            return null;
        }
    }
}
