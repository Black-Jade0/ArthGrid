package com.Prakhar.Accounts_Service.DTOs;


import com.Prakhar.Accounts_Service.Entity.Currency;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotBlank(message = "Customer Id is required")
    private String customerExternalId;       // checking kiska account hai

    private Currency currency= Currency.INR;  // making currency to Inr by default
}
