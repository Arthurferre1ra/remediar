package br.com.remediar.web.dto.auth;

import br.com.remediar.infrastructure.security.AuthenticationResult;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        AuthUserResponse user
) {
    public static AuthTokenResponse from(AuthenticationResult result) {
        return new AuthTokenResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresIn(),
                new AuthUserResponse(
                        result.userId(),
                        result.username(),
                        result.role(),
                        result.actorDocument()
                )
        );
    }
}
