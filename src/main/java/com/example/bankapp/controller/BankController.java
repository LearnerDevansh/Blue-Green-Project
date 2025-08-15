package com.example.bankapp.controller;

import com.example.bankapp.model.Account;
import com.example.bankapp.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final AccountService accountService;

    private static final String ACCOUNT = "account";
    private static final String ACCOUNTS = "accounts";
    private static final String ERROR = "error";
    private static final String NOT_FOUND = "Account not found";
    private static final String REDIRECT_HOME = "redirect:/";

    @GetMapping({"/", "/accounts"})
    public String home(Model model) {
        model.addAttribute(ACCOUNTS, accountService.getAllAccounts());
        return "home";
    }

    @GetMapping({"/add", "/accounts/add"})
    public String showAddForm(Model model) {
        model.addAttribute(ACCOUNT, new Account());
        return "addAccount";
    }

    @PostMapping({"/add", "/accounts/add"})
    public String addAccount(@ModelAttribute(ACCOUNT) Account account) {
        accountService.saveAccount(account);
        return REDIRECT_HOME;
    }

    @GetMapping({"/edit/{id}", "/accounts/edit/{id}"})
    public String showEditForm(@PathVariable Long id, Model model) {
        Account account = accountService.getAccountById(id);
        if (account == null) {
            model.addAttribute(ERROR, NOT_FOUND);
            return ERROR;
        }
        model.addAttribute(ACCOUNT, account);
        return "editAccount";
    }

    @PostMapping({"/edit/{id}", "/accounts/edit/{id}"})
    public String editAccount(@PathVariable Long id, @ModelAttribute(ACCOUNT) Account account) {
        accountService.updateAccount(id, account);
        return REDIRECT_HOME;
    }

    @GetMapping({"/delete/{id}", "/accounts/delete/{id}"})
    public String deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return REDIRECT_HOME;
    }

    @GetMapping({"/account/{id}", "/accounts/{id}"})
    public String viewAccount(@PathVariable Long id, Model model) {
        Account account = accountService.getAccountById(id);
        if (account == null) {
            model.addAttribute(ERROR, NOT_FOUND);
            return ERROR;
        }
        model.addAttribute(ACCOUNT, account);
        return "account";
    }
}
