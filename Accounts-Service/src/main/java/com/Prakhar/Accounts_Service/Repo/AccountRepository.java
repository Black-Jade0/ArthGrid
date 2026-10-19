package com.Prakhar.Accounts_Service.Repo;

import com.Prakhar.Accounts_Service.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {

    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByAccountNumber(String accountNumber);
}
