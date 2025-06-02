/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.register;

import java.io.IOException;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import com.mycompany.register.resources.QRcrear;

/**
 *
 * @author ferran
 */
public class ControladorServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        String usuario = (String) session.getAttribute("user");
        String secret_key = (String) session.getAttribute("reply");
        session.removeAttribute("user");
        session.removeAttribute("reply");
        try {
            mostrarQR(response, usuario, secret_key);
        } catch (Exception ex) {

        }
    }
    
    private void mostrarQR(HttpServletResponse response, String usuario, String secret_key) throws IOException, Exception {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        BufferedImage qrImage = QRcrear.generateQR(usuario, secret_key);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);
        String qrBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
                + "<title>Registro OTP</title>"
                + "<link href='https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700&display=swap' rel='stylesheet'>"
                + "<style>"
                + "body { background: linear-gradient(135deg, #0f2027, #203a43, #2c5364); font-family: 'Orbitron', sans-serif; color: #00ffe7; text-align: center; padding-top: 50px; }"
                + "h2 { text-shadow: 0 0 10px #00ffe7; }"
                + "p { font-size: 1.1em; }"
                + "img { margin-top: 20px; border-radius: 12px; box-shadow: 0 0 15px #00ffe7; }"
                + "#contador { font-weight: bold; color: #ff00ff; font-size: 2em; }"
                + "</style>"
                + "<script>"
                + "let segundos = 10;"
                + "function actualizarContador() {"
                + "    if (segundos <= 0) {"
                + "        document.body.innerHTML = '<h2>Proceso completado</h2><p>Puedes cerrar esta ventana.</p>';"
                + "    } else {"
                + "        document.getElementById('contador').innerText = segundos;"
                + "        segundos--; setTimeout(actualizarContador, 1000);"
                + "    }"
                + "}"
                + "window.onload = function() {"
                + "    document.getElementById('contador').innerText = segundos;"
                + "    actualizarContador();"
                + "};"
                + "</script></head>"
                + "<body>"
                + "<h2>Activando Servicio OTP...</h2>"
                + "<p>Pulsa 'Registrar' en la app y escanea el QR que se muestra</p>"
                + "<img src='data:image/png;base64," + qrBase64 + "' alt='Código QR' width='200'/>"
                + "<p>Esta ventana se cerrará en <span id='contador'>10</span> segundos...</p>"
                + "</body></html>";

        response.setContentType("text/html");
        response.getWriter().write(html);
        response.getWriter().flush();
    }
}    