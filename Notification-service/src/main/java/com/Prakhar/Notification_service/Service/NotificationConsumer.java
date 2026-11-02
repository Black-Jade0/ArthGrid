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

            boolean isTransfer = event.getTargetAccountNumber() != null && !event.getTargetAccountNumber().isEmpty();
            String subject = isTransfer
                    ? "Debit Alert: Rs. " + event.getAmount() + " sent" + (event.getTargetName() != null ? " to " + event.getTargetName() : "")
                    : "Payment Update: " + event.getStatus();

            String body = "Hello " + (event.getSenderName() != null ? "<b>" + event.getSenderName() + "</b>" : "User") + ",\n\n" +
                    (isTransfer
                        ? "Your account <b>" + event.getSourceAccountNumber() + "</b> has been <b style='color:#dc2626'>DEBITED</b> with <b>Rs. " + event.getAmount() + "</b>.\n" +
                          "Sent to: <b>" + (event.getTargetName() != null ? event.getTargetName() : "") + "</b> (A/c " + event.getTargetAccountNumber() + ")\n"
                        : "Your payment of <b>Rs. " + event.getAmount() + "</b> for <b>" + event.getBillerName() + "</b> was <b style='color:blue'>" + event.getStatus() + "</b>.\n") +
                    "Transaction ID: <span style='font-family:monospace; background:#eee; padding:2px;'>" + event.getTransactionId() + "</span>\n\n" +
                    "<i style='color:#666; font-size:12px;'>This is a demo project - no real money was moved.</i>\n\n" +
                    "Regards,\nArthGrid Bank";

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

            String subject = "Credit Alert: Rs. " + event.getAmount() + " received" + (event.getSenderName() != null ? " from " + event.getSenderName() : "");

            String body = "Hello " + (event.getTargetName() != null ? "<b>" + event.getTargetName() + "</b>" : "User") + ",\n\n" +
                    "Your account <b>" + event.getTargetAccountNumber() + "</b> has been <b style='color:green'>CREDITED</b> with <b>Rs. " + event.getAmount() + "</b>.\n" +
                    "From: <b>" + (event.getSenderName() != null ? event.getSenderName() : "") + "</b> (A/c " + event.getSourceAccountNumber() + ")\n" +
                    "Transaction ID: <span style='font-family:monospace; background:#eee; padding:2px;'>" + event.getTransactionId() + "</span>\n\n" +
                    "<i style='color:#666; font-size:12px;'>This is a demo project - no real money was moved.</i>\n\n" +
                    "Regards,\nArthGrid Bank";

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

            if (event.getUserId() != null && event.getUserId().startsWith("local-")) {
                String localBody = "Hello <b>" + event.getUsername() + "</b>,\n\n" +
                        "Welcome to ArthGrid! Your account has been created and we've added a <b>Rs. 5000 welcome balance</b> to it.\n\n" +
                        "Log in with <b>" + event.getEmail() + "</b> and the password you chose at sign-up:\n\n" +
                        "<div style='margin: 20px 0;'><a href='" + event.getResetLink() + "' style='background-color: #fbbf24; color: #0B1120; padding: 12px 25px; text-decoration: none; font-weight: bold; border-radius: 5px; display: inline-block;'>Open ArthGrid</a></div>\n" +
                        "<i style='color:#666; font-size:12px;'>This is a demo project - balances are only numbers in our database, no real money is involved.</i>\n\n" +
                        "Regards,\nArthGrid Bank";
                emailService.sendPaymentNotification(event.getEmail(), "Welcome to ArthGrid!", localBody);
                System.out.println("Welcome Email Sent to: " + event.getEmail());
                return;
            }

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