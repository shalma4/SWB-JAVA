package com.scotwest.banking.service;

import com.scotwest.banking.dto.TransferRequest;
import com.scotwest.banking.dto.TransferResponse;
import com.scotwest.banking.entity.Account;
import com.scotwest.banking.entity.Transaction;
import com.scotwest.banking.exception.BusinessRuleException;
import com.scotwest.banking.exception.InsufficientFundsException;
import com.scotwest.banking.exception.ResourceNotFoundException;
import com.scotwest.banking.repository.AccountRepository;
import com.scotwest.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransferResponse executeTransfer(TransferRequest request) {
        if (request.getSourceAccountNumber().equals(request.getTargetAccountNumber())) {
            throw new BusinessRuleException("Cannot transfer funds to the same account");
        }

        Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found: " + request.getSourceAccountNumber()));

        Account targetAccount = accountRepository.findByAccountNumber(request.getTargetAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Target account not found: " + request.getTargetAccountNumber()));

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(
                "Insufficient funds in account " + request.getSourceAccountNumber() + 
                ". Current balance: £" + sourceAccount.getBalance() + ", Requested: £" + request.getAmount()
            );
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        targetAccount.setBalance(targetAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        Transaction transaction = Transaction.builder()
                .transactionReference(UUID.randomUUID().toString())
                .sourceAccountNumber(request.getSourceAccountNumber())
                .targetAccountNumber(request.getTargetAccountNumber())
                .amount(request.getAmount())
                .status("SUCCESS")
                .description(request.getDescription() != null ? request.getDescription() : "Standard Transfer")
                .build();

        Transaction savedTx = transactionRepository.save(transaction);

        return TransferResponse.builder()
                .transactionReference(savedTx.getTransactionReference())
                .sourceAccountNumber(savedTx.getSourceAccountNumber())
                .targetAccountNumber(savedTx.getTargetAccountNumber())
                .amount(savedTx.getAmount())
                .status(savedTx.getStatus())
                .description(savedTx.getDescription())
                .timestamp(savedTx.getTimestamp())
                .build();
    }

    public List<Transaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findBySourceAccountNumberOrTargetAccountNumberOrderByTimestampDesc(
                accountNumber, accountNumber
        );
    }
}
