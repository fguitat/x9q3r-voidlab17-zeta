package com.example.tfg;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

public class finalActivity extends AppCompatActivity {

    private Button btnAceptar, btnCancelar;
    private Context context = this;
    public static boolean isActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);

        btnAceptar = findViewById(R.id.btnAceptar);
        btnCancelar = findViewById(R.id.btnCancelar);

        btnAceptar.setOnClickListener(v -> {
            clearSharedPreferences();
            Intent serviceIntent = new Intent(context, NotificationService.class);
            stopService(serviceIntent);
            Intent intent = new Intent(finalActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            isActive=true;
            startActivity(intent);
            finish();

        });

        btnCancelar.setOnClickListener(v -> {
            startActivity(new Intent(finalActivity.this, OTPVIEW.class));
            finish();
        });
    }
    private void clearSharedPreferences() {
        getApplicationContext().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }
}
