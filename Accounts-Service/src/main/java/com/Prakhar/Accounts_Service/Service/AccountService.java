package com.Prakhar.Accounts_Service.Service;

import com.Prakhar.Accounts_Service.DTOs.AccountResponse;
import com.Prakhar.Accounts_Service.DTOs.CreateAccountRequest;
import com.Prakhar.Accounts_Service.DTOs.HoldRequest;
import com.Prakhar.Accounts_Service.Entity.Account;
import com.Prakhar.Accounts_Service.Entity.AccountHold;
import com.Prakhar.Accounts_Service.Entity.AccountStatus;
import com.Prakhar.Accounts_Service.Entity.AccountTransaction;
import com.Prakhar.Accounts_Service.Repo.AccountHoldRepository;
import com.Prakhar.Accounts_Service.Repo.AccountRepository;
import com.Prakhar.Accounts_Service.Repo.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private final AccountHoldRepository accountHoldRepository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest accountRequest)
    {
        String accountNumber=generateAccountNumber();

        Account account=Account.builder()  // creating account object
                .customerExternalId(accountRequest.getCustomerExternalId())
                .accountNumber(accountNumber)
                .currentBalance(BigDecimal.ZERO)
                .currency(accountRequest.getCurrency())
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount= accountRepository.save(account); // saving in repository

        return mapToResponse(savedAccount);  // returning the dto response
    }

    private String generateAccountNumber(){

        return String.valueOf((long)(Math.random()*9000000000L)+1000000000L);
    }

    private AccountResponse mapToResponse(Account account)
    {
         return AccountResponse.builder()
                 .accountNumber(account.getAccountNumber())
                 .customerExternalId(account.getCustomerExternalId())
                 .currency(account.getCurrency())
                 .availableBalance(account.getCurrentBalance())
                 .currentBalance(account.getCurrentBalance())
                 .status(account.getStatus())
                 .build();
    }

    // Deposit Money

    @Transactional
    public AccountResponse deposit(String accountNumber,BigDecimal amount)
    {
        Account account= accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()->new RuntimeException("Account does not Exist"));

          account.setCurrentBalance(account.getCurrentBalance().add(amount)); // deposit money
          Account savedAccount=accountRepository.save(account); // save account

        AccountTransaction accountTransaction=AccountTransaction.builder()
                .accountNumber(account.getAccountNumber())
                .amount(amount)
                .transactionType("Deposit")
                .referenceId(UUID.randomUUID().toString())
                .description("Cash Deposit")
                .build();

        transactionRepository.save(accountTransaction);

        return mapToResponse(savedAccount);
    }


    @Transactional
    public AccountResponse withdraw(String accountNumber,BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account does not Exist"));

        BigDecimal totalHolds = accountHoldRepository.calculateTotalHolds(account.getId());

        BigDecimal availableBalance = account.getCurrentBalance().subtract(totalHolds); // yha versdion feild check karega ki koi aur toh check nhi kr rha hai

        if (availableBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient Funds Available" + availableBalance);
        }

        account.setCurrentBalance(account.getCurrentBalance().subtract(amount));

        Account savedAccount = accountRepository.save(account);

        AccountTransaction accountTransaction = AccountTransaction.builder()
                .accountNumber(account.getAccountNumber())
                .amount(amount.negate())
                .transactionType("Withdraw")
                .referenceId(UUID.randomUUID().toString())
                .description("Cash Withdraw")
                .build();

        transactionRepository.save(accountTransaction);

        return mapToResponse(savedAccount);
    }
    public AccountResponse getAccountDetails(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account does not Exist"));

        BigDecimal totalHolds = accountHoldRepository.calculateTotalHolds(account.getId());

        BigDecimal availableBalance = account.getCurrentBalance().subtract(totalHolds); // yha versdion feild check karega ki koi aur toh check nhi kr rha hai


       return AccountResponse.builder()
                .accountNumber(account.getAccountNumber())
                .customerExternalId(account.getCustomerExternalId())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .currentBalance(account.getCurrentBalance())   // Total Paisa
                .availableBalance(availableBalance)            // Kharch karne layak paisa
                .build();
    }


    public List<AccountTransaction> getAccountStatement(String accountNumber)
    {
        return transactionRepository.findByAccountNumberOrderByTimeStampDesc(accountNumber);
    }

    public Page<AccountTransaction> getAccountStatement1(String accountNumber,int page,int size)
    {
        Pageable pageable= PageRequest.of(page,size, Sort.by("timeStamp").descending());
        return transactionRepository.findByAccountNumber(accountNumber,pageable);
    }


    @Transactional
    public AccountResponse createHold(HoldRequest holdRequest) {
        Account account = accountRepository.findByAccountNumber(holdRequest.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account does not Exist"));

        BigDecimal totalHolds = accountHoldRepository.calculateTotalHolds(account.getId());

        BigDecimal availableBalance = account.getCurrentBalance().subtract(totalHolds); // yha versdion feild check karega ki koi aur toh check nhi kr rha hai

        if (availableBalance.compareTo(holdRequest.getAmount()) < 0) {
            throw new RuntimeException("Insufficient Funds Available to hold" + availableBalance);
        }

        AccountHold hold = AccountHold.builder()
                .account(account)
                .amount(holdRequest.getAmount())
                .status("Active")
                .referenceId(holdRequest.getReferenceId())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        accountHoldRepository.save(hold);

        BigDecimal newTotalHolds= totalHolds.add(holdRequest.getAmount());
        BigDecimal newAvailableBalance= account.getCurrentBalance().subtract(newTotalHolds);

        return AccountResponse.builder()
                .accountNumber(account.getAccountNumber())
                .customerExternalId(account.getCustomerExternalId())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .currentBalance(account.getCurrentBalance())
                .availableBalance(newAvailableBalance)
                .build();
    }



    @Transactional
    public AccountResponse captureHold(String holdReferenceId)
    {
        AccountHold hold=accountHoldRepository.findByReferenceId(holdReferenceId)
                .orElseThrow(()->new RuntimeException("Hold Does not Exist"));

          if(!"Active".equals(hold.getStatus()))
          {
              throw new RuntimeException("Hold is not Active");

          }

          Account account=hold.getAccount();
          hold.setStatus("Captured");
          accountHoldRepository.save(hold);

          account.setCurrentBalance(account.getCurrentBalance().subtract(hold.getAmount()));
          Account savedAccount=accountRepository.save(account);

          AccountTransaction transaction= AccountTransaction.builder()
                  .accountNumber(account.getAccountNumber())
                  .amount(hold.getAmount().negate())
                  .transactionType("Withdrawal_Held") // pta chle ye hold wala settelemnt tha
                  .referenceId(holdReferenceId)
                  .description("Hold Captured"+holdReferenceId)
                  .build();

          transactionRepository.save(transaction);

          BigDecimal totalActiveHolds=accountHoldRepository.calculateTotalHolds(savedAccount.getId());
          BigDecimal correctAvailableBalance= savedAccount.getCurrentBalance().subtract(totalActiveHolds);

        return AccountResponse.builder()
                .accountNumber(savedAccount.getAccountNumber())
                .customerExternalId(savedAccount.getCustomerExternalId())
                .currentBalance(savedAccount.getCurrentBalance())
                .availableBalance(correctAvailableBalance)
                .currency(savedAccount.getCurrency())
                .status(savedAccount.getStatus())
                .build();

    }


    @Transactional
    public AccountResponse releaseHold(String holdReferenceId) {
        AccountHold hold = accountHoldRepository.findByReferenceId(holdReferenceId)
                .orElseThrow(() -> new RuntimeException("Hold Does not Exist"));

        if (!"Active".equals(hold.getStatus())) {
            throw new RuntimeException("Hold is not Active");

        }
        hold.setStatus("Released");  // status change hote hi paise wapis avalibale balance mein dekhne lagege
                                     //   kyuki calculateHold kebal active walo ko ginta hai na bhai
        accountHoldRepository.save(hold);

     Account account= hold.getAccount();

     BigDecimal totalActiveHolds= accountHoldRepository.calculateTotalHolds(account.getId());

     BigDecimal correctAvailableBalance= account.getCurrentBalance().subtract(totalActiveHolds);

       return AccountResponse.builder()
               .accountNumber(account.getAccountNumber())
               .customerExternalId(account.getCustomerExternalId())
               .currentBalance(account.getCurrentBalance())
               .availableBalance(correctAvailableBalance)
               .currency(account.getCurrency())
               .status(account.getStatus())
               .build();
    }
}
