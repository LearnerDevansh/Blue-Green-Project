package com.example.bankapp.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class TestFileUtils {
    static String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read expected HTML file: " + path, e);
        }
    }
}
