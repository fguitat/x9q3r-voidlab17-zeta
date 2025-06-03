/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.firstpage;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author ferran
 */
public class TestServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        String section = request.getParameter("section");

        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null)? true:false;
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (!loggedIn) {
            out.println("<script>window.open('" + "login.html" + "', '_top');</script>");
        } else {
            RequestDispatcher dispatcher = request.getRequestDispatcher(section);
            dispatcher.forward(request, response); 
        }
    }


    
}    