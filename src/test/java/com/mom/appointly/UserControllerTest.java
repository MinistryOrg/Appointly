package com.mom.appointly;

import com.mom.appointly.model.*;
import com.mom.appointly.repository.AppointmentRepo;
import com.mom.appointly.repository.CustomerDataRepo;
import com.mom.appointly.repository.ShopRepo;
import com.mom.appointly.repository.UserRepo;
import com.mom.appointly.testUtil.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Time;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AppointmentRepo appointmentRepo;
    @Autowired
    private ShopRepo shopRepo;
    @Autowired
    private CustomerDataRepo customerDataRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testMakeAppointment() {
        // Arrange
        String baseUrl = "http://localhost:" + port +
                "/api/v1/appointly/user/makeAppointment?shopName=shopname";
        // Create a user
        String userEmail = "panikkos@gmail.com";
        String userPassword = "password123";
        UserEntity user = UserEntity.builder()
                .email(userEmail)
                .password(passwordEncoder.encode(userPassword))
                .role(Role.USER)
                .build();
        userRepo.save(user);

        // create appointment
        Appointment appointment = new Appointment(
                 "haircut", 50.0f, new Date(System.currentTimeMillis())
                , new Time(System.currentTimeMillis()), "Stylish");
        // create shop to connect it with the appointment
        Shop shop = new Shop(
                1L, "shopname", "haircut", "Athens",
                true, null, null);
        shopRepo.save(shop);

        TestUtil testUtil = new TestUtil();
        HttpHeaders headers = new HttpHeaders();
        // get a valid authentication token
        headers.setBearerAuth(testUtil.getToken(userEmail, userPassword, restTemplate));
        // Create a request entity with the appointment as the request body
        HttpEntity<Appointment> requestEntity = new HttpEntity<>(appointment, headers);
        // Make the HTTP request to your endpoint
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        // Verify the response status
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        // remove the test data from the database
        customerDataRepo.deleteAll(); // delete all the customer data from the test database
        userRepo.delete(user);
        shopRepo.delete(shop);
        appointmentRepo.delete(appointment);
    }
}
