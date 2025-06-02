package com.example.tfg;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.NotificationChannel;
import android.os.Build;
import android.app.Notification;
import android.os.PowerManager;
import io.github.ssrack.javatotp.TOTPUtility;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import javax.net.ssl.SSLSocketFactory;

public class NotificationService extends Service {
    private SecureWebSocketclient webSocketClient;
    private static final String CHANNEL_ID = "push_notifications";
    private static String usuario;
    private Handler reconnectionHandler = new Handler(Looper.getMainLooper());
    private Runnable reconnectionRunnable;
    private boolean conectado = false;
    private PowerManager.WakeLock wakeLock;
    private static final int NOTIFICATION_ID = 1001; // ID único para la notificación de servicio


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::NotificationWakeLock");
            wakeLock.setReferenceCounted(false); // Para evitar errores si se llama varias veces
        }
        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }

        if (intent != null && "RNOTIFICACION".equals(intent.getAction())) {
            String respuesta = intent.getStringExtra("respuesta");
            int id = intent.getIntExtra("notification_id", -1);

            try {
                Log.d("hey","Holaa");
                JSONObject json = new JSONObject();
                json.put("tipo", "respuesta");
                json.put("id", id);
                json.put("respuesta", respuesta);
                json.put("code", TOTPUtility.getOTPCode(PasswordUtils.obtenerPass(getApplicationContext())));
                enviarMensajeWebSocket(json.toString());

            } catch (JSONException e) {
                enviarMensajeWebSocket("NO");
            }

            return START_NOT_STICKY;

        } else {
            try {
                // Recuperar los IVs de SharedPreferences
                String ivUsernameBase64 = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        .getString("ivusuario", "");

                byte[] ivUsername = Base64.decode(ivUsernameBase64, Base64.DEFAULT);

                // Recuperar las credenciales encriptadas
                String encryptedUsernameBase64 = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        .getString("usuario", "");

                byte[] encryptedUsername = Base64.decode(encryptedUsernameBase64, Base64.DEFAULT);

                // Desencriptar las credenciales
                usuario = decryptData.desencriptar(encryptedUsername, ivUsername);

                requestBatteryOptimizationExemption();
                InputStream p12 = getResources().openRawResource(R.raw.client2);  // client.p12 en res/raw
                InputStream ca = getResources().openRawResource(R.raw.ca);        // ca.crt en res/raw
                String password = "ÑaoPao";  // tu contraseña del .p12

                SSLSocketFactory factory = SecureWebSocketclient.getSocketFactory(p12, password, ca);
                URI uri = new URI("wss://192.168.0.224:8443");

                webSocketClient = new SecureWebSocketclient(uri, factory, "{\"usuario\":\"" + usuario + "\"}", this);
                createNotificationChannel();
                startForegroundService();
                webSocketClient.connect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (webSocketClient != null) {
            conectado = true;
            Log.d("DentroOndestroy", "sfiowefwie");
            webSocketClient.close();
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void setConectado(boolean valor) {
        conectado = valor;
    }

    public void iniciarReconexionConDelay() {
        if (reconnectionRunnable != null) return; // Ya se está reintentando

        reconnectionRunnable = new Runnable() {
            @Override
            public void run() {
                String prueba = getApplicationContext().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        .getString("usuario","");
                try {
                    String otp = TOTPUtility.getOTPCode(PasswordUtils.obtenerPass(getApplicationContext()));
                    if (!conectado) {
                        conectarWebSocket();
                        reconnectionRunnable = null; // Limpia para permitir futuros reintentos
                    }
                } catch (NullPointerException ex){
                    //SE DETECTA EL ERROR PERO NO SE HACE NADA
                }
            }
        };

        reconnectionHandler.postDelayed(reconnectionRunnable, 5000); // Reintento cada 5 segundos
    }
    public void conectarWebSocket() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            return; // Ya está conectado
        } else {
            try {
                InputStream p12 = getResources().openRawResource(R.raw.client2);
                InputStream ca = getResources().openRawResource(R.raw.ca);
                String password = "ÑaoPao";

                SSLSocketFactory factory = SecureWebSocketclient.getSocketFactory(p12, password, ca);
                URI uri = new URI("wss://192.168.0.224:8443");

                String usuario = PasswordUtils.obtenerUser(getApplicationContext());
                webSocketClient = new SecureWebSocketclient(uri, factory, "{\"usuario\":\"" + usuario + "\"}", this);
                webSocketClient.connect();

            } catch (ConnectException ex) {
                Log.d("ERROR", "Detectado excepción");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void mostrarNotificacion(String mensaje) {
        try {
            JSONObject json = new JSONObject(mensaje);
            String mostrar = json.getString("mensaje");

            int idNotificacion = json.getInt("id_mensaje");
            Log.d("ZMQ", "Entra al sistema el usuario: " + mostrar);
            int requestCodeSi = (int) System.currentTimeMillis();
            int requestCodeNo = requestCodeSi + 1;


            PendingIntent pendingIntentSi = crearIntentRespuesta(requestCodeSi, "SI", idNotificacion);
            PendingIntent pendingIntentNo = crearIntentRespuesta(requestCodeNo, "NO", idNotificacion);

            // Construcción de la notificación
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Nueva notificación")
                    .setContentText(mostrar)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.notificacion)
                    .setOngoing(true)
                    .addAction(0, "Sí", pendingIntentSi)
                    .addAction(0, "No", pendingIntentNo)
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(idNotificacion, builder.build());

        } catch (Exception e) {
            Log.e("Notificación", "Error al mostrar la notificación", e);
        }
    }

    private PendingIntent crearIntentRespuesta(int requestCode, String respuesta, int notifid) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("respuesta", respuesta);
        intent.putExtra("notification_id", notifid);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }
        return PendingIntent.getBroadcast(this, requestCode, intent, flags);
    }

    private void startForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Servicio de Notificaciones")
                .setContentText("Escuchando mensajes del servidor...")
                .setSmallIcon(R.drawable.notificacion)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification); // Mantiene el servicio activo en primer plano
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Notificaciones", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Canal de notificaciones para mensajes push");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void enviarMensajeWebSocket(String mensaje) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(mensaje);
        } else {
            Log.w("NotificationService", "WebSocket no está abierto, no se pudo enviar mensaje");
        }
    }

    public void requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // IMPORTANTE para lanzarlo desde un Service
                startActivity(intent); // 💥 Aquí es donde se muestra el diálogo
            }
        }
    }
}