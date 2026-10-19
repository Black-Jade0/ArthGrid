package com.Prakhar.Accounts_Service.Repo;

import com.Prakhar.Accounts_Service.Entity.AccountTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<AccountTransaction,Long> {


    List<AccountTransaction> findByAccountNumberOrderByTimeStampDesc(String accountNumber);

    Page<AccountTransaction> findByAccountNumber(String accountNumber ,Pageable pageable);
}
