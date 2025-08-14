package com.example.bankapp.controller;

import com.example.bankapp.model.Account;
import com.example.bankapp.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BankControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private BankController bankController;

    private MockMvc mockMvc;

    private Account account1;
    private Account account2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bankController).build();

        account1 = new Account();
        account1.setUsername("John Doe");
        account1.setPassword("pass");
        account1.setBalance(BigDecimal.valueOf(1000));
        account1.setId(1L);

        account2 = new Account();
        account2.setUsername("Jane Smith");
        account2.setPassword("pass2");
        account2.setBalance(BigDecimal.valueOf(2000));
        account2.setId(2L);
    }

    @Test
    void testHome() throws Exception {
        when(accountService.getAllAccounts()).thenReturn(Arrays.asList(account1, account2));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("accounts"));

        verify(accountService, times(1)).getAllAccounts();
    }

    @Test
    void testShowAddForm() throws Exception {
        mockMvc.perform(get("/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("addAccount"))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    void testAddAccount() throws Exception {
        mockMvc.perform(post("/add")
                        .param("username", "New User")
                        .param("password", "pass")
                        .param("balance", "500"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(accountService, times(1)).saveAccount(any(Account.class));
    }

    @Test
    void testShowEditForm_Found() throws Exception {
        when(accountService.getAccountById(1L)).thenReturn(account1);

        mockMvc.perform(get("/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editAccount"))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    void testShowEditForm_NotFound() throws Exception {
        when(accountService.getAccountById(1L)).thenReturn(null);

        mockMvc.perform(get("/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("error", "Account not found"));
    }

    @Test
    void testEditAccount() throws Exception {
        mockMvc.perform(post("/edit/1")
                        .param("username", "Updated User")
                        .param("password", "pass")
                        .param("balance", "700"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(accountService, times(1)).updateAccount(eq(1L), any(Account.class));
    }

    @Test
    void testDeleteAccount() throws Exception {
        mockMvc.perform(get("/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(accountService, times(1)).deleteAccount(1L);
    }

    @Test
    void testViewAccount_Found() throws Exception {
        when(accountService.getAccountById(1L)).thenReturn(account1);

        mockMvc.perform(get("/account/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("account"))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    void testViewAccount_NotFound() throws Exception {
        when(accountService.getAccountById(1L)).thenReturn(null);

        mockMvc.perform(get("/account/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("error", "Account not found"));
    }
}
