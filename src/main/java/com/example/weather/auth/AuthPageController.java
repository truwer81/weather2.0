package com.example.weather.auth;

import com.example.weather.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthPageController {

    private final RegistrationService registrationService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            clearSensitiveFields(request);
            return "register";
        }

        try {
            registrationService.register(request);
        } catch (RegistrationService.RegistrationException ex) {
            bindingResult.rejectValue(ex.getField(), "registration." + ex.getField(), ex.getMessage());
            clearSensitiveFields(request);
            return "register";
        }

        return "redirect:/login?registered";
    }

    private void clearSensitiveFields(RegisterRequest request) {
        request.setPassword(null);
        request.setConfirmPassword(null);
    }
}
