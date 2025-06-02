# =============================================
# Configuración agresiva de ofuscación y optimización
# =============================================
-optimizationpasses 5          # Más pasos de optimización
-allowaccessmodification       # Permite reestructurar paquetes
-overloadaggressively          # Ofusca métodos con sobrecarga
-repackageclasses ''           # Reescribe nombres de paquetes
-useuniqueclassmembernames     # Evita colisiones en nombres
-flattenpackagehierarchy ''    # Compacta jerarquía de paquetes
-dontusemixedcaseclassnames    # Evita mayúsculas en clases ofuscadas

# =============================================
# Remover atributos de depuración (stack traces más limpios)
# =============================================
-keepattributes Exceptions, InnerClasses, Signature
-renamesourcefileattribute SourceFile  # Oculta nombres de archivos originales

# =============================================
# Preservar componentes esenciales de Android
# =============================================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View

# =============================================
# Librerías específicas que necesitan preservarse
# =============================================

# Retrofit & Gson (necesarios para serialización JSON)
-keep class com.google.gson.** { *; }
-keep class com.squareup.retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ZXing (QR scanning)
-keep class com.journeyapps.** { *; }

# Java-WebSocket (WebSockets)
-keep class org.java_websocket.** { *; }

# WorkManager (tareas en segundo plano)
-keep class androidx.work.** { *; }

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }

# TOTP (autenticación 2FA)
-keep class io.github.ssrack.** { *; }

# DataStore (preferencias)
-keep class androidx.datastore.** { *; }

# =============================================
# Evitar warnings innecesarios (opcional)
# =============================================
-dontwarn com.squareup.retrofit2.**
-dontwarn org.java_websocket.**
-dontwarn kotlinx.coroutines.**

# Ignorar clases de AWT/javax.imageio (no soportadas en Android)
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn org.mapstruct.ap.spi.**
-dontwarn lombok.launch.**

#Evitar ofuscar WaveLock

# Mantén solo los métodos de WakeLock que usas
-keep class android.os.PowerManager$WakeLock {
    void acquire(...);
    void release();
    boolean isHeld();
    void setReferenceCounted(boolean);
}

# Cuidado: si usas reflección o librerías de terceros, debes añadir excepciones para que no falle
# Por ejemplo, para Retrofit, Room, Gson, Firebase, Jetpack Navigation, etc., debes añadir:
# -keep class com.google.gson.** { *; }

# Opcional: elimina recursos no utilizados
# (esto se activa desde el build.gradle, no aquí)



# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile