/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.otpserver;

import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;
import org.tinyradius.attribute.StringAttribute;
import java.io.IOException;
/**
 *
 * @author FERRAN
 */

public class ValidacionOTPServlet extends HttpServlet {
    private static final String RADIUS_HOST = "192.168.0.224";
    private static final String SHARED_SECRET = "secret_key";
    private static final int AUTH_PORT = 1812;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Control caché
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, private, no-transform");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", -1);
        response.setHeader("Vary", "*");
        response.setHeader("Clear-Site-Data", "\"cache\", \"storage\", \"executionContexts\"");

        String timestamp = String.valueOf(System.currentTimeMillis());

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"es\">");
            out.println("<head>");
            out.println("  <meta charset=\"UTF-8\">");
            out.println("  <meta http-equiv=\"Cache-Control\" content=\"no-store, no-cache, must-revalidate, max-age=0\">");
            out.println("  <meta http-equiv=\"Pragma\" content=\"no-cache\">");
            out.println("  <meta http-equiv=\"Expires\" content=\"-1\">");
            out.println("  <meta http-equiv=\"Clear-Site-Data\" content=\"cache,storage,executionContexts\">");
            out.println("  <title>Verificación en Dos Pasos</title>");
            out.println("  <link href=\"https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap\" rel=\"stylesheet\">");
            out.println("  <style>");
            out.println("    body {");
            out.println("      font-family: 'Roboto', sans-serif;");
            out.println("      background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);");
            out.println("      display: flex;");
            out.println("      justify-content: center;");
            out.println("      align-items: center;");
            out.println("      height: 100vh;");
            out.println("      margin: 0;");
            out.println("    }");
            out.println("    .otp-box {");
            out.println("      background-color: white;");
            out.println("      padding: 25px 30px;");
            out.println("      border-radius: 16px;");
            out.println("      box-shadow: 0 8px 20px rgba(0,0,0,0.15);");
            out.println("      width: 300px;");
            out.println("      text-align: center;");
            out.println("    }");
            out.println("    .otp-box h2 {");
            out.println("      margin-bottom: 20px;");
            out.println("      color: #333;");
            out.println("    }");
            out.println("    .otp-box p {");
            out.println("      color: #666;");
            out.println("      margin-bottom: 25px;");
            out.println("      font-size: 14px;");
            out.println("    }");
            out.println("    .otp-inputs {");
            out.println("      display: flex;");
            out.println("      justify-content: space-between;");
            out.println("      margin-bottom: 20px;");
            out.println("    }");
            out.println("    .otp-inputs input {");
            out.println("      width: 40px;");
            out.println("      height: 40px;");
            out.println("      text-align: center;");
            out.println("      font-size: 14px;");
            out.println("      border: none;");
            out.println("      border-radius: 8px;");
            out.println("      outline: none;");
            out.println("      background-color: #f1f1f1;");
            out.println("      font-family: 'Roboto', sans-serif;");
            out.println("      color: #333;");
            out.println("    }");
            out.println("    .otp-inputs input:focus {");
            out.println("      background-color: #e0e0e0;");
            out.println("      box-shadow: 0 0 5px rgba(37, 117, 252, 0.3);");
            out.println("    }");
            out.println("    .otp-box button {");
            out.println("      width: 100%;");
            out.println("      padding: 10px;");
            out.println("      border: none;");
            out.println("      background-color: #2575fc;");
            out.println("      color: white;");
            out.println("      font-size: 16px;");
            out.println("      border-radius: 8px;");
            out.println("      cursor: pointer;");
            out.println("      transition: background-color 0.3s ease;");
            out.println("    }");
            out.println("    .otp-box button:hover {");
            out.println("      background-color: #1a5ed8;");
            out.println("    }");
            out.println("    .error {");
            out.println("      color: red;");
            out.println("      margin-top: 10px;");
            out.println("      font-size: 14px;");
            out.println("    }");
            out.println("    .resend-link {");
            out.println("      margin-top: 15px;");
            out.println("      font-size: 13px;");
            out.println("    }");
            out.println("    .resend-link a {");
            out.println("      color: #2575fc;");
            out.println("      text-decoration: none;");
            out.println("    }");
            out.println("  </style>");
            out.println("  <script>");
            out.println("    document.addEventListener('DOMContentLoaded', () => {");
            out.println("      const inputs = document.querySelectorAll('.otp-inputs input');");
            out.println("      inputs.forEach((input, index) => {");
            out.println("        input.addEventListener('input', () => {");
            out.println("          if (input.value.length === 1 && index < inputs.length - 1) {");
            out.println("            inputs[index + 1].focus();");
            out.println("          }");
            out.println("        });");
            out.println("      });");
            out.println("    });");
            out.println("  </script>");
            out.println("</head>");
            out.println("<body>");
            out.println("  <div class=\"otp-box\">");
            out.println("    <h2>Verificación de Seguridad</h2>");
            out.println("    <p>Introduce el código OTP de 6 dígitos que se muestra en el dispositivo</p>");
            // Error por si está presente en sesión
            HttpSession session = request.getSession(false);
            String error = session != null ? (String) session.getAttribute("error") : null;
            if (error != null) {
                session.removeAttribute("error");
            }
            // Uso timestamp
            String inputSuffix = timestamp;
            out.println("    <form method=\"POST\" action=\"ValidacionOTPServlet\" autocomplete=\"off\">");
            out.println("      <div class=\"otp-inputs\">");
            // Concatenar 6 dígitos
            out.println("        <input type=\"text\" name=\"otp1-" + inputSuffix + "\" maxlength=\"1\" pattern=\"[0-9]\" required autocomplete=\"new-password\">");
            out.println("        <input type=\"text\" name=\"otp2-" + inputSuffix + "\" maxlength=\"1\" pattern=\"[0-9]\" required autocomplete=\"new-password\">");
            out.println("        <input type=\"text\" name=\"otp3-" + inputSuffix + "\" maxlength=\"1\" pattern=\"[0-9]\" required autocomplete=\"new-password\">");
            out.println("        <input type=\"text\" name=\"otp4-" + inputSuffix + "\" maxlength=\"1\" pattern=\"[0-9]\" required autocomplete=\"new-password\">");
            out.println("        <input type=\"text\" name=\"otp5-" + inputSuffix + "\" maxlength=\"1\" pattern=\"[0-9]\" required autocomplete=\"new-password\">");
            out.println("        <input type=\"text\" name=\"otp6-" + inputSuffix + "\" maxlength=\"1\" pattern=\"[0-9]\" required autocomplete=\"new-password\">");
            out.println("      </div>");
            out.println("      <button type=\"submit\">Verificar</button>");
            out.println("      <div class=\"error\">" + (error != null ? error : "") + "</div>");
            out.println("    </form>");
            out.println("  </div>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Control caché
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, private, no-transform");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", -1);
        String username = null;

        HttpSession session = request.getSession(false);
        if (session != null) {
            username = (String) session.getAttribute("username");
            session.invalidate();
        }
        session = request.getSession(true);

        StringBuilder otp = new StringBuilder();
        
        String inputSuffix = request.getParameterNames().nextElement().toString().split("-")[1];

        for (int i = 1; i <= 6; i++) {
            String digit = request.getParameter("otp" + i + "-" + inputSuffix);
            if (digit == null || digit.isEmpty() || !digit.matches("[0-9]")) {
                session.setAttribute("error", "Por favor ingresa los 6 dígitos");
                response.sendRedirect("ValidacionOTPServlet?t=" + System.currentTimeMillis());
                return;
            }
            otp.append(digit);
        }

        // VAlidación OTP
        boolean isValid = validateOTP(otp.toString(), username);

        if (isValid) {
            response.sendRedirect("PaginaAdmServlet");
        } else {
            session.setAttribute("error", "Código OTP inválido");
            response.sendRedirect("ValidacionOTPServlet?t=" + System.currentTimeMillis());
        }
    }

    private boolean validateOTP(String otp, String usuario) {
        try {             
            RadiusClient rc = new RadiusClient(RADIUS_HOST, SHARED_SECRET);
            rc.setAuthPort(AUTH_PORT);          
            AccessRequest enviarOtp = new AccessRequest(usuario, otp);
            enviarOtp.addAttribute(new StringAttribute(66, "otp"));
            RadiusPacket respuestarad = rc.communicate(enviarOtp, AUTH_PORT);

            if (respuestarad.getPacketType() == RadiusPacket.ACCESS_ACCEPT) {
                return true;   
            } else {
                return false;
            }
        } catch (Exception e){
                return false;
        } 
    }
}



