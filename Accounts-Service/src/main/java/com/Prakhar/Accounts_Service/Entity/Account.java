package com.Prakhar.Accounts_Service.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="accounts")
public class Account {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

     @Column(nullable = false)
     private String customerExternalId;

     @Column(unique = true ,nullable = false)
     private String accountNumber;

     @Column(nullable = false) // asli paisa Available balance-Current Balance-Total Holds;
     private BigDecimal currentBalance;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private Currency currency;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private AccountStatus status;   //active Frozen closed

    @Version
    private Long version;  // optimistic locking ke li ye hai yeh

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
