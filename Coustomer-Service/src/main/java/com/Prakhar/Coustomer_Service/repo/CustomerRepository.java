package com.Prakhar.Coustomer_Service.repo;

import com.Prakhar.Coustomer_Service.Entity.Customer;
import jakarta.annotation.security.DeclareRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {

    boolean existsByEmail(String email);
    boolean existsByExternalId(String externalId);
    Optional<Customer> findByExternalId(String externalId);
    // 3. External ID se customer dhundo (Login ya Details dekhne ke liye)
    // Optional isliye kyuki ho sakta hai customer mile hi na
}
