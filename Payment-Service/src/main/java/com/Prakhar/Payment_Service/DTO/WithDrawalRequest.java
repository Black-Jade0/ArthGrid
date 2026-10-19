package com.Prakhar.Payment_Service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithDrawalRequest {

    private String accountNumber;
    private BigDecimal amount;
}
