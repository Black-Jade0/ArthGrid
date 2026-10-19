package com.Prakhar.Notification_service.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentEvent {

    private String eventType;
    private String sourceAccountNumber;
    private BigDecimal amount;
    private String billerName;
    private String transactionId;
    private String status;
    private String email;
    private String targetEmail;
    private String targetAccountNumber;
}
