/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Filter.java to edit this template
 */
package com.mycompany.firstpage;

import com.mycompany.firstpage.comunes.ConexionDB;
import java.io.IOException;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author ferran
 */
public class RegisterFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        Boolean dentropanel = (Boolean) req.getSession().getAttribute("enpanel");

        if (dentropanel != null && dentropanel) {
            res.sendRedirect(req.getContextPath() + "/PanelServlet");
        } else {
            try {
                if (yaregistrado()){
                    res.sendRedirect(req.getContextPath() + "/MainServlet");
                } else { 
                    chain.doFilter(request, response);
                }
            } catch (SQLException ex) {
                
            }
            
        }
    }
    
    private boolean yaregistrado() throws SQLException{
        try (Connection con = ConexionDB.obtenerConexion()) {
            String query = "SELECT clave FROM login";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return true;
                        
                    } else{
                        return false;
                    }
                } catch (Exception ex) {
                    return false;
                }
            } finally {
                ConexionDB.cerrarConexion();
            }
        }    
    }
    
}
