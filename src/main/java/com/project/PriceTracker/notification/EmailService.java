package com.project.PriceTracker.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${notification.sender.email}")
    private String senderEmail;

    public void sendPriceAlert(String toEmail, String productName,
                               Double currentPrice, Double targetPrice, String productUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(toEmail);
            message.setSubject("🔔 Price Alert: " + productName);

            double savings = targetPrice - currentPrice;
            String emailBody = String.format(
                    "Great news! The price dropped!\n\n" +
                            "Product: %s\n" +
                            "Current Price: $%.2f\n" +
                            "Your Target: $%.2f\n" +
                            "Savings: $%.2f (%.1f%% off)\n\n" +
                            "Buy now: %s\n\n" +
                            "Happy shopping! 🎉",
                    productName,
                    currentPrice,
                    targetPrice,
                    savings,
                    (savings / targetPrice) * 100,
                    productUrl
            );

            message.setText(emailBody);

            mailSender.send(message);
            System.out.println("✅ Email sent to: " + toEmail + " for product: " + productName);

        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Validate email format
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}