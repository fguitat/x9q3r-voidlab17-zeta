package com.example.tfg;

import android.content.Intent;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class MainActivity extends AppCompatActivity {
    private Button registerButton;
    private static final String KEY_ALIAS = "MyKeyAlias";
    private DecoratedBarcodeView barcodeView;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isRegistered()) {
            startActivity(new Intent(MainActivity.this, OTPVIEW.class));
            finish();
        }

        // 1. Inicialización
        barcodeView = findViewById(R.id.barcode_scanner);
        registerButton = findViewById(R.id.registerButton);

        // 2. Capturar pulsación botón
        registerButton.setOnClickListener(v -> {
            if (!isScanning) {
                startQRScan();
            }
        });
    }

    ////////METODO 1: VERIFICAR SI EL USUARIO ESTÁ REGISTRADO VERIFICANDO SI HAY ALGUNA CONTRASEÑA GUARDADA
    private boolean isRegistered() {
        String password = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("password", "");

        // Asegurarse de que ambos valores existen y no son cadenas vacías
        return !password.isEmpty();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////

    //////METODO 2: GESTIONAR LECTOR DE COIDGO QR///////////////////////////////////////////////////////
    private void startQRScan() {
        barcodeView.setVisibility(View.VISIBLE);
        registerButton.setVisibility(View.GONE);

        barcodeView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    String qrContent = result.getText();
                    processQRAndRegister(qrContent);
                }
            }
        });

        barcodeView.resume();
        isScanning = true;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////

    /////////METODO 3: PROCESAR DATOS QR E INICAR PROCESO DE GUARDAR LOS DATOS//////////////////////////
    private void processQRAndRegister(String qrContent) {
        // 1. Detener el escáner
        barcodeView.pause();
        isScanning = false;
        // 2. Separar el nombre de usuario y el secreto:
        String[] parametros = qrContent.split("&");

        String usuario = null;
        String clave = null;


        for (String parametro : parametros) {
            String[] par = parametro.split("=");  // Separar clave=valor
            if (par.length == 2) {
                if ("usuario".equals(par[0])) {
                    usuario = par[1];  // Asignamos el nombre de usuario
                } else if ("clave".equals(par[0])) {
                    clave = par[1];  // Asignamos la clave secreta
                }
            }
        }
        createKey();
        saveCredentials(usuario, clave);

        startActivity(new Intent(this, OTPVIEW.class));
        finish();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////METODO 4: GENERADOR CLAVE SIMETRICA////////////////////////////////////////////
    private void createKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build();

                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                keyGenerator.init(spec);
                keyGenerator.generateKey();
            }
        } catch (Exception e) {
            String errorMsg = "Error simplificado en createKey(): " + e.getClass().getSimpleName() + " - " + e.getMessage();
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }


////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////METODO 5: GUARDA LAS CREDENCIALES CIFRADAS///////////////////////////////////
    private void saveCredentials(String usuario, String password) {
        try {
            // Generar un IV único para el nombre de usuario
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);

            // Encriptar la contraseña
            Cipher cipherusuario = Cipher.getInstance("AES/GCM/NoPadding");
            Cipher cipherPassword = Cipher.getInstance("AES/GCM/NoPadding");
            cipherusuario.init(Cipher.ENCRYPT_MODE, secretKey);
            cipherPassword.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] ivusuario = cipherusuario.getIV(); // Obtener el IV del usuario, generado automaticamente
            byte[] ivPassword = cipherPassword.getIV(); // Obtener el IV del secreto, generado automáticamente
            byte[] encryptedusuario = cipherusuario.doFinal(usuario.getBytes("UTF-8"));
            byte[] encryptedPassword = cipherPassword.doFinal(password.getBytes("UTF-8"));

            if (ivusuario.length != 12) {
                Toast.makeText(this, "IV usuario fuera del estándard", Toast.LENGTH_LONG).show();
            }

            if (ivPassword.length != 12) {
                Toast.makeText(this, "IV pass fuera del estándard", Toast.LENGTH_LONG).show();
            }
            // Guardar el IV de la contraseña
            String ivusuarioBase64 = Base64.encodeToString(ivusuario, Base64.DEFAULT);
            String ivPasswordBase64 = Base64.encodeToString(ivPassword, Base64.DEFAULT);

            getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("ivusuario", ivusuarioBase64)
                    .putString("ivPassword", ivPasswordBase64)
                    .apply();

            // Convertir los datos encriptados a Base64 para almacenarlos en SharedPreferences
            String encryptedusuarioBase64 = Base64.encodeToString(encryptedusuario, Base64.DEFAULT);
            String encryptedPasswordBase64 = Base64.encodeToString(encryptedPassword, Base64.DEFAULT);

            // Guardar los datos encriptados en SharedPreferences
            getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("usuario", encryptedusuarioBase64)
                    .putString("password", encryptedPasswordBase64)
                    .apply();

        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar credenciales: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
    ////////////////////////////////////////////////////////////////////////////////////////////////////