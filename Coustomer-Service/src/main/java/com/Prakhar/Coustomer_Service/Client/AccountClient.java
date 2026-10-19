package com.Prakhar.Coustomer_Service.Client;


import com.Prakhar.Coustomer_Service.CustomerConfig.FeignClientConfig;
import com.Prakhar.Coustomer_Service.DTOs.AccountResponse;
import com.Prakhar.Coustomer_Service.DTOs.CreateAccountRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name="ACCOUNTS-SERVICE",
        url = "${ACCOUNTS_SERVICE_URL:http://localhost:8083}",
        configuration = FeignClientConfig.class
)
public interface AccountClient {

    @PostMapping("/api/v1/accounts")
    AccountResponse createAccount(@RequestBody CreateAccountRequest request);
}
