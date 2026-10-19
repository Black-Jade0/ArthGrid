package com.Prakhar.Accounts_Service.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="accounts_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber; // kis account se paisa aaya gya

    private BigDecimal amount;

    private String transactionType;     // Deposit,Withdraw,Hold_Reserve

    private String referenceId;    // UniqueId for Tracking

    private String description;

    @CreationTimestamp
    private LocalDateTime timeStamp;



}
