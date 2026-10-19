package com.Prakhar.Accounts_Service.DTOs;

import com.Prakhar.Accounts_Service.Entity.AccountStatus;
import com.Prakhar.Accounts_Service.Entity.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountResponse {

    private String accountNumber;
    private String customerExternalId;
    private BigDecimal currentBalance;  // total Balance
    private BigDecimal availableBalance; // Total-hold
    private Currency currency;
    private AccountStatus status;
}
