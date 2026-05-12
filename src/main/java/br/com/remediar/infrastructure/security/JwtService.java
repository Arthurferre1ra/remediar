package br.com.remediar.infrastructure.security;

import br.com.remediar.domain.enums.UserRole;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final JwtProperties properties;
    private final Clock clock;

    public JwtService(ObjectMapper objectMapper, JwtProperties properties, Clock clock) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.clock = clock;
    }

    public String generateToken(UserPrincipal principal) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(properties.getAccessTokenTtl());

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", principal.getUsername());
        payload.put("uid", principal.id());
        payload.put("role", principal.role().name());
        payload.put("actor_document", principal.actorDocument());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String unsignedToken = base64UrlJson(header) + "." + base64UrlJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public JwtClaims parse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new InvalidJwtException("Token JWT malformado.");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        if (!MessageDigest.isEqual(sign(unsignedToken).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new InvalidJwtException("Assinatura JWT invalida.");
        }

        Map<String, Object> payload = parsePayload(parts[1]);
        Instant expiresAt = Instant.ofEpochSecond(asLong(payload.get("exp")));
        if (!expiresAt.isAfter(Instant.now(clock))) {
            throw new InvalidJwtException("Token JWT expirado.");
        }

        return new JwtClaims(
                asLong(payload.get("uid")),
                asString(payload.get("sub")),
                asRole(payload.get("role")),
                asString(payload.get("actor_document")),
                expiresAt
        );
    }

    public long accessTokenTtlSeconds() {
        return properties.getAccessTokenTtl().toSeconds();
    }

    private String base64UrlJson(Map<String, Object> value) {
        try {
            return base64UrlEncode(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao serializar JWT.", exception);
        }
    }

    private Map<String, Object> parsePayload(String payload) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            return objectMapper.readValue(decoded, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new InvalidJwtException("Payload JWT invalido.");
        }
    }

    private String sign(String unsignedToken) {
        try {
            if (properties.getSecret() == null || properties.getSecret().isBlank()) {
                throw new IllegalStateException("Segredo JWT nao configurado.");
            }
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return base64UrlEncode(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao assinar JWT.", exception);
        }
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String asString(Object value) {
        if (value instanceof String string && !string.isBlank()) {
            return string;
        }
        throw new InvalidJwtException("Claim JWT obrigatoria ausente.");
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new InvalidJwtException("Claim JWT numerica obrigatoria ausente.");
    }

    private UserRole asRole(Object value) {
        try {
            return UserRole.valueOf(asString(value));
        } catch (IllegalArgumentException exception) {
            throw new InvalidJwtException("Role JWT invalida.");
        }
    }
}
