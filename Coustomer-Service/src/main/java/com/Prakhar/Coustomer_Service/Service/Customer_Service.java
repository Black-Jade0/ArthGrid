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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        String fingerprint = generateFingerprint(customerRequest);

        Customer newcustomer = new Customer();
        newcustomer.setFirstName(customerRequest.getFirstName());
        newcustomer.setLastName(customerRequest.getLastName());
        newcustomer.setEmail(customerRequest.getEmail());
        newcustomer.setExternalId(customerRequest.getExternalId());
        newcustomer.setAddress(customerRequest.getAddress() == null ? "" : customerRequest.getAddress());
        newcustomer.setDateOfBirth(blankToNull(customerRequest.getDateOfBirth()));
        newcustomer.setCity(blankToNull(customerRequest.getCity()));
        newcustomer.setState(blankToNull(customerRequest.getState()));
        newcustomer.setPostalCode(blankToNull(customerRequest.getPostalCode()));
        newcustomer.setOccupation(blankToNull(customerRequest.getOccupation()));
        if (customerRequest.getPin() != null && !customerRequest.getPin().isBlank()) {
            newcustomer.setPinHash(passwordEncoder.encode(customerRequest.getPin()));
        }
        // Phone is optional. Store "" instead of null so it also works on databases created before this change
        newcustomer.setPhone(customerRequest.getPhone() == null ? "" : customerRequest.getPhone());
        newcustomer.setActive(true);
        newcustomer.setKycStatus(KycStatus.PENDING);
        newcustomer.setRequestFingerprint(fingerprint);
        if (customerRequest.getPassword() != null && !customerRequest.getPassword().isBlank()) {
            newcustomer.setPasswordHash(passwordEncoder.encode(customerRequest.getPassword()));
        }

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
        customerResponse.setDateOfBirth(customer.getDateOfBirth());
        customerResponse.setCity(customer.getCity());
        customerResponse.setState(customer.getState());
        customerResponse.setPostalCode(customer.getPostalCode());
        customerResponse.setOccupation(customer.getOccupation());
        customerResponse.setMemberSince(customer.getCreatedAt());

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

    /** Frontend login: email + password -> the customer's profile (incl. account number). */
    public CustomerResponse login(String email, String password) {
        Customer customer = customerRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password"));
        if (customer.getPasswordHash() == null || password == null
                || !passwordEncoder.matches(password, customer.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
        }
        return mapToResponse(customer);
    }

    /** Checks the 4-digit transaction PIN before a transfer. Throws 401 if it is wrong. */
    public void verifyPin(String email, String pin) {
        Customer customer = customerRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect PIN"));
        if (customer.getPinHash() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This account has no transaction PIN. Open a new account to set one.");
        }
        if (pin == null || !passwordEncoder.matches(pin, customer.getPinHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect PIN");
        }
    }

    private static String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    /** Find the person you want to send money to, by their email. Returns only what the sender needs. */
    public CustomerResponse lookupByEmail(String email) {
        Customer customer = customerRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No ArthGrid user with this email"));
        CustomerResponse r = new CustomerResponse();
        r.setFirstName(customer.getFirstName());
        r.setLastName(customer.getLastName());
        r.setEmail(customer.getEmail());
        r.setAccountNumber(customer.getAccountNumber());
        r.setActive(customer.isActive());
        return r;
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
