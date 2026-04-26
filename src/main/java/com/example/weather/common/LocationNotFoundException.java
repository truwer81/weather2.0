package com.example.weather.common;

public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(Long id) {
        super("Location with id " + id + " not found");
    }
}