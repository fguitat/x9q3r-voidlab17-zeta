/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.register;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.*;
/**
 *
 * @author ferran
 */
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang='es'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Registro de Usuario</title>");
        out.println("<style>");
        out.println("@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700&display=swap');");
        out.println("body { background: radial-gradient(circle at center, #0f2027, #203a43, #2c5364); color: #00ffe7; font-family: 'Orbitron', sans-serif; display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh; margin: 0; }");
        out.println("h2 { text-shadow: 0 0 10px #00ffe7; margin-bottom: 30px; }");
        out.println("form { background-color: rgba(0, 0, 0, 0.4); padding: 40px; border-radius: 15px; box-shadow: 0 0 15px #00ffe7; backdrop-filter: blur(5px); max-width: 400px; margin: auto; }");
        out.println("label { display: block; margin-bottom: 8px; font-size: 14px; text-transform: uppercase; color: #00ffff; }");
        out.println("input { width: 100%; padding: 10px; margin-bottom: 20px; border: 1px solid #00ffff; border-radius: 8px; background: #111; color: #00ffe7; font-size: 14px; outline: none; }");
        out.println("input:focus { border-color: #00ffe7; box-shadow: 0 0 8px #00ffe7; }");
        out.println("button { width: 100%; padding: 12px; border: none; border-radius: 8px; background-color: #00ffe7; color: #000; font-weight: bold; font-size: 14px; cursor: pointer; transition: 0.3s ease-in-out; }");
        out.println("button:hover { background-color: #00ffff; box-shadow: 0 0 15px #00ffe7; transform: scale(1.05); }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Activación de Servicio OTP</h2>");
        out.println("<form action='RegistroServlet' method='post'>");
        out.println("<label for='usuario'>Usuario</label>");
        out.println("<input type='text' name='usuario' id='usuario' required>");
        out.println("<label for='contrasena'>Contraseña</label>");
        out.println("<input type='password' name='contrasena' id='contrasena' required>");
        out.println("<button type='submit'>Activar Servicio OTP</button>");
        out.println("</form>");
        out.println("</body>");
        out.println("</html>");
    }
}
