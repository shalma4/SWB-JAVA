package com.scotwest.banking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scotwest.banking.dto.AccountRequest;
import com.scotwest.banking.dto.AccountResponse;
import com.scotwest.banking.dto.CustomerRequest;
import com.scotwest.banking.dto.TransferRequest;
import com.scotwest.banking.entity.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BankingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("End-to-End: Register customer, open accounts, transfer funds, audit ledger")
    void testFullBankingLifecycle() throws Exception {
        // 1. Create Sender (Anne)
        CustomerRequest anneReq = new CustomerRequest();
        anneReq.setFirstName("Anne");
        anneReq.setLastName("MacLeod");
        anneReq.setEmail("anne.test@scotwest.co.uk");
        anneReq.setPhoneNumber("+441412211000");

        MvcResult anneResult = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anneReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Customer anne = objectMapper.readValue(anneResult.getResponse().getContentAsString(), Customer.class);

        // 2. Create Receiver (Callum)
        CustomerRequest callumReq = new CustomerRequest();
        callumReq.setFirstName("Callum");
        callumReq.setLastName("Fraser");
        callumReq.setEmail("callum.test@scotwest.co.uk");
        callumReq.setPhoneNumber("+441412212000");

        MvcResult callumResult = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callumReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Customer callum = objectMapper.readValue(callumResult.getResponse().getContentAsString(), Customer.class);

        // 3. Open Account for Anne (£500)
        AccountRequest acc1Req = new AccountRequest();
        acc1Req.setCustomerId(anne.getId());
        acc1Req.setAccountType("SAVINGS");
        acc1Req.setInitialDeposit(new BigDecimal("500.00"));

        MvcResult acc1Result = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acc1Req)))
                .andExpect(status().isCreated())
                .andReturn();

        AccountResponse acc1 = objectMapper.readValue(acc1Result.getResponse().getContentAsString(), AccountResponse.class);

        // 4. Open Account for Callum (£100)
        AccountRequest acc2Req = new AccountRequest();
        acc2Req.setCustomerId(callum.getId());
        acc2Req.setAccountType("CHECKING");
        acc2Req.setInitialDeposit(new BigDecimal("100.00"));

        MvcResult acc2Result = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acc2Req)))
                .andExpect(status().isCreated())
                .andReturn();

        AccountResponse acc2 = objectMapper.readValue(acc2Result.getResponse().getContentAsString(), AccountResponse.class);

        // 5. Transfer £150 from Anne to Callum
        TransferRequest transferReq = new TransferRequest();
        transferReq.setSourceAccountNumber(acc1.getAccountNumber());
        transferReq.setTargetAccountNumber(acc2.getAccountNumber());
        transferReq.setAmount(new BigDecimal("150.00"));
        transferReq.setDescription("Test integration transfer");

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.amount", is(150.00)));

        // 6. Verify Anne's balance updated to £350
        mockMvc.perform(get("/api/v1/accounts/" + acc1.getAccountNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(350.00)));

        // 7. Verify Callum's balance updated to £250
        mockMvc.perform(get("/api/v1/accounts/" + acc2.getAccountNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(250.00)));

        // 8. Negative Test: Attempt overdraft transfer (£1000 from Anne's £350 balance)
        TransferRequest overdraftReq = new TransferRequest();
        overdraftReq.setSourceAccountNumber(acc1.getAccountNumber());
        overdraftReq.setTargetAccountNumber(acc2.getAccountNumber());
        overdraftReq.setAmount(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overdraftReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INSUFFICIENT_FUNDS")));
    }
}
