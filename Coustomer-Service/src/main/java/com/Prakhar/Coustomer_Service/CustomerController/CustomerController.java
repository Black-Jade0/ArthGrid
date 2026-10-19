package com.Prakhar.Coustomer_Service.CustomerController;


import com.Prakhar.Coustomer_Service.DTOs.CustomerRequest;
import com.Prakhar.Coustomer_Service.DTOs.CustomerResponse;
import com.Prakhar.Coustomer_Service.Service.Customer_Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {


    private Customer_Service customer_service;
    private HttpServletRequest httpServletRequest;

    public CustomerController(Customer_Service customer_service, HttpServletRequest httpServletRequest) {
        this.customer_service = customer_service;
        this.httpServletRequest = httpServletRequest;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse>createCustomer(@RequestBody @Valid CustomerRequest customerRequest) throws NoSuchAlgorithmException {


        CustomerResponse customerResponse= customer_service.CreateCustomer(customerRequest);

        return new ResponseEntity<>(customerResponse, HttpStatus.CREATED);

    }


    @GetMapping("/{externalId}")
    public ResponseEntity<CustomerResponse>getCustomer(@PathVariable String externalId)
      {

        CustomerResponse customerResponse= customer_service.getCustomer(externalId);

        return ResponseEntity.ok(customerResponse);

      }

        @PatchMapping("/{externalId}/kyc")
        public ResponseEntity<CustomerResponse> verifyKyc(@PathVariable String externalId) {


            CustomerResponse customerResponse = customer_service.verifyCustomerKyc(externalId);

            return ResponseEntity.ok(customerResponse);


        }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {



        String auth0Id = jwt.getSubject();

        CustomerResponse customerResponse = customer_service.getCustomer(auth0Id);


        return ResponseEntity.ok(customerResponse);
    }
}
