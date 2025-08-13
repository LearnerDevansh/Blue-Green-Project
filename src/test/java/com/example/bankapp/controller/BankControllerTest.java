package com.example.bankapp.controller;

import com.example.bankapp.model.Account;
import com.example.bankapp.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.thymeleaf.prefix=classpath:/expected/",
        "spring.thymeleaf.cache=false"
})
class BankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @BeforeEach
    void stubService() {
        // Create mock account for /account/123
        Account acc = new Account();
        acc.setId(123L);
        acc.setUsername("demo");
        acc.setPassword("x");
        acc.setBalance(BigDecimal.valueOf(100));

        when(accountService.getAccountById(123L)).thenReturn(acc);
        when(accountService.getAccountById(999L)).thenReturn(null);
    }

    @Test
    void accountFoundPageShouldMatchExpectedHtml() throws Exception {
        String actualHtml = mockMvc.perform(get("/account/123"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expectedHtml = TestFileUtils.readFile("src/test/resources/expected/account.html");

        org.assertj.core.api.Assertions.assertThat(actualHtml.trim())
                .as("Account page HTML should match expected template output")
                .isEqualToIgnoringNewLines(expectedHtml.trim());
    }

    @Test
    void accountNotFoundPageShouldMatchExpectedHtml() throws Exception {
        String actualHtml = mockMvc.perform(get("/account/999"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expectedHtml = TestFileUtils.readFile("src/test/resources/expected/error.html");

        org.assertj.core.api.Assertions.assertThat(actualHtml.trim())
                .as("Error page HTML should match expected template output")
                .isEqualToIgnoringNewLines(expectedHtml.trim());
    }
}
