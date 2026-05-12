package br.com.remediar.web.dto.auth;

import br.com.remediar.domain.enums.UserRole;

public record AuthUserResponse(
        Long id,
        String username,
        UserRole role,
        String actorDocument
) {
}
