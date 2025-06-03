/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.otpserver;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author FERRAN
 */

public class PaginaAdmServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Encabezados de control de caché
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // Obtener la sesión sin crearla
        HttpSession session = request.getSession(false);
        session.setAttribute("dentro", true);

        // Verificar si la solicitud incluye el parámetro 't'
        String timestamp = request.getParameter("t");
        if (timestamp == null) {
            // Redirigir a una URL única con el parámetro 't'
            String uniqueUrl = request.getRequestURI() + "?t=" + System.currentTimeMillis();
            response.sendRedirect(uniqueUrl);
            return;
        }

        // Renderizar la página
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"es\">");
            out.println("<head>");
            out.println("  <meta charset=\"UTF-8\" />");
            out.println("  <title>Página Organización</title>");
            out.println("  <style>");
            out.println("    .logout-form {");
            out.println("      position: absolute;");
            out.println("      top: 10px;");
            out.println("      right: 10px;");
            out.println("    }");
            out.println("    .logout-btn {");
            out.println("      background-color: red;");
            out.println("      color: white;");
            out.println("      padding: 10px 20px;");
            out.println("      border: none;");
            out.println("      cursor: pointer;");
            out.println("      font-size: 16px;");
            out.println("    }");
            out.println("    .logout-btn:hover {");
            out.println("      background-color: darkred;");
            out.println("    }");
            out.println("  </style>");
            out.println("  <script>");
            out.println("    window.addEventListener(\"pageshow\", function(event) {");
            out.println("      if (event.persisted || (window.performance && window.performance.navigation.type === 2)) {");
            out.println("        window.location.reload(true);");
            out.println("      }");
            out.println("    });");
            out.println("  </script>");
            out.println("</head>");
            out.println("<body>");
            out.println("  <h1>Hola, bienvenid@ a la página web de la organización</h1>");
            out.println("  <form class=\"logout-form\" action=\"LogoutServlet\" method=\"post\">");
            out.println("    <button type=\"submit\" class=\"logout-btn\">Logout</button>");
            out.println("  </form>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}