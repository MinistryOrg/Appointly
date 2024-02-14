package com.mom.appointly.service;

import com.mom.appointly.model.*;
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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest
@AutoConfigureMockMvc
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
        testUtil = new TestUtil();
        UserEntity user = testUtil.createUser();
        // Mocking SecurityContextHolder.getContext().getAuthentication().getName()
        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(auth.getName()).thenReturn(user.getEmail());
    }

    // start of test canMakeChanges

    @Test
    public void testCanMakeChangesSuccess() {
        // Mock the connected user
        UserEntity connectedUser = new UserEntity();
        connectedUser.setId(1L);
        connectedUser.setEmail("connected@example.com");
        connectedUser.setRole(Role.ADMIN);

        // Mock the UserRepository
        when(userRepo.findByEmail("connected@example.com")).thenReturn(Optional.of(connectedUser));

        // Test the method
        assertThrows(RuntimeException.class, () -> appointlyService.canMakeChanges(connectedUser));
    }

    @Test
    public void testCanMakeChangesNotAllowed() {
        // Mock the userEntity
        UserEntity userEntity = new UserEntity();
        userEntity.setId(2L);
        userEntity.setEmail("user@example.com");
        userEntity.setRole(Role.USER);

        // Mock the connected user
        UserEntity connectedUser = new UserEntity();
        connectedUser.setId(1L);
        connectedUser.setEmail("connected@example.com");
        connectedUser.setRole(Role.USER); // Set the role to USER, not ADMIN

        // return the connected user that is not admin or the owner to make changes
        when(userRepo.findByEmail("connected@example.com")).thenReturn(Optional.of(connectedUser));

        // pass different user that is not admin or the owner
        assertThrows(RuntimeException.class, () -> appointlyService.canMakeChanges(userEntity));
    }

    // end of canMakeChanges

    // start of makeAppointment

    @Test
    public void testMakeAppointmentCustomerDataExist(){
        UserEntity userEntity = testUtil.createUser();
        Shop shop = testUtil.createShop();
        Appointment appointment = testUtil.createAppointment();
        CustomerData customerData = testUtil.createCustomerData(userEntity, shop, appointment);
        // when
        when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.of(userEntity));
        when(shopRepo.findByName(Mockito.anyString())).thenReturn(Optional.of(shop));
        when(customerDataRepo.findByUserEntityAndShop(Mockito.any(), Mockito.any())).thenReturn(Optional.of(customerData));
        appointlyService.makeAppointment(shop.getName(), appointment);
        // then
        ArgumentCaptor<CustomerData> customerDataArgumentCaptor =
                ArgumentCaptor.forClass(CustomerData.class);
        verify(customerDataRepo).save(customerDataArgumentCaptor.capture());
        CustomerData customerDataCaptor = customerDataArgumentCaptor.getValue();
        // check if is the customer data saved correctly
        assertThat(customerDataCaptor).isEqualTo(customerData);

        ArgumentCaptor<Appointment> appointmentArgumentCaptor =
                ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepo).save(appointmentArgumentCaptor.capture());
        Appointment appointmentCaptor = appointmentArgumentCaptor.getValue();
        // check if the appointment is saved correctly
        assertThat(appointmentCaptor).isEqualTo(appointment);
    }

    @Test
    public void testMakeAppointmentFirstAppointment(){
        UserEntity userEntity = testUtil.createUser();
        Shop shop = testUtil.createShop();
        Appointment appointment = testUtil.createAppointment();
        CustomerData customerData = testUtil.createCustomerData(1L,userEntity, shop, appointment);

        // when
        when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.of(userEntity));
        when(shopRepo.findByName(Mockito.anyString())).thenReturn(Optional.of(shop));
        appointlyService.makeAppointment(shop.getName(), appointment);

        // then
        ArgumentCaptor<CustomerData> customerDataArgumentCaptor =
                ArgumentCaptor.forClass(CustomerData.class);
        verify(customerDataRepo).save(customerDataArgumentCaptor.capture());
        CustomerData customerDataCaptor = customerDataArgumentCaptor.getValue();
        // check if is the customer data saved correctly
        assertThat(customerDataCaptor).isEqualTo(customerData);
        // check if the appointment is saved correctly
        ArgumentCaptor<Appointment> appointmentArgumentCaptor =
                ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepo).save(appointmentArgumentCaptor.capture());
        Appointment appointmentCaptor = appointmentArgumentCaptor.getValue();
        assertThat(appointmentCaptor).isEqualTo(appointment);
    }

    @Test
    public void testMakeAppointmentShopNotExist() {
        UserEntity userEntity = testUtil.createUser();
        Shop shop = testUtil.createShop();
        Appointment appointment = testUtil.createAppointment();
        // when
        when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.of(userEntity));
        when(shopRepo.findByName(Mockito.anyString())).thenReturn(Optional.empty()); // Shop does not exist
        // then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> appointlyService.makeAppointment(shop.getName(), appointment));
        assertEquals("Shop doesn't exist", exception.getMessage());
    }

    // end of makeAppointment

    // start of editAppointment
    @Test
    public void testEditAppointmentExistsAndChangesAllowed() {
        UserEntity userEntity = testUtil.createUser();
        Shop shop = testUtil.createShop();
        Appointment appointment = testUtil.createAppointment();
        CustomerData customerData = testUtil.createCustomerData(userEntity, shop, appointment);
        appointment.setCustomerData(customerData);
        Appointment newAppointment =testUtil.createAppointment();
        newAppointment.setId(1L);
        newAppointment.setDate(Date.valueOf("2024-02-14"));
        newAppointment.setTime(Time.valueOf("11:00:00"));
        // when
        when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.of(userEntity));
        when(appointmentRepo.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        appointlyService.editAppointment(newAppointment);
        // then
        ArgumentCaptor<Appointment> appointmentArgumentCaptor =
                ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepo).save(appointmentArgumentCaptor.capture());
        Appointment appointmentCaptor = appointmentArgumentCaptor.getValue();
        // check if the appointment is saved correctly
        assertEquals(appointmentCaptor.getDate(), newAppointment.getDate());
        assertEquals(appointmentCaptor.getTime(), newAppointment.getTime());
    }

    @Test
    public void testEditAppointmentExistsButChangesNotAllowed() {
        // Arrange
        UserEntity userEntity = testUtil.createUser();
        userEntity.setRole(Role.USER);
        Shop shop = testUtil.createShop();
        Appointment existingAppointment = testUtil.createAppointment();
        CustomerData customerData = testUtil.createCustomerData(userEntity, shop, existingAppointment);
        existingAppointment.setCustomerData(customerData);
        Appointment newAppointment = testUtil.createAppointment();
        newAppointment.setDate(Date.valueOf("2024-02-14"));
        newAppointment.setTime(Time.valueOf("11:00:00"));
        newAppointment.setCustomerData(customerData);
        // when
        when(appointmentRepo.findById(existingAppointment.getId())).thenReturn(Optional.of(existingAppointment));
        when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.of(userEntity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> appointlyService.editAppointment(newAppointment));
        assertEquals("You don't have the permissions", exception.getMessage());
        // then
        assertThrows(RuntimeException.class, () -> appointlyService.editAppointment(newAppointment));
    }

    @Test
    public void testEditAppointmentDoesNotExist() {
        Appointment newAppointment = new Appointment();
        newAppointment.setId(1L);
        // when
        when(appointmentRepo.findById(newAppointment.getId())).thenReturn(Optional.empty());
        // then
        assertThrows(RuntimeException.class, () -> appointlyService.editAppointment(newAppointment));
    }
    // end of editAppointment





}
