package com.opa.permission.controller;

import com.opa.permission.security.JwtTokenService;
import com.opa.permission.security.PermissionUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.username())
                || !StringUtils.hasText(request.password())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("username and password are required"));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            if (!(authentication.getPrincipal() instanceof PermissionUserDetails user)) {
                throw new IllegalStateException("Authentication returned an unsupported principal type");
            }
            JwtTokenService.Token token = jwtTokenService.createToken(user);
            return ResponseEntity.ok(new LoginResponse(token.accessToken(), "Bearer", token.expiresIn()));
        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("invalid username or password"));
        }
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
    }

    public record ErrorResponse(String message) {
    }
}
