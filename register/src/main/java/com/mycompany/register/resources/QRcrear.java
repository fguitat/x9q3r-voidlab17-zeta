package com.mycompany.register.resources;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ferran
 */
import net.glxn.qrgen.QRCode;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public class QRcrear { 
    
    public static BufferedImage generateQR(String usuario, String secretKey) throws Exception {
        
        String qrData = "usuario=" + usuario + "&clave=" + secretKey;  
        try (ByteArrayOutputStream stream = QRCode.from(qrData)
            .withSize(300, 300)
            .stream()) {
            return ImageIO.read(new ByteArrayInputStream(stream.toByteArray()));
        } catch (Exception e) {
            throw new Exception("Error al generar el código QR", e);
        }
    }
}
