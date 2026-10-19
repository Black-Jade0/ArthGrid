package com.Prakhar.Payment_Service.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillPaymentRequest {  //User Bhejega yeh

    private String sourceAccountNumber;
    private BigDecimal amount;
    private String billerName;
    private String billReferenceNumber;
    private String email;
    private String targetEmail;
    private String targetAccountNumber;
}
