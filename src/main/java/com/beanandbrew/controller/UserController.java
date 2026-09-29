package com.beanandbrew.controller;

import com.beanandbrew.dto.UserProfileResponse;
import com.beanandbrew.entity.User;
import com.beanandbrew.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(new UserProfileResponse(
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.isVerified()
                )))
                .orElse(ResponseEntity.status(404).build());
    }
}