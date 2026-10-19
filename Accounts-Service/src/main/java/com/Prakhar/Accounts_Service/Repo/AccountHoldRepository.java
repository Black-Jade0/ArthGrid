package com.Prakhar.Accounts_Service.Repo;

import com.Prakhar.Accounts_Service.Entity.AccountHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountHoldRepository extends JpaRepository<AccountHold ,Long> {

    @Query("SELECT COALESCE(SUM(h.amount), 0) FROM AccountHold h WHERE h.account.id = :accountId AND h.status = 'ACTIVE'")
    BigDecimal calculateTotalHolds(@Param("accountId") Long accountId);


    Optional<AccountHold> findByReferenceId(String referenceId);
}
