package com.Prakhar.Payment_Service.Client;


import com.Prakhar.Payment_Service.Config.FeignClientConfig;
import com.Prakhar.Payment_Service.DTO.DepositRequest;
import com.Prakhar.Payment_Service.DTO.HoldRequest;
import com.Prakhar.Payment_Service.DTO.WithDrawalRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name="ACCOUNTS-SERVICE",
        url = "${ACCOUNTS_SERVICE_URL:http://localhost:8083}",
        configuration = FeignClientConfig.class
)
public interface AccountsClient {

    @PostMapping("/api/v1/accounts/hold")
    void createHold(@RequestBody HoldRequest holdRequest);

    @PostMapping("/api/v1/accounts/hold/{holdId}/capture")
    void captureHold(@PathVariable String holdId);

    @PostMapping("/api/v1/accounts/hold/{holdId}/release")
    void releaseHold (@PathVariable String holdId);

    @PostMapping("/api/v1/accounts/withdraw")
    Object withdraw(@RequestBody WithDrawalRequest request);

    // Aapka controller "/api/v1/accounts/deposit" par hai aur RequestBody mangta hai
    @PostMapping("/api/v1/accounts/deposit")
    Object deposit(@RequestBody DepositRequest request);

}
