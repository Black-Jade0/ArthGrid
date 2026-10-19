package com.Prakhar.Notification_service.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendPaymentNotification(String toEmail, String subject, String body) {

        try {
            // Change: Using MimeMessage for HTML support instead of SimpleMailMessage
            MimeMessage message = javaMailSender.createMimeMessage();

            // 'true' means multipart/HTML is enabled
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("prakharietdavv@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);

            // --- AGRAWAL BANK PREMIUM TEMPLATE ---
            // Humne tumhare 'body' variable ko is HTML structure ke beech mein inject kar diya hai
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { margin: 0; padding: 0; background-color: #f1f5f9; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; }
                        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                        .header { padding: 30px; text-align: center; background-color: #0B1120; color: white; border-bottom: 4px solid #d4af37; }
                        .brand { font-size: 22px; font-weight: bold; letter-spacing: 1px; }
                        .content { padding: 40px 30px; color: #334155; line-height: 1.6; font-size: 16px; }
                        .footer { background-color: #f8fafc; padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
                        .highlight { color: #0B1120; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="brand">Agrawal<span style="color: #d4af37;">Bank</span></div>
                        </div>
                        <div class="content">
                            {{MESSAGE_BODY}}
                        </div>

                        <div class="footer">
                            <p>&copy; 2026 Agrawal Bank Ltd. All rights reserved.</p>
                            <p style="margin-top: 10px;">This is a system-generated email. Please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;

            // Logic: Replace {{MESSAGE_BODY}} with the actual 'body' passed to function
            // Also replacing newlines (\n) with HTML breaks (<br>) so formatting stays good
            String finalHtml = htmlContent.replace("{{MESSAGE_BODY}}", body.replace("\n", "<br>"));

            helper.setText(finalHtml, true); // true indicates HTML

            javaMailSender.send(message);
            System.out.println("✅ Premium HTML Email Sent Successfully to " + toEmail);
        }
        catch (MessagingException e) {
            System.err.println("❌ Error sending email: " + e.getMessage());
        }
    }
}