package com.mom.appointly.testUtil;

import com.mom.appointly.auth.model.AuthenticationRequest;
import com.mom.appointly.auth.model.AuthenticationResponse;
import com.mom.appointly.model.*;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Time;
import java.util.ArrayList;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestUtil {
    public String getToken(String userEmail, String userPassword, TestRestTemplate restTemplate) {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(userEmail, userPassword);
        ResponseEntity<AuthenticationResponse> authResponseEntity = restTemplate.postForEntity("/api/v1/auth/appointly/authenticate", authenticationRequest, AuthenticationResponse.class);
        assertEquals(HttpStatus.OK, authResponseEntity.getStatusCode());
        assertNotNull(authResponseEntity.getBody());
        assertNotNull(authResponseEntity.getBody().getToken());
        return authResponseEntity.getBody().getToken();
    }

    public UserEntity createUser(PasswordEncoder passwordEncoder) {
        String userEmail = "panikkos@gmail.com";
        String userPassword = "password123";
        return UserEntity.builder()
                .email(userEmail)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.ADMIN)
                .build();
    }

    public Appointment createAppointment() {
        return new Appointment(
                "haircut", 50.0f, new Date(System.currentTimeMillis())
                , new Time(System.currentTimeMillis()), "Stylish");
    }

    public Shop createShop() {

        return new Shop("Test Shop", "Test Location", "Test Address", "+30 123 456789", "9:00", "18:00", 4.2f, "Test Description", "Test Service", true, Arrays.asList("Service1", "Service2", "Service3"), Arrays.asList("Service1 20", "Service2 30", "Service3 15"), Arrays.asList("Person1", "Person2", "Person3"));
    }

    public CustomerData createCustomerData(UserEntity user, Shop shop, Appointment appointment) {
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);
        return new CustomerData(
                user,
                shop,
                appointmentList
        );
    }
}
