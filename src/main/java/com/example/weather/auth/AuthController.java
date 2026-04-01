package com.example.weather.auth;

import com.example.weather.auth.dto.AuthMeResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/api/auth/me")
    public AuthMeResponse me(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return new AuthMeResponse(false, null, java.util.List.of());
        }

        return new AuthMeResponse(
                true,
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .toList()
        );
    }
}