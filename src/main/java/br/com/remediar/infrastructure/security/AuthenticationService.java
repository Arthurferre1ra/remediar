package br.com.remediar.infrastructure.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthenticationResult login(String username, String password) {
        UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(username, password);
        UserPrincipal principal = (UserPrincipal) authenticationManager.authenticate(credentials).getPrincipal();

        return new AuthenticationResult(
                jwtService.generateToken(principal),
                "Bearer",
                jwtService.accessTokenTtlSeconds(),
                principal.id(),
                principal.getUsername(),
                principal.role(),
                principal.actorDocument()
        );
    }
}
