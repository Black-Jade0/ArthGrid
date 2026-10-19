package com.Prakhar.Accounts_Service.DTOs;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HoldRequest {

    private String accountNumber;
    private BigDecimal amount;
    private String description; // eg.Hotel booking , ya phir amazon order
    private String referenceId;
}
