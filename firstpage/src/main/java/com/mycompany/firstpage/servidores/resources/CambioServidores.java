/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.firstpage.servidores.resources;

import java.io.*;    
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ferran
 */
public class CambioServidores {

    public static void ActualizarRadius(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            String command = "pkill -f servidor.py"; 
            Runtime.getRuntime().exec(command);
            String usuario = (String) request.getSession().getAttribute("cliente");

            String pythonScriptPath = "/home/ferran/Documentos/radius/servidor.py";
            String logPath = "/home/ferran/Documentos/radius/servidor.log";
            String command2 = String.format("nohup python3 %s %s > %s 2>&1 &", pythonScriptPath, usuario, logPath);

            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command2);
            pb.start();
        } catch (IOException e){}    
    }
    
    public static void ActualizarWebSocket(HttpServletRequest request){
        try {
            String command = "pkill -f servidorpush.py"; 
            Runtime.getRuntime().exec(command);
            String usuario = (String) request.getSession().getAttribute("cliente");
            
            String pythonScriptPath = "/home/ferran/Documentos/radius/servidorpush.py";
            String logPath = "/home/ferran/Documentos/radius/servidorpush.log";
            String command2 = String.format("nohup python3 %s %s > %s 2>&1 &", pythonScriptPath, usuario, logPath);

            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command2);
            pb.start();
        } catch (IOException e){}        
    }
}
