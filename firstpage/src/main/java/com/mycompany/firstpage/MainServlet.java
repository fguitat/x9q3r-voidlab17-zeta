/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.firstpage;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mycompany.firstpage.comunes.CifradoSimetrico;
import com.mycompany.firstpage.comunes.ConexionDB;


/**
 *
 * @author ferran
 */

public class MainServlet extends HttpServlet {
    private boolean isUserRegistered() {
        try (Connection con = ConexionDB.obtenerConexion()) {
            String query = "SELECT COUNT(*) FROM login";
            try (PreparedStatement stmt = con.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;  
                }
            }
        } catch (SQLException e) {
        } finally {
            ConexionDB.cerrarConexion();
        }
        return false; 
    }

    // Método para registrar al usuario con la contraseña cifrada
    private void registerUser(String nombre, String palabraClave) throws SQLException, NoSuchAlgorithmException {
        try (Connection con = ConexionDB.obtenerConexion()) {
            String query = "INSERT INTO login (clave) VALUES (?)";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, CifradoSimetrico.cifrador(palabraClave, nombre));
                pstmt.executeUpdate();
            } catch (Exception ex) {
                Logger.getLogger(MainServlet.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                ConexionDB.cerrarConexion();
            }
        }
    }

    // Método para comprobar la contraseña de un usuario
    private String validateUser(String nombre) throws SQLException, NoSuchAlgorithmException {
        try (Connection con = ConexionDB.obtenerConexion()) {
            String query = "SELECT clave FROM login";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return CifradoSimetrico.descifrador(rs.getString("clave"), nombre);
                        
                    }
                } catch (Exception ex) {

                }
            } finally {
                ConexionDB.cerrarConexion();
        }
        }
        return "";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate"); // No almacenar en caché
        response.setHeader("Pragma", "no-cache"); // Para HTTP/1.0
        response.setDateHeader("Expires", 0); // Proxies y caché
        request.getSession().setAttribute("Aplicacion-arrancada", true);
        Boolean autorizado = (Boolean) request.getSession().getAttribute("adminAutenticado");
        Boolean entrasinurl = (Boolean) request.getSession().getAttribute("iniciado");
        if (autorizado != null && autorizado) {
            response.sendRedirect("PanelServlet"); // Redirigir si ya está autenticado
            return;
        }else if (entrasinurl != null && entrasinurl){
            response.sendRedirect("ControlServlet");
            return;
        }else {
            if (!isUserRegistered()) {
                request.getRequestDispatcher("registro.html").forward(request, response);
            } else {
                request.getRequestDispatcher("login.html").forward(request, response);
            }
        }  
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {     
        String nombre = request.getParameter("nombre");
        String palabraClave = request.getParameter("palabra_clave");
        request.getSession().setAttribute("cliente", nombre);
        if (!isUserRegistered()) {
            try {
                registerUser(nombre, palabraClave);
                request.getSession().setAttribute("adminAutenticado", true);
                response.sendRedirect("PanelServlet");
            } catch (SQLException | NoSuchAlgorithmException e) {
                response.getWriter().println("Error al registrar el usuario: " + e.getMessage());
            }
        } else {
            String claveDescifrada = null;
            try {
                claveDescifrada = validateUser(nombre);
            } catch (Exception ex) {
            } 
            request.getSession().setAttribute("iniciado", true);
            request.getSession().setAttribute("variable", claveDescifrada);
            response.sendRedirect("ControlServlet");
        }
    }
}

