/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.firstpage.comunes;

/**
 *
 * @author ferran
 */
import org.mindrot.jbcrypt.BCrypt;


public class GestionPasswords {
    

    public static String hashear(String pass) {
        String hash = BCrypt.hashpw(pass, BCrypt.gensalt());
        return hash;
    }
    
}   
