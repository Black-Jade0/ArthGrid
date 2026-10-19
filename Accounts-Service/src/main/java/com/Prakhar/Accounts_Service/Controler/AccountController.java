package com.Prakhar.Accounts_Service.Controler;

import com.Prakhar.Accounts_Service.DTOs.*;
import com.Prakhar.Accounts_Service.Entity.AccountTransaction;
import com.Prakhar.Accounts_Service.Service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody @Valid CreateAccountRequest accountRequest)
    {
        AccountResponse response=accountService.createAccount(accountRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountResponse> depositMoney(@RequestBody DepositRequest depositRequest)
    {
        AccountResponse response=accountService.deposit(depositRequest.getAccountNumber(),depositRequest.getAmount());
        return ResponseEntity.ok(response);

    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountResponse> withdrawMoney(@RequestBody WithdrawRequest withdrawRequest)
    {
        AccountResponse response=accountService.withdraw(withdrawRequest.getAccountNumber(),withdrawRequest.getAmount());
        return ResponseEntity.ok(response);

    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber)
    {
        AccountResponse response=accountService.getAccountDetails(accountNumber);
        return ResponseEntity.ok(response);
    }

   /* @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<AccountTransaction>> getStatement(@PathVariable String accountNumber)
    {
        List<AccountTransaction> response=accountService.getAccountStatement(accountNumber);
        return ResponseEntity.ok(response);
    } */

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<Page<AccountTransaction>> getStatement1(@PathVariable String accountNumber,@RequestParam(defaultValue ="0") int page,@RequestParam(defaultValue = "5") int size){

        return ResponseEntity.ok(accountService.getAccountStatement1(accountNumber,page,size));
    }


    @PostMapping("/hold")
    public ResponseEntity<AccountResponse> createHold(@RequestBody HoldRequest holdRequest)
    {
        AccountResponse response=accountService.createHold(holdRequest);
        return ResponseEntity.ok(response);

    }

     @PostMapping("/hold/{holdId}/capture")
    public ResponseEntity<AccountResponse>captureHold(@PathVariable String holdId)
    {
        return ResponseEntity.ok(accountService.captureHold(holdId));
    }

    @PostMapping("/hold/{holdId}/release")
    public ResponseEntity<AccountResponse>releaseHold(@PathVariable String holdId)
    {
        return ResponseEntity.ok(accountService.releaseHold(holdId));
    }

}
