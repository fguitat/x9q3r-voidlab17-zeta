package com.example.tfg;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import javax.net.ssl.*;
import java.io.*;
import java.net.URI;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SecureWebSocketclient extends WebSocketClient {
    private final String mensajeInicial;
    private NotificationService service;
    public SecureWebSocketclient(URI serverUri, SSLSocketFactory sslSocketFactory, String mensajeInicial, NotificationService service) {
        super(serverUri);
        this.setSocketFactory(sslSocketFactory);
        this.mensajeInicial = mensajeInicial;
        this.service = service;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        service.setConectado(true);  // indica que se ha conectado con éxito
        send(mensajeInicial);
    }

    @Override
    public void onMessage(String message) {
        service.mostrarNotificacion(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        service.setConectado(false);  // marca como desconectado
        service.iniciarReconexionConDelay();
    }

    @Override
    public void onError(Exception ex) {
        service.setConectado(false);  // marca como desconectado
        service.iniciarReconexionConDelay();
    }

    public static SSLSocketFactory getSocketFactory(InputStream p12Stream, String p12Password, InputStream caInput) throws Exception {
        // Cargar clave cliente
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        clientStore.load(p12Stream, p12Password.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
        kmf.init(clientStore, p12Password.toCharArray());

        // Cargar CA
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(caInput);
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        trustStore.setCertificateEntry("caCert", caCert);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(trustStore);

        // Crear contexto SSL
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return context.getSocketFactory();
    }
}
