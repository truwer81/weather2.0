package com.example.weather.localization;

public class LocalizationNotFoundException extends RuntimeException {
    public LocalizationNotFoundException(Long id) {
        super("Localization not found: " + id);
    }
}