/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.firstpage.usuarios;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import com.mycompany.firstpage.usuarios.resources.keygen;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mycompany.firstpage.comunes.*;
import javax.servlet.http.HttpSession;

/**
 *
 * @author ferran
 */

public class UsuarioServlet extends HttpServlet {    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");
        String accion = request.getParameter("accion");
        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null)? true:false;
        
        if (!loggedIn) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json");
            response.getWriter().write("{\"redirect\": \"login.html\"}");
            return;
        } else {    
            try {

                switch (accion) {
                    case "listar":
                        listarUsuarios(response.getWriter());
                        break;
                    case "agregar":
                        agregarUsuario(request, response);
                        break;
                    case "actualizarEstado":
                        actualizarEstado(request);
                        response.getWriter().write("Estado actualizado correctamente");
                        break;
                    case "eliminar":
                        eliminarUsuario(request);
                        response.getWriter().write("Usuario eliminado correctamente");
                        break;
                    default:
                        response.getWriter().write("Acción no válida");
                }
            } catch (Exception ex) {
                response.getWriter().write("Error en la operación: " + ex.getMessage());
            }
        }    

    }
    
    private void listarUsuarios(PrintWriter out) throws SQLException {
        StringBuilder html = new StringBuilder();
        
        html.append("<table>")
            .append("<tr><th>Usuario</th><th>Estado</th><th>Acciones</th></tr>");
        
        try (Connection conn = ConexionDB.obtenerConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, usuario, estado FROM usuarios")){
            while (rs.next()) {
                String usuario = rs.getString("usuario");
                String estado = rs.getString("estado");
                int id = rs.getInt("id");

                // Verificamos el estado actual para seleccionar la opción correcta
                boolean preRegistradoSelected = "Pre-registrado".equalsIgnoreCase(estado);
                boolean registradoSelected = "Registrado".equalsIgnoreCase(estado);

                html.append("<tr data-id='").append(id).append("'>")
                    .append("<td>").append(usuario).append("</td>")
                    .append("<td>")
                    .append("<select class='select-estado' onchange='actualizarEstado(this)'>")
                    .append("<option value='Pre-registrado' ").append(preRegistradoSelected ? "selected" : "").append(">Pre-registrado</option>")
                    .append("<option value='Registrado' ").append(registradoSelected ? "selected" : "").append(">Registrado</option>")
                    .append("</select>")
                    .append("</td>")
                    .append("<td><button class='btn-eliminar' onclick='eliminarUsuario(").append(id).append(")'>Eliminar</button></td>")
                    .append("</tr>");
            }
        } catch (Exception e){
        } 
    
        
        html.append("</table>");
        out.print(html.toString());
    }
    
    private void actualizarEstado(HttpServletRequest request) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        String nuevoEstado = request.getParameter("estado");

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE usuarios SET estado = ? WHERE id = ?")) {

            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }
    
    
    private void agregarUsuario(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String usuario = request.getParameter("usuario");
        String password = request.getParameter("password");
        String cliente = (String) request.getSession().getAttribute("cliente");
        PrintWriter out = response.getWriter();

        try (Connection conn = ConexionDB.obtenerConexion()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM usuarios WHERE usuario = ?")) {

                checkStmt.setString(1, usuario);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        response.setStatus(HttpServletResponse.SC_CONFLICT); // 409
                        out.write("Lo siento, un usuario con ese nick ya está en la base de datos. Prueba otro");
                        return;
                    }
                }
            }
            
            String key = keygen.generatekey();
            String keycif = CifradoSimetrico.cifrador(key, cliente);
            String hashedPass = GestionPasswords.hashear(password);

            // INSERCIÓN DEL NUEVO USUARIO
            try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO usuarios (usuario, password, clave_secreta) VALUES(?, ?, ?)")) {

                insertStmt.setString(1, usuario);
                insertStmt.setString(2, hashedPass);
                insertStmt.setString(3, keycif);
                insertStmt.executeUpdate();

                response.setStatus(HttpServletResponse.SC_OK); // 200
                out.write("Usuario agregado correctamente");
            }
        } catch (Exception ex) {
            Logger.getLogger(UsuarioServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void eliminarUsuario(HttpServletRequest request) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM usuarios WHERE id = ?")) {

            pstmt.setInt(1, id);
            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("No se encontró el usuario con ID: " + id);
            }
        }
    }
}
