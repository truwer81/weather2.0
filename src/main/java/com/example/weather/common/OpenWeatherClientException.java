package com.example.weather.common;

public class OpenWeatherClientException extends Exception {
    public OpenWeatherClientException(String message) {
        super(message);
    }

    public OpenWeatherClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
