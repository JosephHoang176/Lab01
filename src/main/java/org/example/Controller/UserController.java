package org.example.Controller;

import jakarta.validation.Valid;
import org.example.DTO.request.LoginRequest;
import org.example.DTO.request.RefreshTokenRequest;
import org.example.DTO.request.RegisterRequest;
import org.example.DTO.response.ApiResponse;
import org.example.DTO.response.TokenResponse;
import org.example.DTO.response.UserResponse;
import org.example.Service.UserService;
import org.example.Security.JwtService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/auth/register")
    public ApiResponse<UserResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        UserResponse user = userService.registerUser(registerRequest);
        if (user == null) {
            return new ApiResponse<>("Registration failed", "REGISTRATION_FAILED", null);
        }
        return new ApiResponse<>("Registration successful", "REGISTERED", user);
    }

    @PostMapping("/auth/login")
    public ApiResponse<TokenResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        UserResponse user = userService.authenticate(loginRequest);
        if (user == null) {
            return new ApiResponse<>("Invalid email or password", "INVALID_CREDENTIALS", null);
        }
        return new ApiResponse<>("Login successful", "AUTHENTICATED",
                jwtService.createTokenPair(user.email(), user.role()));
    }

    @PostMapping("/auth/refresh")
    public ApiResponse<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        try {
            if (request == null) {
                return new ApiResponse<>("Refresh token is required", "INVALID_REFRESH_TOKEN", null);
            }
            String email = jwtService.getEmailFromRefreshToken(request.refreshToken());
            UserResponse user = userService.findByEmail(email);
            if (user == null) {
                return new ApiResponse<>("User not found", "USER_NOT_FOUND", null);
            }
            return new ApiResponse<>("Token refreshed", "TOKEN_REFRESHED",
                    jwtService.createTokenPair(user.email(), user.role()));
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>("Invalid refresh token", "INVALID_REFRESH_TOKEN", null);
        }
    }

    @org.springframework.web.bind.annotation.GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> getMe(Authentication authentication) {
        UserResponse user = userService.findByEmail(authentication.getName());
        return new ApiResponse<>("Current user", "CURRENT_USER", user);
    }
}
