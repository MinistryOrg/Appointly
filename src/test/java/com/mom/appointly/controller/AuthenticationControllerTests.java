package com.mom.appointly.controller;

import com.mom.appointly.auth.model.AuthenticationRequest;
import com.mom.appointly.auth.model.AuthenticationResponse;
import com.mom.appointly.auth.model.ChangePasswordRequest;
import com.mom.appointly.auth.model.RegisterRequest;
import com.mom.appointly.model.Role;
import com.mom.appointly.model.UserEntity;
import com.mom.appointly.repository.UserRepo;
import com.mom.appointly.testUtil.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

        // Create a user
        String userEmail = "panikkos@gmail.com";
        String userPassword = "password123";
        UserEntity user = UserEntity.builder()
                .email(userEmail)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .build();
        userRepo.save(user);

        TestUtil testUtil = new TestUtil();

        HttpHeaders headers = new HttpHeaders();
        // get a valid authentication token
        headers.setBearerAuth(testUtil.getToken(userEmail, userPassword, restTemplate));
        HttpEntity<ChangePasswordRequest> requestEntity = new HttpEntity<>(new ChangePasswordRequest("password123", "newPassword123", "newPassword123"), headers);

        // Act
        ResponseEntity<Void> responseEntity = restTemplate.exchange(baseUrl, HttpMethod.POST, requestEntity, Void.class);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // remove the user from the database
        userRepo.delete(user);
    }


}
