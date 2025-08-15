package com.example.bankapp.service;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- findAccountByUsername ----------
    @Test
    @DisplayName("findAccountByUsername should return account when found")
    void testFindAccountByUsername_Success() {
        Account acc = new Account();
        acc.setUsername("user1");
        when(accountRepository.findByUsername("user1")).thenReturn(Optional.of(acc));

        Account result = accountService.findAccountByUsername("user1");

        assertThat(result).isEqualTo(acc);
        verify(accountRepository).findByUsername("user1");
    }

    @Test
    @DisplayName("findAccountByUsername should throw when not found")
    void testFindAccountByUsername_NotFound() {
        when(accountRepository.findByUsername("missing")).thenReturn(Optional.empty());

        String username = "missing";
        assertThatThrownBy(() -> accountService.findAccountByUsername(username))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Account not found");
    }

    // ---------- registerAccount ----------
    @Test
    @DisplayName("registerAccount should save new account with encoded password")
    void testRegisterAccount_Success() {
        when(accountRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        Account saved = new Account();
        saved.setUsername("newuser");
        saved.setPassword("encodedPass");
        saved.setBalance(BigDecimal.ZERO);

        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        Account result = accountService.registerAccount("newuser", "pass");

        assertThat(result.getPassword()).isEqualTo("encodedPass");
        assertThat(result.getBalance()).isEqualTo(BigDecimal.ZERO);

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("registerAccount should throw when username exists")
    void testRegisterAccount_UsernameExists() {
        when(accountRepository.findByUsername("exists")).thenReturn(Optional.of(new Account()));

        String username = "exists";
        String password = "pass";
        assertThatThrownBy(() -> accountService.registerAccount(username, password))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("Username already exists");
    }

    // ---------- deposit ----------
    @Test
    void testDeposit() {
        Account acc = new Account();
        acc.setBalance(BigDecimal.valueOf(100));

        accountService.deposit(acc, BigDecimal.valueOf(50));

        assertThat(acc.getBalance()).isEqualTo(BigDecimal.valueOf(150));
        verify(accountRepository).save(acc);
        verify(transactionRepository).save(any(Transaction.class));
    }

    // ---------- withdraw ----------
    @Test
    void testWithdraw_Success() {
        Account acc = new Account();
        acc.setBalance(BigDecimal.valueOf(200));

        accountService.withdraw(acc, BigDecimal.valueOf(50));

        assertThat(acc.getBalance()).isEqualTo(BigDecimal.valueOf(150));
        verify(accountRepository).save(acc);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testWithdraw_InsufficientFunds() {
        Account acc = new Account();
        acc.setBalance(BigDecimal.valueOf(10));

        BigDecimal withdrawAmount = BigDecimal.valueOf(50);
        assertThatThrownBy(() -> accountService.withdraw(acc, withdrawAmount))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds");

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    // ---------- getTransactionHistory ----------
    @Test
    void testGetTransactionHistory() {
        Account acc = new Account();
        acc.setId(1L);

        List<Transaction> transactions = Arrays.asList(new Transaction(), new Transaction());
        when(transactionRepository.findByAccountId(1L)).thenReturn(transactions);

        List<Transaction> result = accountService.getTransactionHistory(acc);
        assertThat(result).hasSize(2);
    }

    // ---------- loadUserByUsername ----------
    @Test
    void testLoadUserByUsername_Success() {
        Account acc = new Account();
        acc.setUsername("userX");
        acc.setPassword("pass");
        acc.setBalance(BigDecimal.ZERO);

        when(accountRepository.findByUsername("userX")).thenReturn(Optional.of(acc));

        UserDetails ud = accountService.loadUserByUsername("userX");

        assertThat(ud.getUsername()).isEqualTo("userX");
        assertThat(ud.getAuthorities()).extracting("authority").contains("USER");
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(accountRepository.findByUsername("missing")).thenReturn(Optional.empty());

        String username = "missing";
        assertThatThrownBy(() -> accountService.loadUserByUsername(username))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Account not found");
    }

    // ---------- transferAmount ----------
    @Test
    void testTransferAmount_Success() {
        Account from = new Account();
        from.setUsername("from");
        from.setBalance(BigDecimal.valueOf(500));

        Account to = new Account();
        to.setUsername("to");
        to.setBalance(BigDecimal.valueOf(200));

        when(accountRepository.findByUsername("to")).thenReturn(Optional.of(to));

        BigDecimal transferAmount = BigDecimal.valueOf(100);
        accountService.transferAmount(from, "to", transferAmount);

        assertThat(from.getBalance()).isEqualTo(BigDecimal.valueOf(400));
        assertThat(to.getBalance()).isEqualTo(BigDecimal.valueOf(300));

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void testTransferAmount_InsufficientFunds() {
        Account from = new Account();
        from.setBalance(BigDecimal.valueOf(50));

        BigDecimal transferAmount = BigDecimal.valueOf(100);
        assertThatThrownBy(() -> accountService.transferAmount(from, "toUser", transferAmount))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void testTransferAmount_RecipientNotFound() {
        Account from = new Account();
        from.setUsername("from");
        from.setBalance(BigDecimal.valueOf(500));

        when(accountRepository.findByUsername("missing")).thenReturn(Optional.empty());

        BigDecimal transferAmount = BigDecimal.valueOf(100);
        assertThatThrownBy(() -> accountService.transferAmount(from, "missing", transferAmount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Recipient account not found");
    }

    // ---------- getAllAccounts ----------
    @Test
    void testGetAllAccounts() {
        when(accountRepository.findAll()).thenReturn(Collections.singletonList(new Account()));

        List<Account> result = accountService.getAllAccounts();
        assertThat(result).hasSize(1);
    }

    // ---------- saveAccount ----------
    @Test
    void testSaveAccount() {
        Account acc = new Account();
        when(accountRepository.save(acc)).thenReturn(acc);

        Account result = accountService.saveAccount(acc);
        assertThat(result).isSameAs(acc);
    }

    // ---------- updateAccount ----------
    @Test
    void testUpdateAccount_Success() {
        Account existing = new Account();
        existing.setUsername("oldUser");

        Account updated = new Account();
        updated.setUsername("newUser");
        updated.setPassword("newPass");
        updated.setBalance(BigDecimal.valueOf(500));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.updateAccount(1L, updated);

        assertThat(result.getUsername()).isEqualTo("newUser");
        assertThat(result.getPassword()).isEqualTo("newPass");
        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(500));
    }

    @Test
    void testUpdateAccount_NotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        Account result = accountService.updateAccount(1L, new Account());
        assertThat(result).isNull();
    }

    // ---------- deleteAccount ----------
    @Test
    void testDeleteAccount() {
        accountService.deleteAccount(5L);
        verify(accountRepository).deleteById(5L);
    }

    // ---------- getAccountById ----------
    @Test
    void testGetAccountById_Found() {
        Account acc = new Account();
        when(accountRepository.findById(10L)).thenReturn(Optional.of(acc));

        Account result = accountService.getAccountById(10L);
        assertThat(result).isEqualTo(acc);
    }

    @Test
    void testGetAccountById_NotFound() {
        when(accountRepository.findById(10L)).thenReturn(Optional.empty());

        Account result = accountService.getAccountById(10L);
        assertThat(result).isNull();
    }
}
