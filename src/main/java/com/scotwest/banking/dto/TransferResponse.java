package com.scotwest.banking.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransferResponse {
    private String transactionReference;
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private BigDecimal amount;
    private String status;
    private String description;
    private LocalDateTime timestamp;
}
