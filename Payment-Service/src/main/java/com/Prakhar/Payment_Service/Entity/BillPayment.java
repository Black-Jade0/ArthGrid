package com.Prakhar.Payment_Service.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="bill_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String sourceAccountNumber;

    private BigDecimal amount;

    private String billerName;    //kisko paisa de rhe hai

    private String billReferenceNumber;  // bill number

    @Enumerated(EnumType.STRING)
    private Status status;         // INITIATED , SETTLED , FAILED ,  SUCCESS


    private String transactionReferenceId;   // tracking ke liye

    private String reason;   // agr fail hua toh ku hua

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;







}
