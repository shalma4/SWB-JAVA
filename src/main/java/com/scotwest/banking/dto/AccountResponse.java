package com.scotwest.banking.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {
    private Long accountId;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private Long customerId;
    private String customerName;
    private LocalDateTime createdAt;
}
