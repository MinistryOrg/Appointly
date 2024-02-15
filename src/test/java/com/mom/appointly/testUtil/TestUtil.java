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

    public UserEntity createUser() {
        return UserEntity.builder()
                .firstname("mia")
                .lastname("wallace")
                .email("mia@gmail.com")
                .password("password123")
                .role(Role.ADMIN)
                .build();
    }

    public Appointment createAppointment() {
        return Appointment.builder()
                .id(1L)
                .service("Haircut")
                .cost(50.0f)
                .date(Date.valueOf("2024-02-13"))
                .time(Time.valueOf("10:00:00"))
                .personnel("Barber")
                .userFirstname("John")
                .userLastname("Doe")
                .customerData(null)
                .build();
    }


    public Shop createShop() {
        return Shop.builder()
                .id(1L)
                .name("shopname")
                .location("location")
                .address("Somewhere")
                .telephone("555-555-5555")
                .openHour("09:00 AM")
                .closeHour("06:00 PM")
                .rating(4.5f)
                .description("Example Description")
                .about("about, about.")
                .service("service")
                .partner(true)
                .servicesOptions(Arrays.asList("Option 1", "Option 2", "Option 3"))
                .cost(Arrays.asList(10, 20, 30))
                .personnel(Arrays.asList("Person 1", "Person 2", "Person 3"))
                .backgroundImgPath("example_background.jpg")
                .shopImg(Arrays.asList("shop_img1.jpg", "shop_img2.jpg"))
                .serviceImg(Arrays.asList("service_img1.jpg", "service_img2.jpg"))
                .shopLogo("shop_logo.jpg")
                .build();
    }

    public CustomerData createCustomerData(UserEntity user, Shop shop, Appointment appointment) {
        List<Appointment> appointments = new ArrayList<>();
        appointments.add(appointment);

        return CustomerData.builder()
                .id(1L)
                .userEntity(user)
                .shop(shop)
                .appointments(appointments)
                .build();
    }

    public CustomerData createCustomerData(Long id,UserEntity user, Shop shop, Appointment appointment) {
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);

        return CustomerData.builder()
                .id(1L)
                .userEntity(user)
                .shop(shop)
                .appointments(appointmentList)
                .build();
    }

    public AdminData createAdminData(UserEntity userEntity, List<Shop> shops) {
        return AdminData.builder()
                .id(1L)
                .userEntity(userEntity)
                .shops(shops)
                .build();
    }

    public ShopUpdateRequest createShopUpdateRequest(){
        ArrayList<Integer> cost = new ArrayList<>(Arrays.asList(10, 20, 30));
        ArrayList<String> servicesOptions = new ArrayList<>(Arrays.asList("Service 1", "Service 2", "Service 3"));

        return new ShopUpdateRequest(
                1L,
                "NewShopName",
                "new address",
                "new desc",
                "9999999999",
                cost,
                servicesOptions

        );
    }
}
