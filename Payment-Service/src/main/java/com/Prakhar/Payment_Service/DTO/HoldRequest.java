package com.Prakhar.Payment_Service.DTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class HoldRequest { // yeh hum account ko bhejege by feign client bhaiya hold lga de itne ka

    private String accountNumber;

    private BigDecimal amount;

    private String description;

    private String  referenceId;
}
