package com.scotwest.banking.controller;

import com.scotwest.banking.dto.TransferRequest;
import com.scotwest.banking.dto.TransferResponse;
import com.scotwest.banking.entity.Transaction;
import com.scotwest.banking.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> transferFunds(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.executeTransfer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<List<Transaction>> getHistory(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transferService.getAccountHistory(accountNumber));
    }
}
