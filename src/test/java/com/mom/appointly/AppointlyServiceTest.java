package com.mom.appointly;

import com.mom.appointly.model.Appointment;
import com.mom.appointly.model.CustomerData;
import com.mom.appointly.model.Shop;
import com.mom.appointly.model.UserEntity;
import com.mom.appointly.repository.*;
import com.mom.appointly.service.AppointlyService;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AppointlyServiceTest {
    @Mock
    private UserRepo userRepo;

    @Mock
    private ShopRepo shopRepo;

    @Mock
    private CustomerDataRepo customerDataRepo;

    @Mock
    private AppointmentRepo appointmentRepo;

    @InjectMocks
    private AppointlyService appointlyService;

    @Test
    public void testMakeAppointment() {
        // Mock the authenticated user's email
        String userEmail = "test@example.com";

        // Create a mock Authentication object
        Authentication authentication = mock(Authentication.class);
        // Set the authenticated user's email
        when(authentication.getName()).thenReturn(userEmail);

        // Create a mock SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        // Set the Authentication object in the SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Set the SecurityContext in the SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);

        // Mock the UserEntity object
        UserEntity userEntity = new UserEntity();
        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(userEntity));

        // Mock the Shop object
        Shop shop = new Shop();
        when(shopRepo.findByName("shopName")).thenReturn(Optional.of(shop));

        // Mock the CustomerData object
        CustomerData customerData = new CustomerData();
        when(customerDataRepo.findByUserEntityAndShop(userEntity, shop)).thenReturn(Optional.of(customerData));

        // Mock the appointment object
        Appointment appointment = new Appointment();

        // Call the method under test
        CustomerData result = appointlyService.makeAppointment("shopName", appointment);

        // Verify that the appointment is saved and associated with the customerData
        verify(appointmentRepo, times(1)).save(appointment);
        verify(customerDataRepo, times(1)).save(customerData);

        // Verify that the correct customerData object is returned
        assertEquals(customerData, result);
    }
}
