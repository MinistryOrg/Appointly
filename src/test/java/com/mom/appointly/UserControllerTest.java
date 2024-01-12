package com.mom.appointly;

import com.mom.appointly.model.Appointment;
import com.mom.appointly.model.CustomerData;
import com.mom.appointly.model.Shop;
import com.mom.appointly.model.UserEntity;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

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
    TestUtil testUtil = new TestUtil();

    @Test
    public void testMakeAppointmentIntegration() {
        // Arrange
        String baseUrl = "http://localhost:" + port +
                "/api/v1/appointly/user/makeAppointment?shopName=shopname";
        // Create a user
        UserEntity user = testUtil.createUser(passwordEncoder);
        userRepo.save(user);
        // create appointment
        Appointment appointment = testUtil.createAppointment();
        // create shop to connect it with the appointment
        Shop shop = testUtil.createShop();
        shopRepo.save(shop);
        HttpHeaders headers = new HttpHeaders();
        // get a valid authentication token
        headers.setBearerAuth(testUtil.getToken(user.getEmail(), "password123", restTemplate));
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

    @Test
    public void testEditAppointmentIntegration() {
        // Arrange
        String baseUrl = "http://localhost:" + port +
                "/api/v1/appointly/user/editAppointment";

        // Create a user
        UserEntity user = testUtil.createUser(passwordEncoder);
        userRepo.save(user);
        // create shop
        Shop shop = testUtil.createShop();
        shopRepo.save(shop);
        // Create an initial appointment
        Appointment initialAppointment = testUtil.createAppointment();
        // Create Customer data
        CustomerData customerData = testUtil.createCustomerData(user,shop,initialAppointment);
        customerDataRepo.save(customerData);
        // Save the appointment with customerData to the database
        initialAppointment.setCustomerData(customerData);
        appointmentRepo.save(initialAppointment);
        // Update the appointment details
        initialAppointment.setService("newService");
        initialAppointment.setCost(60.0f);

        HttpHeaders headers = new HttpHeaders();
        // Get a valid authentication token
        headers.setBearerAuth(testUtil.getToken(user.getEmail(), "password123", restTemplate));
        // Create a request entity with the updated appointment as the request body
        HttpEntity<Appointment> requestEntity = new HttpEntity<>(initialAppointment, headers);
        // Make the HTTP request to your endpoint
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT, // Use PUT for editing appointment
                requestEntity,
                String.class
        );
        // Verify the response status
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Optionally, you can fetch the updated appointment from the database and assert its new values
        Appointment updatedAppointment = appointmentRepo.findById(initialAppointment.getId()).orElseThrow();

        // Assertions on the updatedAppointment values
        assertEquals("newService", updatedAppointment.getService());
        assertEquals(60.0f, updatedAppointment.getCost());

        // Remove the test data from the database
        customerDataRepo.delete(customerData);
        appointmentRepo.delete(updatedAppointment);
        userRepo.delete(user);
        shopRepo.delete(shop);
    }


    @Test
    public void testCancelAppointmentIntegration() throws Exception {
        // Arrange
        String baseUrl = "http://localhost:" + port +
                "/api/v1/appointly/user/cancelAppointment";

        // Create a user
        UserEntity user = testUtil.createUser(passwordEncoder);
        userRepo.save(user);
        // create shop
        Shop shop = testUtil.createShop();
        shopRepo.save(shop);
        // Create an initial appointment
        Appointment initialAppointment = testUtil.createAppointment();
        // Create Customer data
        CustomerData customerData = testUtil.createCustomerData(user, shop, initialAppointment);
        customerDataRepo.save(customerData);
        // Save the appointment with customerData to the database
        initialAppointment.setCustomerData(customerData);
        appointmentRepo.save(initialAppointment);

        HttpHeaders headers = new HttpHeaders();
        // Get a valid authentication token
        headers.setBearerAuth(testUtil.getToken(user.getEmail(), "password123", restTemplate));
        // Create a request entity without a body
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        // Make the HTTP request to your endpoint
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                baseUrl + "?id=" + initialAppointment.getId(),
                HttpMethod.DELETE,
                requestEntity,
                String.class
        );
        // Verify the response status
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Verify that the appointment and related data have been deleted from the database
        assertEquals(0, appointmentRepo.count());
        assertEquals(0, customerDataRepo.count());

        // Verify that the user and shop still exist in the database
        assertEquals(1, userRepo.count());
        assertEquals(1, shopRepo.count());

        // Remove the test data from the database
        customerDataRepo.delete(customerData);
        appointmentRepo.delete(initialAppointment);
        userRepo.delete(user);
        shopRepo.delete(shop);
    }

    @Test
    public void testGetDatesIntegration() throws Exception {
        // Arrange
        String baseUrl = "http://localhost:" + port +
                "/api/v1/appointly/user/dates";

        // Create a user
        UserEntity user = testUtil.createUser(passwordEncoder);
        userRepo.save(user);
        // create shop
        Shop shop = testUtil.createShop();
        shopRepo.save(shop);

        HttpHeaders headers = new HttpHeaders();
        // Get a valid authentication token
        headers.setBearerAuth(testUtil.getToken(user.getEmail(), "password123", restTemplate));
        // Create a request entity without a body (as it's a GET request)
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Make the HTTP request to your endpoint
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                baseUrl + "?shopName=" + shop.getName(),
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        // Verify the response status
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Remove the test data from the database (if needed)
        userRepo.delete(user);
        shopRepo.delete(shop);
    }


    @Test
    public void testGetByLocationServiceIntegration() throws Exception {
        // Arrange
        String baseUrl = "http://localhost:" + port +
                "/api/v1/appointly/user/shopsByLocationService";

        // Create a user
        UserEntity user = testUtil.createUser(passwordEncoder);
        userRepo.save(user);

        HttpHeaders headers = new HttpHeaders();
        // Get a valid authentication token
        headers.setBearerAuth(testUtil.getToken(user.getEmail(), "password123", restTemplate));
        // Create a request entity without a body (as it's a GET request)
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Create a shop for testing
        Shop shop = new Shop("Test Shop",
                "Test Location", "Test Address",
                "+30 123 456789", "9:00", "18:00",
                4.2f, "Test Description", "Test Service", true,
                Arrays.asList("Service1", "Service2", "Service3"), Arrays.asList("Service1 20", "Service2 30", "Service3 15"),
                Arrays.asList("Person1", "Person2", "Person3"));

        shopRepo.save(shop);

        // Make the HTTP request to your endpoint
        ResponseEntity<List<Shop>> responseEntity = restTemplate.exchange(
                baseUrl + "?location=location&service=service",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<Shop>>() {}
        );

        // Verify the response status
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        // get the list of shops that the request return
        List<Shop> shops = responseEntity.getBody();
        assertEquals(1, shops.size());  // expected one shop let's see if is correct

        // Remove the test data from the database (if needed)
        userRepo.delete(user);
        shopRepo.delete(shop);
    }

}
