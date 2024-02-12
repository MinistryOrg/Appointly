package com.mom.appointly;

import com.mom.appointly.model.Appointment;
import com.mom.appointly.model.CustomerData;
import com.mom.appointly.model.Shop;
import com.mom.appointly.model.UserEntity;
import com.mom.appointly.repository.AppointmentRepo;
import com.mom.appointly.repository.CustomerDataRepo;
import com.mom.appointly.repository.ShopRepo;
import com.mom.appointly.repository.UserRepo;
import com.mom.appointly.service.AppointlyService;
import com.mom.appointly.testUtil.TestUtil;
import net.bytebuddy.dynamic.DynamicType;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointlyServiceTest {
    @InjectMocks
    private AppointlyService appointlyService;

    @Mock
    private SecurityContextHolder securityContextHolder;

    @Mock
    private Authentication authentication;

    @Mock
    private UserRepo userRepo;

    @Mock
    private ShopRepo shopRepo;

    @Mock
    private CustomerDataRepo customerDataRepo;

    @Mock
    private AppointmentRepo appointmentRepo;

    private TestUtil testUtil;
    @Mock
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    public void setUp() {
        // Mock user authentication
        testUtil = new TestUtil();
        UserEntity userEntity = testUtil.createUser(passwordEncoder);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new TestingAuthenticationToken(userEntity, null));
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void testMakeAppointment() {
        // Mock data
        UserEntity userEntity = testUtil.createUser(passwordEncoder);
        Shop shop = testUtil.createShop();
        Appointment appointment = testUtil.createAppointment();

        // Mock repository behavior
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        when(shopRepo.findByName(anyString())).thenReturn(Optional.of(shop));

        // Mocking the case where customerDataRepo.findByUserEntityAndShop returns empty optional
        when(customerDataRepo.findByUserEntityAndShop(any(UserEntity.class), any(Shop.class))).thenReturn(Optional.empty());
        // Perform the appointment
        CustomerData result = appointlyService.makeAppointment("shopname", appointment);

        // Verify the result
        assertNotNull(result);
        // Add more assertions as needed

        // Mocking the case where customerDataRepo.findByUserEntityAndShop returns non-empty optional
        when(customerDataRepo.findByUserEntityAndShop(any(UserEntity.class), any(Shop.class))).thenReturn(Optional.of(testUtil.createCustomerData(userEntity, shop, appointment)));
        // Perform the appointment again to cover the other scenario
        result = appointlyService.makeAppointment("shopname", appointment);

        // Verify the result again
        assertNotNull(result);
        // Add more assertions as needed
    }


}
