package com.scotwest.banking.service;

import com.scotwest.banking.dto.AccountRequest;
import com.scotwest.banking.dto.AccountResponse;
import com.scotwest.banking.entity.Account;
import com.scotwest.banking.entity.Customer;
import com.scotwest.banking.repository.AccountRepository;
import com.scotwest.banking.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public AccountResponse openAccount(AccountRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + request.getCustomerId()));

        String generatedAccountNumber = "SWB" + (10000000 + new Random().nextInt(90000000));

        Account account = Account.builder()
                .accountNumber(generatedAccountNumber)
                .accountType(request.getAccountType().toUpperCase())
                .balance(request.getInitialDeposit())
                .customer(customer)
                .build();

        Account saved = accountRepository.save(account);

        return mapToResponse(saved);
    }

    public List<AccountResponse> getAccountsByCustomer(Long customerId) {
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));
        return mapToResponse(account);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .customerId(account.getCustomer().getId())
                .customerName(account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
