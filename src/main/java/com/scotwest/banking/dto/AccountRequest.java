package com.scotwest.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Account type is required (e.g. SAVINGS, CHECKING)")
    private String accountType;

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Initial deposit cannot be negative")
    private BigDecimal initialDeposit;
}
