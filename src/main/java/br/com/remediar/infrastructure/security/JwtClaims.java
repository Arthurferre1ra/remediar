package br.com.remediar.infrastructure.security;

import br.com.remediar.domain.enums.UserRole;
import java.time.Instant;

public record JwtClaims(
        Long userId,
        String username,
        UserRole role,
        String actorDocument,
        Instant expiresAt
) {
}
