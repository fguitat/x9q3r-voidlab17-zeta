/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.firstpage;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 *
 * @author ferran
 */
public class ServletTemporal extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache"); 
        response.setDateHeader("Expires", 0); 

        // Verificar si el usuario está autenticado
        Boolean autorizado = (Boolean) request.getSession().getAttribute("adminAutenticado");
        if (autorizado != null && autorizado) {
            response.sendRedirect("PanelServlet"); 
            return;
        }
        
        
        request.getSession().removeAttribute("variable");
        
        
        String confirmacion = request.getParameter("confirmacion");

        if ("si".equals(confirmacion)) {
            request.getSession().setAttribute("adminAutenticado", true);
            response.sendRedirect("PanelServlet");

        } else{
            request.getSession().invalidate();  
            response.sendRedirect("login.html");
        }
    }

}    