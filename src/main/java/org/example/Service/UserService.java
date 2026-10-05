package org.example.Service;

import org.example.DTO.request.LoginRequest;
import org.example.DTO.request.RegisterRequest;
import org.example.DTO.response.UserResponse;
import org.example.Entity.User;
import org.example.Repository.UserJPARepository;
import org.example.enums.User_Type;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    private final UserJPARepository userJPARepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserJPARepository userJPARepository, PasswordEncoder passwordEncoder) {
        this.userJPARepository = userJPARepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse registerUser(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        String email = request.email() == null
                ? null
                : request.email().trim().toLowerCase(Locale.ROOT);

        String fullName = request.fullName() == null
                ? null
                : request.fullName().trim();

        String password = request.password();


        if (email == null || email.isBlank()) {
            return null;
        }

        if (!isValidEmail(email)) {
            return null;
        }


        if (fullName == null || fullName.isBlank()) {
            return null;
        }

        if (fullName.length() <= 8) {
            return null;
        }


        if (password == null || password.isBlank()) {
            return null;
        }

        if (password.length() <= 12) {
            return null;
        }
        if (userJPARepository.existsByEmail(email)) {
            return null;
        }

        User registeredUser = new User();
        registeredUser.setId(nextUserId());
        registeredUser.setEmail(email);
        registeredUser.setFullName(fullName);
        registeredUser.setPassword(passwordEncoder.encode(password));
        registeredUser.setRole(User_Type.STAFF);
        User savedUser = userJPARepository.save(registeredUser);
        return toResponse(savedUser);
    }

    public boolean loginUser(LoginRequest request) {
        return authenticate(request) != null;
    }

    public UserResponse authenticate(LoginRequest request) {
        if (request == null) {
            return null;
        }

        if (request.email() == null || request.email().isBlank()) {
            return null;
        }

        if (request.password() == null || request.password().isBlank()) {
            return null;
        }
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Optional<User> userOptional = userJPARepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return null;
        }
        User user = userOptional.get();
        return user.getPassword() != null
                && passwordEncoder.matches(request.password(), user.getPassword())
                ? toResponse(user)
                : null;
    }

    public UserResponse findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userJPARepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .map(this::toResponse)
                .orElse(null);
    }

    private boolean isValidEmail(String email) {
        int atIndex = email.indexOf('@');
        return atIndex > 0
                && atIndex == email.lastIndexOf('@')
                && atIndex < email.length() - 1
                && !email.substring(atIndex + 1).isBlank();
    }

    private int nextUserId() {
        return userJPARepository.findAll().stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0) + 1;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );
    }
}
