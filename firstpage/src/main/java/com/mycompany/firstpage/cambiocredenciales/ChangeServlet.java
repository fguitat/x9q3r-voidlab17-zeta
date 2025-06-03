/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.firstpage.cambiocredenciales;

import com.mycompany.firstpage.comunes.ConexionDB;
import com.mycompany.firstpage.comunes.CifradoSimetrico;
import java.io.*;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 *
 * @author ferran
 */
public class ChangeServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
        Boolean vienedepanel = (Boolean) request.getSession().getAttribute("enpanel"); 
        if (vienedepanel == null){
            request.getSession().invalidate();
            response.sendRedirect("login.html");
            return;
        }
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setDateHeader("Expires", 0); // Proxies
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang='es'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Cambio Credenciales</title>");
        out.println("<style>");
        out.println("body {");
        out.println("    background: linear-gradient(135deg, #00b4d8, #3a7bd5);");
        out.println("    font-family: 'Roboto', sans-serif;");
        out.println("    display: flex;");
        out.println("    justify-content: center;");
        out.println("    align-items: center;");
        out.println("    height: 100vh;");
        out.println("    margin: 0;");
        out.println("    color: #fff;");
        out.println("    overflow: hidden;");
        out.println("}");
        out.println(".form-container {");
        out.println("    background: rgba(0, 0, 0, 0.6);");
        out.println("    padding: 40px 50px;");
        out.println("    border-radius: 15px;");
        out.println("    box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);");
        out.println("    backdrop-filter: blur(10px);");
        out.println("    display: flex;");
        out.println("    flex-direction: column;");
        out.println("    align-items: center;");
        out.println("    width: 350px;");
        out.println("}");
        out.println(".form-container h1 {");
        out.println("    font-size: 24px;");
        out.println("    margin-bottom: 20px;");
        out.println("    text-align: center;");
        out.println("}");
        out.println(".form-container p {");
        out.println("    text-align: center;");
        out.println("    margin-bottom: 20px;");
        out.println("    font-size: 14px;");
        out.println("    line-height: 1.5;");
        out.println("}");
        out.println(".input-field {");
        out.println("    width: 100%;");
        out.println("    margin: 10px 0;");
        out.println("    padding: 15px;");
        out.println("    background-color: rgba(255, 255, 255, 0.1);");
        out.println("    border: 1px solid #fff;");
        out.println("    border-radius: 10px;");
        out.println("    color: #fff;");
        out.println("    font-size: 16px;");
        out.println("    transition: all 0.3s ease-in-out;");
        out.println("}");
        out.println(".input-field:focus {");
        out.println("    border-color: #00b4d8;");
        out.println("    outline: none;");
        out.println("    background-color: rgba(255, 255, 255, 0.2);");
        out.println("}");
        out.println(".checkbox-label {");
        out.println("    margin-top: 15px;");
        out.println("    font-size: 14px;");
        out.println("    display: flex;");
        out.println("    align-items: center;");
        out.println("    gap: 8px;");
        out.println("}");
        out.println(".checkbox-label input[type='checkbox'] {");
        out.println("    accent-color: #00b4d8;");
        out.println("    transform: scale(1.2);");
        out.println("}");
        out.println(".button {");
        out.println("    width: 100%;");
        out.println("    padding: 15px;");
        out.println("    background-color: #00b4d8;");
        out.println("    color: #fff;");
        out.println("    border: none;");
        out.println("    border-radius: 10px;");
        out.println("    font-size: 18px;");
        out.println("    cursor: pointer;");
        out.println("    transition: background-color 0.3s ease-in-out;");
        out.println("    margin-top: 20px;");
        out.println("}");
        out.println(".button:hover {");
        out.println("    background-color: #007ea7;");
        out.println("}");
        out.println(".button:disabled {");
        out.println("    background-color: #555;");
        out.println("    cursor: not-allowed;");
        out.println("}");
        out.println(".button:focus {");
        out.println("    outline: none;");
        out.println("}");
        out.println(".warning-text {");
        out.println("    font-size: 12px;");
        out.println("    margin-top: 20px;");
        out.println("    text-align: center;");
        out.println("    color: rgba(255, 255, 255, 0.7);");
        out.println("}");
        out.println("</style>");
        out.println("<script>");
        out.println("function toggleSubmitButton() {");
        out.println("    var checkbox = document.getElementById('confirmacion');");
        out.println("    var submitButton = document.getElementById('submitButton');");
        out.println("    submitButton.disabled = !checkbox.checked;");
        out.println("}");
        out.println("</script>");
        out.println("</head>");
        out.println("<body>");
        out.println("<form class='form-container' method='post' action='ChangeServlet'>");
        out.println("<h1>Cambio Credenciales</h1>");
        out.println("<p>Vas a proceder a cambiar tus credenciales de inicio de sesión. Esta acción es irreversible</p>");
        out.println("<input type='text' class='input-field' id='nombre' name='nombre' placeholder='Usuario' required>");
        out.println("<input type='text' class='input-field' id='palabra_clave' name='palabra_clave' placeholder='Palabra clave' required>");
        out.println("<label class='checkbox-label'>");
        out.println("<input type='checkbox' id='confirmacion' onchange='toggleSubmitButton()'>");
        out.println("Confirmo que los datos son correctos.");
        out.println("</label>");
        out.println("<button type='submit' class='button' id='submitButton' disabled>Cambiar credenciales</button>");
        out.println("<p class='warning-text'>Cuando pulses 'Cambiar credenciales', tu sesión se cerrará automáticamente.</p>");
        out.println("</form>");
        out.println("</body>");
        out.println("</html>");

        
    }    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
        String nombre = request.getParameter("nombre");
        String palabraClave = request.getParameter("palabra_clave");
        String cliente = (String) request.getSession().getAttribute("cliente");
    
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try (Connection con = ConexionDB.obtenerConexion()) {
            con.setAutoCommit(false);
            String query1 = "SELECT id, clave_secreta FROM usuarios";
            try (PreparedStatement pstmt1 = con.prepareStatement(query1); ResultSet rs = pstmt1.executeQuery()) {
                while (rs.next()) {
                    String secreto = rs.getString("clave_secreta");
                    String id = rs.getString("id");
                    String newsec = CifradoSimetrico.descifrador(secreto, cliente);
                    String newsecif = CifradoSimetrico.cifrador(newsec, nombre);
                    String updateQuery = "UPDATE usuarios SET clave_secreta = ? WHERE id = ?";
                    try (PreparedStatement pstmt2 = con.prepareStatement(updateQuery)) {
                        pstmt2.setString(1, newsecif);
                        pstmt2.setString(2,id);
                        pstmt2.executeUpdate();
                    } catch (Exception e){
                    }    
                }
            // Actualización tabla 'login'
            String query3 = "UPDATE login SET clave = ?";
            try (PreparedStatement pstmt3 = con.prepareStatement(query3)) {
                pstmt3.setString(1, CifradoSimetrico.cifrador(palabraClave, nombre));
                pstmt3.executeUpdate();
            } 
            con.commit();
            }catch (NullPointerException e) {
                con.rollback();
                String paginadest ="login.html";
                out.println("<html><body>");
                out.println("<script>");
                out.println("alert('La sesión ha caducado antes de pulsar. Por motivos de seguridad, no se hará el cambio de credenciales');");
                out.println("window.location.href = '"+paginadest+"';");
                out.println("</script>");
                out.println("</body></html>");              
            }catch (Exception ex){
                con.rollback();
                request.getSession().invalidate();
                response.sendRedirect("login.html");          
            }    
        } catch (Exception e) {
            request.getSession().invalidate();
            response.sendRedirect("login.html");
        }
    }
}
