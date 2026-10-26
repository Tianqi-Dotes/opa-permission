package com.opa.permission.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtTokenService {
    public static final String ISSUER = "opa-permission";

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    public JwtTokenService(JwtEncoder jwtEncoder,
                           @Value("${security.jwt.expiration-seconds:900}") long expirationSeconds) {
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("JWT expiration must be greater than zero");
        }
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    public Token createToken(PermissionUserDetails user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getUsername())
                .claim("user_id", user.getUserId())
                .claim("tenant_id", user.getTenantId())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String encodedToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new Token(encodedToken, expirationSeconds);
    }

    public record Token(String accessToken, long expiresIn) {
    }
}
