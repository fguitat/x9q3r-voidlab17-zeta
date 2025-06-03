/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.firstpage.usuarios.resources;

/**
 *
 * @author ferran
 */
import io.github.ssrack.javatotp.TOTPUtility;

public class keygen {
    
    public static String generatekey() throws Exception {
        String secretKey = TOTPUtility.generateSecretKey();
        
        return secretKey;
    }
}    

