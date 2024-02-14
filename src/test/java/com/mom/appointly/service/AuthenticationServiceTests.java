package com.mom.appointly.service;

import com.mom.appointly.auth.model.AuthenticationRequest;
import com.mom.appointly.auth.model.AuthenticationResponse;
import com.mom.appointly.auth.model.ChangePasswordRequest;
import com.mom.appointly.auth.model.RegisterRequest;
import com.mom.appointly.auth.service.AuthenticationService;
import com.mom.appointly.auth.service.JwtService;
import com.mom.appointly.model.Role;
import com.mom.appointly.model.UserEntity;
import com.mom.appointly.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class AuthenticationServiceTests {
    @Mock
    private UserRepo userRepo;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerSuccessfulRegistration() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("Panic", "G", "pgeo@hotmail.com", "password123");
        UserEntity mockUser = UserEntity.builder()
                .firstname("Panic")
                .lastname("G")
                .email("pgeo@hotmail.com")
                .password("encodedPassword") // assuming you know the encoded password
                .role(Role.ADMIN)
                .build();

        Mockito.when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(userRepo.save(any(UserEntity.class))).thenReturn(mockUser);
        Mockito.when(jwtService.generateToken(any(UserEntity.class), any(UserEntity.class)))
                .thenReturn("mockedJwtToken");
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encodedPassword");

        // Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mockedJwtToken", response.getToken());
    }

    @Test
    void registerUserAlreadyExists() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("Panic", "G", "pgeo@hotmail.com", "password123");
        UserEntity existingUser = UserEntity.builder().build();
        Mockito.when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.of(existingUser));

        // Act and Assert
        assertThrows(RuntimeException.class, () -> authenticationService.register(registerRequest),
                "Fail to register the user, user already exist");
    }

    @Test
    void authenticateSuccessfulAuthentication() {
        // Arrange
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("pgeo@hotmail.com", "password123");
        UserEntity mockUser = UserEntity.builder()
                .email("pgeo@hotmail.com")
                .password("encodedPassword") // assuming you know the encoded password
                .build();

        Mockito.when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.of(mockUser));
        Mockito.when(jwtService.generateToken(any(UserEntity.class), any(UserEntity.class)))
                .thenReturn("mockedJwtToken");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mockedJwtToken", response.getToken());
    }

    @Test
    void authenticateInvalidCredentials() {
        // Arrange
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("deniparxobro@example.com", "invalidPassword");

        Mockito.when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.doThrow(new RuntimeException("Invalid credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act and Assert
        assertThrows(RuntimeException.class, () -> authenticationService.authenticate(authenticationRequest),
                "Invalid credentials");
    }

    @Test
    void changePasswordSuccessfulChange() {
        // Arrange
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("oldPassword", "newPassword", "newPassword");
        UserEntity mockUser = UserEntity.builder()
                .email("pgeo@hotmail.com")
                .password("encodedOldPassword") // assuming you know the encoded old password
                .build();
        Principal mockPrincipal = new UsernamePasswordAuthenticationToken(mockUser, null);

        Mockito.when(userRepo.save(any(UserEntity.class))).thenReturn(mockUser);
        Mockito.when(passwordEncoder.matches(Mockito.eq("oldPassword"), Mockito.anyString())).thenReturn(true);
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encodedNewPassword");

        // Act
        authenticationService.changePassword(changePasswordRequest, mockPrincipal);

        // Assert
        Mockito.verify(userRepo, Mockito.times(1)).save(any(UserEntity.class));
        assertEquals("encodedNewPassword", mockUser.getPassword());
    }

    @Test
    void changePasswordWrongOldPassword() {
        // Arrange
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("wrongOldPassword", "newPassword", "newPassword");
        UserEntity mockUser = UserEntity.builder()
                .email("pgeo@hotmail.com")
                .password("encodedOldPassword") // assuming you know the encoded old password
                .build();
        Principal mockPrincipal = new UsernamePasswordAuthenticationToken(mockUser, null);

        Mockito.when(passwordEncoder.matches(Mockito.eq("wrongOldPassword"), Mockito.anyString())).thenReturn(false);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> authenticationService.changePassword(changePasswordRequest, mockPrincipal),
                "Wrong password");
        Mockito.verify(userRepo, Mockito.never()).save(Mockito.any(UserEntity.class));
    }

    @Test
    void changePasswordPasswordsNotSame() {
        // Arrange
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("oldPassword", "newPassword", "differentConfirmationPassword");
        UserEntity mockUser = UserEntity.builder()
                .email("pgeo@hotmail.com")
                .password("encodedOldPassword") // assuming you know the encoded old password
                .build();
        Principal mockPrincipal = new UsernamePasswordAuthenticationToken(mockUser, null);

        Mockito.when(passwordEncoder.matches(Mockito.eq("oldPassword"), Mockito.anyString())).thenReturn(true);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> authenticationService.changePassword(changePasswordRequest, mockPrincipal),
                "Passwords not the same");
        Mockito.verify(userRepo, Mockito.never()).save(Mockito.any(UserEntity.class));
    }

    @Test
    void changePasswordNewPasswordSameAsOld() {
        // Arrange
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("oldPassword", "oldPassword", "oldPassword");
        UserEntity mockUser = UserEntity.builder()
                .email("pgeo@hotmail.com")
                .password("encodedOldPassword") // assuming you know the encoded old password
                .build();
        Principal mockPrincipal = new UsernamePasswordAuthenticationToken(mockUser, null);

        Mockito.when(passwordEncoder.matches(Mockito.eq("oldPassword"), Mockito.anyString())).thenReturn(true);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> authenticationService.changePassword(changePasswordRequest, mockPrincipal),
                "New password can't be the same with old");
        Mockito.verify(userRepo, Mockito.never()).save(Mockito.any(UserEntity.class));
    }

}
