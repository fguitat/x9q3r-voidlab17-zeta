package com.example.tfg;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import io.github.ssrack.javatotp.TOTPUtility;


public class OTPVIEW extends AppCompatActivity {

    private TextView otpView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuButton;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpview);
        iniciarServicioNotificaciones();

        otpView = findViewById(R.id.OTPView);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.menu_button);

        // Abre el menú cuando se hace clic en el botón de hamburguesa
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Manejar la opción del menú
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_cambiar_usuario) {
                    startActivity(new Intent(OTPVIEW.this, finalActivity.class));
                    finish();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (PasswordUtils.obtenerPass(getApplicationContext()) != null &&PasswordUtils.obtenerUser(getApplicationContext()) != null) {
                    String otp = TOTPUtility.getOTPCode(PasswordUtils.obtenerPass(getApplicationContext()));
                    otpView.setText(otp);
                    handler.postDelayed(this, 30000); // Ejecuta cada 30 segundos (30000 ms)
                }
            }

        };
        handler.post(runnable);
    }
    private void iniciarServicioNotificaciones() {
        if (isServiceRunning(NotificationService.class)) {
            return;
        }

        Intent serviceIntent = new Intent(this, NotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }


    private boolean isServiceRunning(Class<?> serviceClass) {
        android.app.ActivityManager manager = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
