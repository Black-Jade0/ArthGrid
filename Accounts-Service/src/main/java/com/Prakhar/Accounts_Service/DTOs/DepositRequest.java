package com.Prakhar.Accounts_Service.DTOs;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {

    private String accountNumber;
    private BigDecimal amount;
    private String description;   // optional, shown in the statement (e.g. "Received from Asha Verma")
}
