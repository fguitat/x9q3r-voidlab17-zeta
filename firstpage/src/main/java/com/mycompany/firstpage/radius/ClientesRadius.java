/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.firstpage.radius;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.sql.*;
import com.mycompany.firstpage.comunes.ConexionDB;

/**
 *
 * @author ferran
 */
public class ClientesRadius extends HttpServlet {  
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
            .append("<tr><th>Nombre cliente</th><th>IP cliente</th><th>Secreto</th></tr>");
        
        String query ="SELECT id, Nombre_cliente, IP_Cliente, Secreto FROM clientes_radius";  
        try (Connection conn = ConexionDB.obtenerConexion();
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()){
            while (rs.next()) {
                String cliente = rs.getString("Nombre_Cliente");
                String ip = rs.getString("IP_Cliente");
                String secreto = rs.getString("Secreto");
                int id = rs.getInt("id");


                html.append("<tr data-id='").append(id).append("'>")
                    .append("<td>").append(cliente).append("</td>")
                    .append("<td>").append(ip).append("</td>")
                    .append("<td>").append(secreto).append("</td>")
                    .append("<td><button class='btn-eliminar' onclick='eliminarUsuario(").append(id).append(")'>Eliminar</button></td>")
                    .append("</tr>");
            }
        } catch (Exception e){
        } 
        html.append("</table>");
        out.println("</html>");
        out.print(html.toString());
    }
    
    
    
    
    private void agregarUsuario(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String cliente = request.getParameter("nombre");
        String ip = request.getParameter("ip");
        String secreto = request.getParameter("secreto");
        PrintWriter out = response.getWriter();

        try (Connection conn = ConexionDB.obtenerConexion()) {
            try (PreparedStatement checkStmt2 = conn.prepareStatement(
                    "SELECT COUNT(*) FROM clientes_radius WHERE IP_Cliente = ?")) {
                checkStmt2.setString(1, ip);
                try (ResultSet rs2 = checkStmt2.executeQuery()) {
                    if (rs2.next() && rs2.getInt(1) != 0) {
                        response.setStatus(HttpServletResponse.SC_CONFLICT); // 409
                        out.write("No puedes tener dos servidores con la misma IP");
                        return;
                    }
                }
            } 
            
            // INSERCIÓN DEL NUEVO USUARIO
            try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO clientes_radius (Nombre_Cliente, IP_Cliente, Secreto) VALUES(?, ?, ?)")) {

                insertStmt.setString(1, cliente);
                insertStmt.setString(2, ip);
                insertStmt.setString(3, secreto);
                insertStmt.executeUpdate();

                response.setStatus(HttpServletResponse.SC_OK); // 200
                out.write("Usuario agregado correctamente");
            }
        } catch (Exception ex) {
           
        }
    }      

    private void eliminarUsuario(HttpServletRequest request) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM clientes_radius WHERE id = ?")) {
            pstmt.setInt(1, id);
            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("No se encontró el usuario con ID: " + id);
            }
        }
    }
    
  
}
