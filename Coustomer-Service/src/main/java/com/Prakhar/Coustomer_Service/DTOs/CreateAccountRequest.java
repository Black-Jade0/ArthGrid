package com.Prakhar.Coustomer_Service.DTOs;

import jakarta.validation.constraints.NotBlank;

import java.util.Currency;

public class CreateAccountRequest {

    @NotBlank(message = "Customer Id is required")
    private String customerExternalId;

    private String currency;

    public String getCustomerExternalId() {
        return customerExternalId;
    }

    public void setCustomerExternalId(String customerExternalId) {
        this.customerExternalId = customerExternalId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
