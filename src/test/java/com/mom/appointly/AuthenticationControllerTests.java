package com.mom.appointly;

import com.mom.appointly.auth.controller.AuthenticationController;
import com.mom.appointly.auth.model.AuthenticationRequest;
import com.mom.appointly.auth.model.AuthenticationResponse;
import com.mom.appointly.auth.model.ChangePasswordRequest;
import com.mom.appointly.auth.model.RegisterRequest;
import com.mom.appointly.auth.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthenticationControllerTests {
    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegister() {
        // Given
        RegisterRequest mockRequest = new RegisterRequest(); // Create a mock request or use a builder pattern
        AuthenticationResponse mockResponse = new AuthenticationResponse(); // Create a mock response or use a builder pattern

        // Mock the behavior of the authenticationService
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(mockResponse);

        // When
        ResponseEntity<AuthenticationResponse> responseEntity = authenticationController.register(mockRequest);

        // Then
        // Assert that the response status is OK (200)
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Assert that the response body is the expected AuthenticationResponse
        assertEquals(mockResponse, responseEntity.getBody());

        // Verify that the authenticationService.register method was called with the correct argument
        verify(authenticationService, times(1)).register(eq(mockRequest));
    }

    @Test
    public void testAuthenticate() {
        // Given
        AuthenticationRequest mockRequest = new AuthenticationRequest(); // Create a mock request or use a builder pattern
        AuthenticationResponse mockResponse = new AuthenticationResponse(); // Create a mock response or use a builder pattern

        // Mock the behavior of the authenticationService
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenReturn(mockResponse);

        // When
        ResponseEntity<AuthenticationResponse> responseEntity = authenticationController.authenticated(mockRequest);

        // Then
        // Assert that the response status is OK (200)
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Assert that the response body is the expected AuthenticationResponse
        assertEquals(mockResponse, responseEntity.getBody());

        // Verify that the authenticationService.authenticate method was called with the correct argument
        verify(authenticationService, times(1)).authenticate(eq(mockRequest));
    }

    @Test
    public void testChangePassword() {
        // Given
        ChangePasswordRequest mockRequest = new ChangePasswordRequest("oldPassword", "newPassword", "newPassword"); // Create a mock request or use a builder pattern
        Principal mockPrincipal = mock(Principal.class); // Create a mock Principal

        // When
        authenticationController.changePassword(mockRequest, mockPrincipal);

        // Then
        // Verify that the authenticationService.changePassword method was called with the correct arguments
        verify(authenticationService, times(1)).changePassword(eq(mockRequest), eq(mockPrincipal));
    }



}
