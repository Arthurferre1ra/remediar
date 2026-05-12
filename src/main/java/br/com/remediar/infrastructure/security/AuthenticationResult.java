package br.com.remediar.infrastructure.security;

import br.com.remediar.domain.enums.UserRole;

public record AuthenticationResult(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String username,
        UserRole role,
        String actorDocument
) {
}
