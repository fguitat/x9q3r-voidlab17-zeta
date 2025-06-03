/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Filter.java to edit this template
 */
package com.mycompany.firstpage;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
/**
 *
 * @author ferran
 */
public class LoginFilter implements Filter {
    
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
            chain.doFilter(request, response);
        }
    }
}    