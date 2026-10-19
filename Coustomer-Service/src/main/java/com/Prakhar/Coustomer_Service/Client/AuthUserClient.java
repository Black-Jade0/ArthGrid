package com.Prakhar.Coustomer_Service.Client;


import com.Prakhar.Coustomer_Service.DTOs.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "AUTH-SERVICE", url = "${AUTH_SERVICE_URL:http://localhost:8082}")
public interface AuthUserClient {


    @PostMapping("/api/v1/auth/users")
    String createUser(@RequestBody CustomerResponse customerResponse);
}
