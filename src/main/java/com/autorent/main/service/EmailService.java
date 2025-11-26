package com.autorent.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException; // Importar la excepción base
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreo(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        
        // El remitente (FROM) es manejado por application.properties
        // Si no lo tienes ahí, podría fallar si Moosend no lo asigna automáticamente.
        
        message.setSubject(subject);
        message.setText(text);
        
        try {
            // Intenta enviar el correo
            mailSender.send(message);
            System.out.println("✅ Correo enviado exitosamente a: " + to);
        } catch (MailException e) {
            // 🛑 CAPTURAR Y MOSTRAR EL ERROR EXACTO 🛑
            System.err.println("❌ ERROR al enviar correo a: " + to);
            e.printStackTrace(); // Imprime la traza completa para diagnóstico
        }
    }
}