package com.example.weather.auth.dto;

import java.util.List;

public record AuthMeResponse(
        boolean authenticated,
        String username,
        List<String> roles
) {
}