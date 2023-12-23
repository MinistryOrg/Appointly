package com.mom.appointly;

import com.mom.appointly.auth.controller.AuthenticationController;
import com.mom.appointly.auth.model.AuthenticationRequest;
import com.mom.appointly.auth.model.AuthenticationResponse;
import com.mom.appointly.auth.model.ChangePasswordRequest;
import com.mom.appointly.auth.model.RegisterRequest;
import com.mom.appointly.auth.service.AuthenticationService;
import com.mom.appointly.model.Role;
import com.mom.appointly.model.UserEntity;
import com.mom.appointly.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationControllerTests {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testRegistrationIntegration() {
        // Arrange
        String baseUrl = "http://localhost:" + port + "/api/v1/auth/appointly/register";
        RegisterRequest registerRequest = new RegisterRequest("Panikkos", "Panikkou", "panic@gmail.com", "panikkou");
        // Act
        ResponseEntity<AuthenticationResponse> responseEntity = restTemplate.postForEntity(baseUrl, registerRequest, AuthenticationResponse.class);
        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        // Check that the user is saved in the database
        UserEntity savedUser = userRepo.findByEmail(registerRequest.getEmail()).orElse(null);
        assertEquals(registerRequest.getFirstname(), savedUser.getFirstname());
        assertEquals(registerRequest.getLastname(), savedUser.getLastname());
        assertEquals(registerRequest.getEmail(), savedUser.getEmail());
        // remove the user from the database
        userRepo.delete(savedUser);
    }

    @Test
    public void testAuthenticationIntegration() {
        // Arrange
        String baseUrl = "http://localhost:" + port + "/api/v1/auth/appointly/authenticate";
        String userEmail = "authenticate@gmail.com";
        String userPassword = "kodikos";
        UserEntity user = UserEntity.builder()
                .email(userEmail)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .build();
        userRepo.save(user);
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(userEmail, userPassword);
        // Act
        ResponseEntity<AuthenticationResponse> responseEntity = restTemplate.postForEntity(baseUrl, authenticationRequest, AuthenticationResponse.class);
        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertNotNull(responseEntity.getBody().getToken());
        // remove the user from the database
        userRepo.delete(user);
    }

    @Test
    public void testChangePasswordIntegration() {
        // Arrange
        String baseUrl = "http://localhost:" + port + "/api/v1/auth/appointly/password";

        // Create a user and get a valid authentication token
        String userEmail = "panikkos@gmail.com";
        String userPassword = "password123";
        UserEntity user = UserEntity.builder()
                .email(userEmail)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .build();
        userRepo.save(user);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(userEmail, userPassword);
        ResponseEntity<AuthenticationResponse> authResponseEntity = restTemplate.postForEntity("/api/v1/auth/appointly/authenticate", authenticationRequest, AuthenticationResponse.class);
        assertEquals(HttpStatus.OK, authResponseEntity.getStatusCode());
        assertNotNull(authResponseEntity.getBody());
        assertNotNull(authResponseEntity.getBody().getToken());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authResponseEntity.getBody().getToken());
        HttpEntity<ChangePasswordRequest> requestEntity = new HttpEntity<>(new ChangePasswordRequest("password123", "newPassword123", "newPassword123"), headers);

        // Act
        ResponseEntity<Void> responseEntity = restTemplate.exchange(baseUrl, HttpMethod.POST, requestEntity, Void.class);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // remove the user from the database
        userRepo.delete(user);
    }


}
