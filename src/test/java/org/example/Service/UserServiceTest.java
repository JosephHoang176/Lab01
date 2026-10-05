package org.example.Service;

import org.example.DTO.request.LoginRequest;
import org.example.DTO.request.RegisterRequest;
import org.example.DTO.response.UserResponse;
import org.example.Entity.User;
import org.example.Repository.UserJPARepository;
import org.example.enums.User_Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserJPARepository repository;
    @Mock PasswordEncoder encoder;
    UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, encoder);
    }

    @Test void registerNullRequestReturnsNull() {
        assertNull(service.registerUser(null));
    }

    @Test void registerRejectsBlankEmail() {
        assertNull(service.registerUser(new RegisterRequest(" ", "Valid Name", "long-password-123")));
    }

    @Test void registerRejectsMalformedEmail() {
        assertNull(service.registerUser(new RegisterRequest("bad-email", "Valid Name", "long-password-123")));
    }

    @Test void registerRejectsShortName() {
        assertNull(service.registerUser(new RegisterRequest("a@b.com", "Short", "long-password-123")));
    }

    @Test void registerRejectsShortPassword() {
        assertNull(service.registerUser(new RegisterRequest("a@b.com", "Valid Full Name", "short")));
    }

    @Test void registerRejectsDuplicateEmail() {
        when(repository.existsByEmail("a@b.com")).thenReturn(true);
        assertNull(service.registerUser(new RegisterRequest("A@B.COM", "Valid Full Name", "long-password-123")));
    }

    @Test void registerHashesAndReturnsUser() {
        when(repository.existsByEmail("a@b.com")).thenReturn(false);
        when(repository.findAll()).thenReturn(List.of());
        when(encoder.encode("long-password-123")).thenReturn("hashed");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = service.registerUser(
                new RegisterRequest("A@B.COM", "Valid Full Name", "long-password-123")
        );

        assertNotNull(result);
        assertEquals("a@b.com", result.email());
        assertEquals(User_Type.STAFF, result.role());
        verify(encoder).encode("long-password-123");
    }

    @Test void loginRejectsNullRequest() {
        assertFalse(service.loginUser(null));
    }

    @Test void loginRejectsUnknownEmail() {
        when(repository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        assertFalse(service.loginUser(new LoginRequest("A@B.COM", "password")));
    }

    @Test void loginRejectsWrongPassword() {
        User user = user("a@b.com", "hashed");
        when(repository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "hashed")).thenReturn(false);
        assertFalse(service.loginUser(new LoginRequest("a@b.com", "wrong")));
    }

    @Test void loginReturnsTrueForValidPassword() {
        User user = user("a@b.com", "hashed");
        when(repository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(encoder.matches("password", "hashed")).thenReturn(true);
        assertTrue(service.loginUser(new LoginRequest("a@b.com", "password")));
    }

    @Test void findByEmailReturnsSafeResponse() {
        when(repository.findByEmail("a@b.com")).thenReturn(Optional.of(user("a@b.com", "hashed")));
        UserResponse result = service.findByEmail(" A@B.COM ");
        assertEquals("a@b.com", result.email());
        assertEquals(User_Type.STAFF, result.role());
    }

    private User user(String email, String password) {
        User user = new User(1, email, "Valid Full Name", User_Type.STAFF, password);
        return user;
    }
}
