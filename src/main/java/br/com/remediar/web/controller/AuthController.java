package br.com.remediar.web.controller;

import br.com.remediar.infrastructure.security.AuthenticationService;
import br.com.remediar.web.dto.auth.AuthLoginRequest;
import br.com.remediar.web.dto.auth.AuthTokenResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return AuthTokenResponse.from(authenticationService.login(request.username(), request.password()));
    }
}
