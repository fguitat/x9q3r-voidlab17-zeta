/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.firstpage.servidores;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.JSONObject;
import com.mycompany.firstpage.comunes.ConexionDB;
import com.mycompany.firstpage.servidores.resources.CambioServidores;

/**
 *
 * @author ferran
 */
public class EstadoServidoresServlet extends HttpServlet {
    
    private Process process;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null)? true:false;
        
        if (!loggedIn) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json");
            response.getWriter().write("{\"redirect\": \"login.html\"}");
            return;
        } else {   
            Boolean vienedepanel = (Boolean) request.getSession().getAttribute("enpanel");
            if ((vienedepanel  == null || !vienedepanel)) {
                request.getSession().invalidate(); 
                response.sendRedirect("login.html"); 
                return;
            }
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
            response.setHeader("Pragma", "no-cache"); // HTTP 1.0
            response.setDateHeader("Expires", 0); // Proxies
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            JSONObject jsonResponse  = new JSONObject();

            try (Connection conn = ConexionDB.obtenerConexion()) {
                String query = "SELECT nombre_servidor, estado FROM estado_botones";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String servidor = rs.getString("nombre_servidor");
                        boolean estado = rs.getBoolean("estado");
                        if (estado && servidor.equals("radius")){
                            CambioServidores.ActualizarRadius(request, response);
                            
                        } 
                        if (estado && servidor.equals("push")){
                            CambioServidores.ActualizarWebSocket(request);
                        }
                        jsonResponse.put(servidor, estado);
                        
                    }
                }
            } catch (SQLException e) {
            } finally {
                ConexionDB.cerrarConexion();
            }
            response.getWriter().write(jsonResponse.toString());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null)? true:false;
        
        if (!loggedIn) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json");
            response.getWriter().write("{\"redirect\": \"login.html\"}");
            return;
        } else {   
            JSONObject jsonresponse = null;

            // Obtener la solicitud del cuerpo de la petición
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            // Convertir el JSON recibido en un objeto
            JSONObject jsonRequest = new JSONObject(sb.toString());
            String servidor = jsonRequest.getString("servidor");
            boolean estado = jsonRequest.getBoolean("estado");

            // Actualizar el estado en la base de datos
            jsonresponse = devolverestado(request, response, servidor, estado);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonresponse.toString());
        }
    }

    private JSONObject devolverestado(HttpServletRequest request, HttpServletResponse response, String servidor, boolean nuevoEstado) {
        Map<String, Boolean> estados = new HashMap<>();
        JSONObject json = new JSONObject();
        String usuario = (String) request.getSession().getAttribute("cliente");
        
        // Conexión a la base de datos
        try (Connection conn = ConexionDB.obtenerConexion()) {

            // Obtener el estado actual de todos los servidores
            String query = "SELECT nombre_servidor, estado FROM estado_botones";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String nombre = rs.getString("nombre_servidor");
                    boolean estadoActual = rs.getBoolean("estado"); // se interpreta como booleano
                    estados.put(nombre, estadoActual);
                }
            }
            // Simular el cambio de estado
            estados.put(servidor, nuevoEstado);

            if (estados.get("push")){
                estados.put("radius", true);
            } else if (!estados.get("radius")){
                estados.put("push", false);
            }

            String updateQuery = "UPDATE estado_botones SET estado = ? WHERE nombre_servidor = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                for (Map.Entry<String, Boolean> entry : estados.entrySet()) {
                    updateStmt.setBoolean(1, entry.getValue());
                    updateStmt.setString(2, entry.getKey());
                    updateStmt.executeUpdate();
                    json.put(entry.getKey(), entry.getValue());
                }
            }

            if (estados.get("radius")){
                try {
                    CambioServidores.ActualizarRadius(request, response);     
                } catch (Exception e) {
                    e.printStackTrace();
                }  
            } else{
                try{
                    String command = "pkill -f servidor.py"; // Mata cualquier proceso que tenga 'servidor.py' en su línea de comando
                    Runtime.getRuntime().exec(command);
                }catch(Exception e){

                }    
            }
            
            if (estados.get("push")){
                try {
                    CambioServidores.ActualizarWebSocket(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }  

            } else{
                try{
                    String command = "pkill -f servidorpush.py"; // Mata cualquier proceso que tenga 'servidor.py' en su línea de comando
                    Runtime.getRuntime().exec(command);
                }catch(Exception e){

                }    
            }
            return json;
            
        } catch (SQLException ex) {
        } finally {
            ConexionDB.cerrarConexion();
        }


        // En caso de error, se devuelve un JSON vacío
        return json;
    }
}    
