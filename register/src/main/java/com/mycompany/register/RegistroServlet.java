package com.mycompany.register;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

/**
 *
 * @author ferran
*/

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.net.SocketTimeoutException;
import javax.servlet.http.HttpSession;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.attribute.StringAttribute;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;

public class RegistroServlet extends HttpServlet {

    private static final String RADIUS_HOST = "localhost"; // Cambia por la IP de tu servidor RADIUS
    private static final String SHARED_SECRET = "secret_key"; // Clave compartida entre cliente y servidor RADIUS
    private static final int AUTH_PORT = 1812;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        String usuario = request.getParameter("usuario");
        String contrasenaenclaro = request.getParameter("contrasena");

        try {
            RadiusClient rc = new RadiusClient(RADIUS_HOST, SHARED_SECRET);
            rc.setAuthPort(AUTH_PORT);
            AccessRequest accessRequest = new AccessRequest(usuario, contrasenaenclaro);
            accessRequest.addAttribute((new StringAttribute(70, "reg"))); 
            RadiusPacket responsePacket = rc.authenticate(accessRequest);
            if (responsePacket.getPacketType() == RadiusPacket.ACCESS_ACCEPT) {
                RadiusAttribute replyMessageAttr = responsePacket.getAttribute(18);
                String replyMessage = replyMessageAttr != null ? replyMessageAttr.getAttributeValue() : "No Reply-Message";
                session.setAttribute("user", usuario);
                session.setAttribute("reply", replyMessage);
                response.sendRedirect("/register/ControladorServlet");
            } else if (responsePacket.getPacketType() == RadiusPacket.ACCESS_REJECT) {
                mostrarMensaje(response, "Autenticación fallida: Contacta con el administrador para saber más detalles");
            } else {
                mostrarMensaje(response, "Autenticación fallida: Desconocido");
            }
            rc.close();

        } catch (SocketTimeoutException e) {
            mostrarMensaje(response, "Autenticación fallida: "
                    + "servidor Radius apagado");
        } catch (Exception e) {
            mostrarMensaje(response, "Autenticación fallida: "
                    + "servidor Radius no disponible");
        } 
        
    }    

    private void mostrarMensaje(HttpServletResponse response, String mensaje) throws IOException {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
                + "<style>body { background: #000; color: #fff; font-family: 'Orbitron', sans-serif; text-align: center; margin-top: 20vh; }"
                + "h2 { color: #ff00ff; text-shadow: 0 0 10px #ff00ff; }</style>"
                + "<link href='https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700&display=swap' rel='stylesheet'>"
                + "</head><body><h2>" + mensaje + "</h2></body></html>";
        response.setContentType("text/html");
        response.getWriter().println(html);
    }
}
































/*
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.imageio.ImageIO;
import com.mycompany.register.resources.QRcrear;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class RegistroServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/dbtfg";
    private static final String DB_USER = "ferran";
    private static final String DB_PASSWORD = "Fguiñat2025*";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String usuario = request.getParameter("usuario");
        String password = request.getParameter("contrasena");
        
        // Escapar comillas simples para prevenir inyección SQL básica
        usuario = usuario.replace("'", "''");
        password = password.replace("'", "''");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement()) {
                
                String query = "SELECT estado FROM usuarios WHERE usuario = '" + usuario 
                            + "' AND password = '" + password + "'";
                
                try (ResultSet rs = stmt.executeQuery(query)) {
                    if (rs.next()) {
                        String estado = rs.getString("estado");
                        if ("pre-registrado".equals(estado)) {
                            System.out.println("El usario no está´ resgistrado");
                            mostrarQR(response, usuario);
                        } else {
                            mostrarMensaje(response, "Usuario ya registrado");
                        }
                    } else {
                        mostrarMensaje(response, "Credenciales incorrectas");
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(RegistroServlet.class.getName()).log(Level.SEVERE, null, e);
            mostrarMensaje(response, "Error en el servidor: " + e.getMessage());
        }
    }
    
    private void mostrarQR(HttpServletResponse response, String usuario) throws IOException {
        
    
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

        String updateQuery = "UPDATE usuarios SET estado = 'registrado' WHERE usuario = '" + usuario.replace("'", "''") + "'";
        stmt.executeUpdate(updateQuery);
        
        BufferedImage qrImage = QRcrear.generateQR(usuario);
        
        // Convertir imagen QR a Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);
        String qrBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

        // HTML con contador
        String html = "<!DOCTYPE html><html><head>"
                + "<title>Registro OTP</title>"
                + "<script>"
                + "var segundos = 10;"
                + "function actualizarContador() {"
                + "  document.getElementById('contador').innerHTML = segundos;"
                + "  if(segundos <= 0) { document.body.innerHTML = '<h2>Proceso completado</h2><p>Puede cerrar esta ventana</p>'; }"
                + "  else { segundos--; setTimeout(actualizarContador, 1000); }"
                + "}"
                + "window.onload = function() { actualizarContador(); };"
                + "</script>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; text-align: center; margin-top: 50px; }"
                + "</style>"
                + "</head><body>"
                + "<h2>Usuario pre-registrado</h2>"
                + "<p>Escanea este código QR con tu app OTP:</p>"
                + "<img src='data:image/png;base64," + qrBase64 + "' alt='Código QR'/>"
                + "<p>Esta ventana se cerrará en <span id='contador'>10</span> segundos...</p>"
                + "</body></html>";

        response.setContentType("text/html");
        response.getWriter().println(html);
        
    } catch (Exception e) {
        throw new IOException("Error al generar QR", e);
    }

    }   catch (ClassNotFoundException ex) {
            Logger.getLogger(RegistroServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    


    private void mostrarMensaje(HttpServletResponse response, String mensaje) throws IOException {
        response.setContentType("text/html");
        response.getWriter().println("<h2>" + mensaje + "</h2>");
    }
}







*/




