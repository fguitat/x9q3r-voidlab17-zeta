/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.firstpage;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 *
 * @author ferran
 */

public class ControlServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        Boolean autorizado = (Boolean) request.getSession().getAttribute("enpanel");
        String dato = (String) request.getSession().getAttribute("variable");
        if (dato == null && autorizado == null){
            HttpSession sesion = request.getSession(false);
            sesion.invalidate();  
            response.sendRedirect("login.html");
        } else if (autorizado != null && autorizado) {
            response.sendRedirect("PanelServlet"); // Redirigir si ya está autenticado
        }    
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>" +
        "<html lang=\"es\">" + "<head>" + "  <meta charset=\"UTF-8\">" +
        "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
        "  <title>Verificación de contraseña</title>" +
        "  <style>" +"    body {" +"      background: linear-gradient(135deg, #00b4d8, #3a7bd5);" +
        "      font-family: 'Roboto', sans-serif;" +"      display: flex;" +
        "      justify-content: center;" +"      align-items: center;" +"      height: 100vh;" +
        "      margin: 0;" +"      color: #fff;" + "      overflow: hidden;" +
        "    }" +"" +"    .confirm-container {" + "      background: rgba(0, 0, 0, 0.6);" +
        "      padding: 40px 50px;" +"      border-radius: 15px;" +
        "      box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);" +"      backdrop-filter: blur(10px);" +
        "      display: flex;" +"      flex-direction: column;" +"      align-items: center;" +
        "      width: 300px;" +"    }" +"" +"    .confirm-container h1 {" +
        "      font-size: 24px;" +"      margin-bottom: 20px;" +"      text-align: center;" +
        "    }" +"" +"    .button {" +"      width: 100%;" + "      padding: 15px;" +
        "      background-color: #00b4d8;" +"      color: #fff;" +"      border: none;" +
        "      border-radius: 10px;" +"      font-size: 18px;" +"      cursor: pointer;" +
        "      transition: background-color 0.3s ease-in-out;" +"      margin: 5px 0;" +
        "    }" +"" +"    .button:hover {" +"      background-color: #007ea7;" +
        "    }" +"" +"    .button:focus {" +"      outline: none;" +"    }" +"" +
        "    .footer {" +"      position: absolute;" +"      bottom: 20px;" +
        "      font-size: 14px;" +"      color: #aaa;" +"    }" +"  </style>" +
        "</head>" +"<body>" +"    " +
        "    <form class=\"confirm-container\" action=\"ServletTemporal\" method=\"post\">" +
        "        <p id=\"texto-cambiable\">Tu palabra clave es: <h1>"+dato+"</h1> </p>" +
        "        <button type=\"submit\" name=\"confirmacion\" value=\"si\" class=\"button\">SI</button>" +
        "        <button type=\"submit\" name=\"confirmacion\" value=\"no\" class=\"button\">NO</button>" +
        "    </form>" +"" +"</body>" +"</html>");}}    
        
    