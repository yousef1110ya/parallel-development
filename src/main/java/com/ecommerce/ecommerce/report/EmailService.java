package com.ecommerce.ecommerce.report;

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailWithAttachment(String to, String subject,
                                         String body, byte[] attachment,
                                         String attachmentFilename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(attachmentFilename, new ByteArrayResource(attachment));
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
    
    public void sendEmail(String to , String from ,String subject, String body) {
    	try {
    		SimpleMailMessage message = new SimpleMailMessage();
    		message.setTo(to);
    		message.setFrom(from);
    		message.setSubject(subject);
    		message.setText(body);
    		mailSender.send(message);
    		
    		System.out.println("Sent an email to : " + to);  
    	} catch(Exception e) {
    		throw new RuntimeException("Failed to send email: " + e.getMessage());
    	}
    }
    
    public void sendOrderConfirmation(String toEmail, Long orderId, String totalPrice) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setFrom("yousefalshikhali1711@gmail.com");
            message.setSubject("Order Confirmation - Order #" + orderId);
            message.setText(
                "Hello,\n\n" +
                "Your order has been placed successfully!\n\n" +
                "Order ID   : #" + orderId + "\n" +
                "Total Price: $" + totalPrice + "\n\n" +
                "Thank you for shopping with us.\n\n" +
                "Best regards,\nEcommerce Team"
            );
            mailSender.send(message);


        } catch (Exception e) {
        	throw new RuntimeException("Failed to send email from " + e.getMessage());
        }
    }
}