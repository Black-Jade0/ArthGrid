package com.Prakhar.Accounts_Service.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "accounts_holds")
public class AccountHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id",nullable = false)
    private Account account;


    private BigDecimal amount;

    private String referenceId;


    private String status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;


}
