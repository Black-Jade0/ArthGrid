package com.Prakhar.Coustomer_Service.DTOs;

import java.math.BigDecimal;

public class AccountResponse {

    private String accountNumber;
    private String customerExternalId;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private String currency;
    private String status;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCustomerExternalId() {
        return customerExternalId;
    }

    public void setCustomerExternalId(String customerExternalId) {
        this.customerExternalId = customerExternalId;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
