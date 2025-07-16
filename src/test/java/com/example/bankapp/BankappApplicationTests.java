package com.example.bankapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.TestPropertySource;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
// @TestPropertySource(locations = "classpath:application-test.properties")

public class BankappApplicationTests {

    @Test
    void contextLoads() {
    }
}

