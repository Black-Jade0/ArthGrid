package com.Prakhar.Payment_Service.Event;

import com.Prakhar.Payment_Service.Entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {


    private String eventType;   // payment_completed very important

    private String sourceAccountNumber;

    private BigDecimal amount;

    private String billerName;

    private String transactionId;

    private Status status;

    private String email;

    private String targetEmail; // Receiver Email

    private String targetAccountNumber;
}
