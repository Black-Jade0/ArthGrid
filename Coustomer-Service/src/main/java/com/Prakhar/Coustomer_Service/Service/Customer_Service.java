package com.Prakhar.Coustomer_Service.Service;


import com.Prakhar.Coustomer_Service.Client.AccountClient;
import com.Prakhar.Coustomer_Service.Client.AuthUserClient;
import com.Prakhar.Coustomer_Service.DTOs.AccountResponse;
import com.Prakhar.Coustomer_Service.DTOs.CreateAccountRequest;
import com.Prakhar.Coustomer_Service.DTOs.CustomerRequest;
import com.Prakhar.Coustomer_Service.DTOs.CustomerResponse;
import com.Prakhar.Coustomer_Service.Entity.Customer;
import com.Prakhar.Coustomer_Service.Entity.KycStatus;
import com.Prakhar.Coustomer_Service.repo.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Service
public class Customer_Service {

    private CustomerRepository customerRepository;

    private AccountClient accountClient;

    public Customer_Service(CustomerRepository customerRepository, AccountClient accountClient, AuthUserClient authUserClient) {
        this.customerRepository = customerRepository;
        this.accountClient = accountClient;
        this.authUserClient = authUserClient;
    }

    public CustomerResponse CreateCustomer(CustomerRequest customerRequest) throws NoSuchAlgorithmException {
        Optional<Customer> existingCustomer = customerRepository.findByExternalId(customerRequest.getExternalId());
        if (existingCustomer.isPresent()) {
            return mapToResponse(existingCustomer.get());
        }

        if (customerRepository.existsByEmail(customerRequest.getEmail())) {
            throw new RuntimeException("Email already Exists" + customerRequest.getEmail());
        }

        String fingerprint = generateFingerprint(customerRequest);

        Customer newcustomer = new Customer();
        newcustomer.setFirstName(customerRequest.getFirstName());
        newcustomer.setLastName(customerRequest.getLastName());
        newcustomer.setEmail(customerRequest.getEmail());
        newcustomer.setExternalId(customerRequest.getExternalId());
        newcustomer.setAddress(customerRequest.getAddress());
        newcustomer.setPhone(customerRequest.getPhone());
        newcustomer.setActive(true);
        newcustomer.setKycStatus(KycStatus.PENDING);
        newcustomer.setRequestFingerprint(fingerprint);

        Customer savedCustomer = customerRepository.save(newcustomer);

        return verifyCustomerKyc(newcustomer.getExternalId());


    }

    private CustomerResponse mapToResponse(Customer customer) {
        CustomerResponse customerResponse = new CustomerResponse();
        customerResponse.setId(customer.getId());
        customerResponse.setExternalId(customer.getExternalId());
        customerResponse.setFirstName(customer.getFirstName());
        customerResponse.setLastName(customer.getLastName());
        customerResponse.setAddress(customer.getAddress());
        customerResponse.setEmail(customer.getEmail());
        customerResponse.setPhone(customer.getPhone());
        customerResponse.setKycStatus(customer.getKycStatus());
        customerResponse.setActive(customer.isActive());
        customerResponse.setAccountNumber(customer.getAccountNumber());

        return customerResponse;

    }

    private String generateFingerprint(CustomerRequest customerRequest) throws NoSuchAlgorithmException {
        try {
            String raw = customerRequest.getEmail() + "|" + customerRequest.getExternalId();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte hash[] = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating fingerprint", e);
        }

    }

    public CustomerResponse getCustomer(String externalId) {
        Customer customer = customerRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer Not Found: " + externalId));

        return mapToResponse(customer);

    }


    private AuthUserClient authUserClient;


    @Transactional
    public CustomerResponse verifyCustomerKyc(String externalId) {
        Customer customer = customerRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Customer Not found" + externalId));

        customer.setKycStatus(KycStatus.VERIFIED);

        Customer savedCustomer = customerRepository.save(customer);

        CustomerResponse customerResponse = mapToResponse(savedCustomer);

        try {
            String realAuth0Id = authUserClient.createUser(customerResponse);
            if (realAuth0Id != null) {
                //YAHAN HAI MAIN LOGIC: Purani ID update kar do
                customer.setExternalId(realAuth0Id);
            }
        } catch (Exception e) {
            System.out.println("Failed to create User in Auth Service" + e.getMessage());
        }


        try {
            CreateAccountRequest createAccountRequest = new CreateAccountRequest();
            createAccountRequest.setCustomerExternalId(customer.getExternalId());
            createAccountRequest.setCurrency("INR");
            AccountResponse accountResponse = accountClient.createAccount(createAccountRequest);
            customer.setAccountNumber(accountResponse.getAccountNumber());
            savedCustomer = customerRepository.save(customer);
        } catch (Exception e) {
            System.out.println("Failed to create account" + e.getMessage());
            throw new RuntimeException("Account Creation Failed");
        }

        return mapToResponse(savedCustomer);
    }
}