/*
public class ValidacionOTPServlet extends HttpServlet {
    private static final String RADIUS_HOST = "192.168.0.224"; // Cambia por la IP de tu servidor RADIUS
    private static final String SHARED_SECRET = "secret_key"; // Clave compartida entre cliente y servidor RADIUS
    private static final int AUTH_PORT = 1812;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
        // Desactivar caché
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        PrintWriter out = response.getWriter();
        System.out.println("Se hace un GET");
                
        response.setContentType("text/html;charset=UTF-8");
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"es\">");
        out.println("<head>");
        out.println("  <meta charset=\"UTF-8\">");
        out.println("  <title>Verificación en Dos Pasos</title>");
        out.println("  <link href=\"https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap\" rel=\"stylesheet\">");
        out.println("  <style>");
        out.println("    body {");
        out.println("      font-family: 'Roboto', sans-serif;");
        out.println("      background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);");
        out.println("      display: flex;");
        out.println("      justify-content: center;");
        out.println("      align-items: center;");
        out.println("      height: 100vh;");
        out.println("      margin: 0;");
        out.println("    }");
        out.println("    .otp-box {");
        out.println("      background-color: white;");
        out.println("      padding: 25px 30px;");
        out.println("      border-radius: 16px;");
        out.println("      box-shadow: 0 8px 20px rgba(0,0,0,0.15);");
        out.println("      width: 300px;");
        out.println("      text-align: center;");
        out.println("    }");
        out.println("    .otp-box h2 {");
        out.println("      margin-bottom: 20px;");
        out.println("      color: #333;");
        out.println("    }");
        out.println("    .otp-box p {");
        out.println("      color: #666;");
        out.println("      margin-bottom: 25px;");
        out.println("      font-size: 14px;");
        out.println("    }");
        out.println("    .otp-inputs {");
        out.println("      display: flex;");
        out.println("      justify-content: space-between;");
        out.println("      margin-bottom: 20px;");
        out.println("    }");
        out.println("    .otp-inputs input {");
        out.println("      width: 40px;");
        out.println("      height: 40px;");
        out.println("      text-align: center;");
        out.println("      font-size: 18px;");
        out.println("      border: 1px solid #ddd;");
        out.println("      border-radius: 8px;");
        out.println("      outline: none;");
        out.println("    }");
        out.println("    .otp-inputs input:focus {");
        out.println("      border-color: #2575fc;");
        out.println("      box-shadow: 0 0 5px rgba(37, 117, 252, 0.3);");
        out.println("    }");
        out.println("    .otp-box button {");
        out.println("      width: 100%;");
        out.println("      padding: 10px;");
        out.println("      border: none;");
        out.println("      background-color: #2575fc;");
        out.println("      color: white;");
        out.println("      font-size: 16px;");
        out.println("      border-radius: 8px;");
        out.println("      cursor: pointer;");
        out.println("      transition: background-color 0.3s ease;");
        out.println("    }");
        out.println("    .otp-box button:hover {");
        out.println("      background-color: #1a5ed8;");
        out.println("    }");
        out.println("    .error {");
        out.println("      color: red;");
        out.println("      margin-top: 10px;");
        out.println("      font-size: 14px;");
        out.println("    }");
        out.println("    .resend-link {");
        out.println("      margin-top: 15px;");
        out.println("      font-size: 13px;");
        out.println("    }");
        out.println("    .resend-link a {");
        out.println("      color: #2575fc;");
        out.println("      text-decoration: none;");
        out.println("    }");
        out.println("  </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("  <div class=\"otp-box\">");
        out.println("    <h2>Verificación de Seguridad</h2>");
        out.println("    <p>Introduce el código OTP de 6 dígitos enviado a tu dispositivo</p>");
        out.println("    <form id=\"otpForm\" method=\"GET\" action=\"PaginaAdmServlet\">");
        out.println("      <div class=\"otp-inputs\">");
        out.println("        <input type=\"text\" name=\"otp1\" maxlength=\"1\" pattern=\"[0-9]\" required>");
        out.println("        <input type=\"text\" name=\"otp2\" maxlength=\"1\" pattern=\"[0-9]\" required>");
        out.println("        <input type=\"text\" name=\"otp3\" maxlength=\"1\" pattern=\"[0-9]\" required>");
        out.println("        <input type=\"text\" name=\"otp4\" maxlength=\"1\" pattern=\"[0-9]\" required>");
        out.println("        <input type=\"text\" name=\"otp5\" maxlength=\"1\" pattern=\"[0-9]\" required>");
        out.println("        <input type=\"text\" name=\"otp6\" maxlength=\"1\" pattern=\"[0-9]\" required>");
        out.println("      </div>");
        out.println("      <input type=\"hidden\" id=\"fullOtp\" name=\"otp\">");
        out.println("      <button type=\"submit\">Verificar</button>");
        out.println("      <div class=\"error\" id=\"errorMsg\"></div>");
        out.println("    </form>");
        out.println("  </div>");
        out.println("  <script>");
        out.println("    const inputs = document.querySelectorAll('.otp-inputs input');");
        out.println("    inputs.forEach((input, index) => {");
        out.println("      input.addEventListener('input', (e) => {");
        out.println("        if (e.target.value.length === 1) {");
        out.println("          if (index < inputs.length - 1) {");
        out.println("            inputs[index + 1].focus();");
        out.println("          }");
        out.println("        }");
        out.println("      });");
        out.println("      input.addEventListener('keydown', (e) => {");
        out.println("        if (e.key === 'Backspace' && e.target.value === '' && index > 0) {");
        out.println("          inputs[index - 1].focus();");
        out.println("        }");
        out.println("      });");
        out.println("    });");
        out.println("    document.getElementById('otpForm').addEventListener('submit', (e) => {");
        out.println("      const otp = Array.from(inputs).map(input => input.value).join('');");
        out.println("      document.getElementById('fullOtp').value = otp;");
        out.println("      if (otp.length !== 6) {");
        out.println("        e.preventDefault();");
        out.println("        document.getElementById('errorMsg').textContent = 'Por favor ingresa los 6 dígitos';");
        out.println("      }");
        out.println("    });");
        out.println("  </script>");
        out.println("</body>");
        out.println("</html>");

    }
    
    
     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        HttpSession session = request.getSession(false);
        String codigo = request.getParameter("otp");
        String username = (String) session.getAttribute("username");

        try {             
            RadiusClient rc = new RadiusClient(RADIUS_HOST, SHARED_SECRET);
            rc.setAuthPort(AUTH_PORT);          
            AccessRequest enviarOtp = new AccessRequest(username, codigo);
            enviarOtp.addAttribute(new StringAttribute(66, "otp"));
            RadiusPacket respuestarad = rc.communicate(enviarOtp, AUTH_PORT);

            if (respuestarad.getPacketType() == RadiusPacket.ACCESS_ACCEPT) {
                session.setAttribute("autenticado", true);
                response.sendRedirect("PaginaAdmServlet?r=" + System.currentTimeMillis());
                return;


            } else {
                // OTP incorrecto: Invalidar sesión para evitar ataques
                session.invalidate();
                response.getWriter().println("Código OTP incorrecto. Vuelve a intentarlo.");
                return;
            }


        } catch (Exception e){
                session.invalidate();
                response.getWriter().println("Error en la autenticación. Vuelve a intentarlo más tarde");
                return;

        } 
    }    
}
*/