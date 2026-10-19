package com.Prakhar.Notification_service.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_entry")
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class NotificationEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private BigDecimal amount;
    private String status;
    private String emailSentTo;
    private LocalDateTime sentAt;

    public NotificationEntry(String transactionId, BigDecimal amount, String status, String emailSentTo) {
        this.transactionId = transactionId;
        this.amount=amount;
        this.status = status;
        this.emailSentTo = emailSentTo;
        this.sentAt = LocalDateTime.now();
    }
}
