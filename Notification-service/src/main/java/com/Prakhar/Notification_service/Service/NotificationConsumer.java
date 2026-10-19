package com.Prakhar.Notification_service.Service;

import com.Prakhar.Notification_service.DTO.PaymentEvent;
import com.Prakhar.Notification_service.DTO.UserEvent;
import com.Prakhar.Notification_service.Entity.NotificationEntry;
import com.Prakhar.Notification_service.Repo.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationRepository notificationRepository;


    public void debug(String message) {
        System.out.println("RAW MESSAGE = " + message);
    }

    @KafkaListener(topics = "payment-events", containerFactory = "paymentKafkaListenerContainerFactory")
    public void consumePaymentSuccessNotifications(PaymentEvent event) {

        System.out.println("Message Received: " + event);

        // --- 1. SENDER EMAIL (Debit Alert) ---
        if (event.getEmail() != null && !event.getEmail().isEmpty()) {

            String subject = "Payment Update: " + event.getStatus();

            // HTML Styling added for Premium Look
            String body = "Hello User,\n\n" +
                    "Your payment of <b>Rs. " + event.getAmount() + "</b> for <b>" + event.getBillerName() + "</b> was <b style='color:blue'>" + event.getStatus() + "</b>.\n" +
                    "Transaction ID: <span style='font-family:monospace; background:#eee; padding:2px;'>" + event.getTransactionId() + "</span>\n\n" +
                    "Regards,\nAgrawal Bank";

            String email = event.getEmail();
            emailService.sendPaymentNotification(email, subject, body);

            NotificationEntry notificationEntry = new NotificationEntry(
                    event.getTransactionId(),
                    event.getAmount(),
                    event.getStatus(),
                    email
            );
            notificationRepository.save(notificationEntry);
            System.out.println("Notification Saved to Database!");
        }

        // --- 2. RECEIVER EMAIL (Credit Alert) ---
        if (event.getTargetEmail() != null && !event.getTargetEmail().isEmpty()) {

            String subject = "Money Received: Credit Alert!";

            // Green color for Credit alert
            String body = "Hello User,\n\n" +
                    "Your account <b>" + event.getTargetAccountNumber() + "</b> has been <b style='color:green'>CREDITED</b> with <b>Rs. " + event.getAmount() + "</b>.\n" +
                    "Sender Account: " + event.getSourceAccountNumber() + "\n" +
                    "Transaction ID: <span style='font-family:monospace; background:#eee; padding:2px;'>" + event.getTransactionId() + "</span>\n\n" +
                    "Enjoy spending!\nAgrawal Bank";

            // Email bhejo
            emailService.sendPaymentNotification(event.getTargetEmail(), subject, body);

            // DB mein entry
            NotificationEntry receiverLog = new NotificationEntry(
                    event.getTransactionId(),
                    event.getAmount(),
                    "CREDITED",
                    event.getTargetEmail()
            );
            notificationRepository.save(receiverLog);
            System.out.println("Receiver Notification Sent & Saved!");
        }
    }

    @KafkaListener(topics = "user-events",containerFactory = "userKafkaListenerContainerFactory")
    public void consumeUserEvents(UserEvent event) {

        System.out.println("User Event Received: " + event.getEmail());

        if ("USER_CREATED".equals(event.getEventType())) {

            String subject = "Welcome to Agrawal Bank!";
            String tempPassword = "Bank@123";
            String resetLink=event.getResetLink();

            String body = "Hello <b>" + event.getUsername() + "</b>,\n\n" +
                    "Welcome to Agrawal Bank Digital Services! Your premium savings account has been successfully created.\n\n" +
                    "Here are your secure login details:\n" +
                    "📧 <b>Email:</b> " + event.getEmail() + "\n" +
                    "🔑 <b>Temporary Password:</b> <span style='background-color:#ffffcc; padding:2px 5px; border:1px solid #e0e0e0;'>" + tempPassword + "</span>\n\n" +
                    "<b>Set Your Own Password:</b>\n" +
                    "To set a permanent password of your choice, please click the button below:\n\n" +

                    // GOLDEN BUTTON SECTION
                    "<div style='margin: 20px 0;'>" +
                    "  <a href='" + resetLink + "' style='background-color: #fbbf24; color: #0B1120; padding: 12px 25px; text-decoration: none; font-weight: bold; border-radius: 5px; display: inline-block; border: 1px solid #d97706; text-transform: uppercase; font-size: 13px;'>Set Your Secure Password</a>" +
                    "</div>\n" +

                    "<i style='color: #666; font-size: 12px;'>⚠️ Note: For your security, this password reset link will expire in <b>24 hours</b>.</i>\n\n" +
                    "Please login and change your password immediately for security reasons. Do not share your credentials with anyone.\n\n" +
                    "Regards,\n" +
                    "<b>Security Team</b>\n" +
                    "Agrawal Bank Digital Services";

            emailService.sendPaymentNotification(event.getEmail(), subject, body);

            System.out.println("Welcome Email Sent to: " + event.getEmail());
        }
    }
}