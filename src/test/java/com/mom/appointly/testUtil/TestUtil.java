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
                "Haircut",
                50.0f,
                new Date(System.currentTimeMillis()), // Current date
                new Time(System.currentTimeMillis()), // Current time
                "John Doe",
                "Alice",
                "Smith"
        );
    }

    public Shop createShop() {
        return new Shop(
                1L,
                "shopname",
                "location",
                "Somewhere",
                "555-555-5555",
                "09:00 AM",
                "06:00 PM",
                4.5f,
                "Example Description",
                "about, about.",
                "service",
                true,
                Arrays.asList("Option 1", "Option 2", "Option 3"),
                Arrays.asList(10, 20, 30),
                Arrays.asList("Person 1", "Person 2", "Person 3"),
                "example_background.jpg",
                Arrays.asList("shop_img1.jpg", "shop_img2.jpg"),
                Arrays.asList("service_img1.jpg", "service_img2.jpg"),
                "shop_logo.jpg", null, null
        );
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
