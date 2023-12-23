package com.mom.appointly.auth.controller;

import com.mom.appointly.auth.model.AuthenticationRequest;
import com.mom.appointly.auth.model.AuthenticationResponse;
import com.mom.appointly.auth.model.ChangePasswordRequest;
import com.mom.appointly.auth.model.RegisterRequest;
import com.mom.appointly.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth/appointly")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @GetMapping("/test")
    public  ResponseEntity<?> getTest(){
        return ResponseEntity.ok("test");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticated(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/password")
    public void changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, Principal connectedUser) {
        authenticationService.changePassword(changePasswordRequest, connectedUser);
    }

    @GetMapping("/logout")
    public void logout() {
        SecurityContextHolder.clearContext();
    }

}