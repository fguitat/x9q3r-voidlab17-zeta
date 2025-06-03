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
public class PanelServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getSession().setAttribute("enpanel", true);
        Boolean autorizado = (Boolean) request.getSession().getAttribute("adminAutenticado");
        if (autorizado == null || !autorizado) {
            request.getSession().invalidate();
            response.sendRedirect("login.html"); 
            return;
        }
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); 
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0); 
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>"
            + "<html lang='es'>"+ "<head>"+ "  <meta charset='UTF-8'>"
            + "  <title>Panel de Administración</title>"+ "  <style>"
            + "    body { margin: 0; font-family: Arial, sans-serif; }"
            + "    .menu { width: 200px; background-color: #333; color: white; height: 100vh; float: left; padding-top: 20px; }"
            + "    .menu a { display: block; color: white; padding: 12px; text-decoration: none; }"
            + "    .menu a:hover { background-color: #575757; }"
            + "    .top-bar { background-color: #f4f4f4; padding: 10px; text-align: right; }"
            + "    .top-bar form { display: inline; }"
            + "    .content { margin-left: 200px; height: calc(100vh - 40px); }"
            + "    iframe { width: 100%; height: 100%; border: none; }"
            + "  </style>"+ "</head>" + "<body>" + "  <div class='top-bar'>"
            + "    <span style='float:left; padding-left: 10px;'>¡Hola, admin!</span>"
            + "    <form action='LogoutServlet' method='post'>"
            + "      <button type='submit' style='background-color: red; color: white; padding: 10px 20px; border: none; border-radius: 6px; cursor: pointer;'>Logout</button>"
            + "    </form>"
            + "    <form action='ChangeServlet' method='get' style='display: inline;'>"
            + "      <button type='submit' style='background-color: yellow; color: black; padding: 10px 20px; border: none; border-radius: 6px; cursor: pointer;'>Cambio Credenciales</button>"
            + "    </form>" + "  </div>"  + "  <div class='menu'>"
            +"     <form method='post' action='TestServlet' target='contenido' style='margin:0;'>"
            +"       <input type='hidden' name='section' value='servidores/inicio.html'>"
            +"       <button type='submit' style='background:none; border:none; color:white; padding:12px; text-align:left; width:100%; cursor:pointer;'>Servidores</button>"
            +"    </form>"
            +"     <form method='post' action='TestServlet' target='contenido' style='margin:0;'>"
            +"       <input type='hidden' name='section' value='usuarios/entrada.html'>"
            +"       <button type='submit' style='background:none; border:none; color:white; padding:12px; text-align:left; width:100%; cursor:pointer;'>Usuarios</button>"
            +"    </form>"               
            +"     <form method='post' action='TestServlet' target='contenido' style='margin:0;'>"
            +"       <input type='hidden' name='section' value='radius/inicio.html'>"
            +"       <button type='submit' style='background:none; border:none; color:white; padding:12px; text-align:left; width:100%; cursor:pointer;'>Clientes Radius</button>"
            +"    </form>"+ "  </div>"+ "  <div class='content'>"
            + "    <iframe name='contenido' src='bienvenida.html'></iframe>"
            + "  </div>" + "</body>"+ "</html>");}}

 