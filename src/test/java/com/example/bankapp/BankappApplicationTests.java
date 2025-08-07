package com.example.bankapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;


import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
// @TestPropertySource(locations = "classpath:application-test.properties")

class BankappApplicationTests {

    @Test
    void contextLoads() {
        // This test will simply check if the application context loads successfully.
        // If there are any issues with the configuration, this test will fail.
        // No additional assertions are needed here.
    }
}

