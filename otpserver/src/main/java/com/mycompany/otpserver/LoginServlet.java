/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.otpserver;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author FERRAN
 */

import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import org.tinyradius.attribute.StringAttribute;
import org.tinyradius.util.RadiusException;

public class LoginServlet extends HttpServlet {

    private static final String RADIUS_HOST = "192.168.0.224"; // Cambia por la IP de tu servidor RADIUS
    private static final String SHARED_SECRET = "secret_key"; // Clave compartida entre cliente y servidor RADIUS
    private static final int AUTH_PORT = 1812;
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"es\">");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<title>Iniciar Sesión</title>");
        out.println("<link href=\"https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap\" rel=\"stylesheet\">");
        out.println("<style>");
        out.println("body {");
        out.println("  font-family: 'Roboto', sans-serif;");
        out.println("  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);");
        out.println("  display: flex;");
        out.println("  justify-content: center;");
        out.println("  align-items: center;");
        out.println("  height: 100vh;");
        out.println("  margin: 0;");
        out.println("}");
        out.println(".login-box {");
        out.println("  background-color: white;");
        out.println("  padding: 25px 30px;");
        out.println("  border-radius: 16px;");
        out.println("  box-shadow: 0 8px 20px rgba(0,0,0,0.15);");
        out.println("  width: 300px;");
        out.println("}");
        out.println(".login-box h2 {");
        out.println("  text-align: center;");
        out.println("  margin-bottom: 20px;");
        out.println("  color: #333;");
        out.println("}");
        out.println(".login-box input[type=\"text\"],");
        out.println(".login-box input[type=\"password\"] {");
        out.println("  width: 100%;");
        out.println("  padding: 10px;");
        out.println("  margin-bottom: 15px;");
        out.println("  border: none;");
        out.println("  border-radius: 8px;");
        out.println("  background-color: #f1f1f1;");
        out.println("  font-size: 14px;");
        out.println("}");
        out.println(".login-box input:focus {");
        out.println("  outline: none;");
        out.println("  background-color: #e0e0e0;");
        out.println("}");
        out.println(".login-box button {");
        out.println("  width: 100%;");
        out.println("  padding: 10px;");
        out.println("  border: none;");
        out.println("  background-color: #2575fc;");
        out.println("  color: white;");
        out.println("  font-size: 16px;");
        out.println("  border-radius: 8px;");
        out.println("  cursor: pointer;");
        out.println("  transition: background-color 0.3s ease;");
        out.println("}");
        out.println(".login-box button:hover {");
        out.println("  background-color: #1a5ed8;");
        out.println("}");
        out.println(".error {");
        out.println("  color: red;");
        out.println("  text-align: center;");
        out.println("  margin-top: 10px;");
        out.println("  font-size: 14px;");
        out.println("}");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"login-box\">");
        out.println("<h2>Iniciar Sesión</h2>");
        out.println("<form id=\"loginForm\" method=\"POST\" action=\"LoginServlet\">");
        out.println("<input type=\"text\" name=\"username\" placeholder=\"Usuario\" required>");
        out.println("<input type=\"password\" name=\"password\" placeholder=\"Contraseña\" required>");
        out.println("<button type=\"submit\">Entrar</button>");
        out.println("</form>");
        out.println("<div class=\"error\" id=\"errorMsg\"></div>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Establecer cabeceras de control de caché
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        authenticateWithRadius(username, password, request, response);
        
        
    }
    
    private void authenticateWithRadius(String username, String password, HttpServletRequest req, HttpServletResponse res) throws SocketTimeoutException, IOException {
            // Establecer cabeceras de control de caché
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        
        try {
            RadiusClient rc = new RadiusClient(RADIUS_HOST, SHARED_SECRET);
            rc.setAuthPort(AUTH_PORT);          
            AccessRequest accessRequest = new AccessRequest(username, password);
            accessRequest.addAttribute(new StringAttribute(67, "preotp"));
            RadiusPacket response = rc.authenticate(accessRequest);
            

            if (response.getPacketType() == RadiusPacket.ACCESS_CHALLENGE) {
                
                HttpSession session = req.getSession(true);
                session.setAttribute("username", username);
                res.sendRedirect("ValidacionOTPServlet");
                return;
            }else {
                out.println("<script>");
                out.println("alert('Usuario o contraseña incorrectos');");
                out.println("window.location.href = '/otpserver/LoginServlet';");
                out.println("</script>");
            }    
        } catch (SocketTimeoutException e) {
                out.println("<script>");
                out.println("alert('Servidor RADIUS APAGADO. Contacta con el administrador');");
                out.println("window.location.href = '/otpserver/LoginServlet';");
                out.println("</script>");
        } catch (RadiusException ex) {
            Logger.getLogger(LoginServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException exx){
            
        }
    }
}

