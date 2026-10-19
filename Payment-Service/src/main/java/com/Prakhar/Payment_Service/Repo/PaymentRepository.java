package com.Prakhar.Payment_Service.Repo;

import com.Prakhar.Payment_Service.Entity.BillPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<BillPayment ,Long> {

    BillPayment findBySourceAccountNumber(String accountNumber);
}
