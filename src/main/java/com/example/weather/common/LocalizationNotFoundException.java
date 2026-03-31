package com.example.weather.common;

public class LocalizationNotFoundException extends RuntimeException {

    public LocalizationNotFoundException(Long id) {
        super("Localization with id " + id + " not found");
    }
}