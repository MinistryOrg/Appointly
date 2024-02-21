package com.mom.appointly.service;

import com.mom.appointly.model.*;
import com.mom.appointly.repository.*;
import com.mom.appointly.testUtil.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @Mock
    private AdminDataRepo adminDataRepo;

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
        // given
        UserEntity connectedUser = new UserEntity();
        connectedUser.setId(1L);
        connectedUser.setEmail("connected@example.com");
        connectedUser.setRole(Role.ADMIN);

        // when
        when(userRepo.findByEmail("connected@example.com")).thenReturn(Optional.of(connectedUser));

        // then
        assertThrows(RuntimeException.class, () -> appointlyService.canMakeChanges(connectedUser));
    }

    @Test
    public void testCanMakeChangesNotAllowed() {
        // given
        UserEntity userEntity = new UserEntity();
        userEntity.setId(2L);
        userEntity.setEmail("user@example.com");
        userEntity.setRole(Role.USER);

        // Mock the connected user
        UserEntity connectedUser = new UserEntity();
        connectedUser.setId(1L);
        connectedUser.setEmail("connected@example.com");
        connectedUser.setRole(Role.USER); // Set the role to USER, not ADMIN

        // when return the connected user that is not admin or the owner to make changes
        when(userRepo.findByEmail("connected@example.com")).thenReturn(Optional.of(connectedUser));

        // then pass different user that is not admin or the owner
        assertThrows(RuntimeException.class, () -> appointlyService.canMakeChanges(userEntity));
    }

    // end of canMakeChanges

    // start of makeAppointment

    @Test
    public void testMakeAppointmentCustomerDataExist(){
        // given
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
        // given
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
        // given
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
        // given
        UserEntity userEntity = testUtil.createUser();
        Shop shop = testUtil.createShop();
        Appointment appointment = testUtil.createAppointment();
        CustomerData customerData = testUtil.createCustomerData(userEntity, shop, appointment);
        appointment.setCustomerData(customerData);
        Appointment newAppointment = testUtil.createAppointment();
        // set the new data
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
        // given
        UserEntity userEntity = testUtil.createUser();
        userEntity.setRole(Role.USER);
        Shop shop = testUtil.createShop();
        Appointment existingAppointment = testUtil.createAppointment();
        CustomerData customerData = testUtil.createCustomerData(userEntity, shop, existingAppointment);
        existingAppointment.setCustomerData(customerData);
        Appointment newAppointment = testUtil.createAppointment();
        // set the new data
        newAppointment.setDate(Date.valueOf("2024-02-14"));
        newAppointment.setTime(Time.valueOf("11:00:00"));
        newAppointment.setCustomerData(customerData);
        // when
        when(appointmentRepo.findById(existingAppointment.getId())).thenReturn(Optional.of(existingAppointment));
        when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.of(userEntity));
        // then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> appointlyService.editAppointment(newAppointment));
        assertEquals("You don't have the permissions", exception.getMessage());
    }

    @Test
    public void testEditAppointmentDoesNotExist() {
        // given
        Appointment newAppointment = new Appointment();
        newAppointment.setId(1L);
        // when
        when(appointmentRepo.findById(newAppointment.getId())).thenReturn(Optional.empty());
        // then
        assertThrows(RuntimeException.class, () -> appointlyService.editAppointment(newAppointment));
    }

    // end of editAppointment

    // start of cancelAppointment

    @Test
    public void testCancelAppointmentExistsAndChangesAllowed() {
        // given
        UserEntity userEntity = testUtil.createUser();
        Shop shop = testUtil.createShop();
        Appointment appointment = testUtil.createAppointment();
        CustomerData customerData = testUtil.createCustomerData(userEntity, shop, appointment);
        appointment.setCustomerData(customerData);
        given(appointmentRepo.findById(appointment.getId()))
                .willReturn(Optional.of(appointment));
        // when
        when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.of(userEntity));
        when(appointmentRepo.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(customerDataRepo.save(customerData)).thenReturn(customerData);
        // act
        appointlyService.cancelAppointment(appointment.getId());
        // then
        verify(appointmentRepo).delete(appointment);
        verify(customerDataRepo).delete(customerData);
    }

    @Test
    public void testCancelAppointmentDoesNotExist() {
        // given
        long nonExistingAppointmentId = 100L;
        // then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> appointlyService.cancelAppointment(nonExistingAppointmentId));
        // when
        assertEquals("Appointment not found", exception.getMessage());
    }

    // end of cancelAppointment

    // start of getAppointments

    @Test
    public void testGetAppointmentsShopExists() {
        // given
        String shopName = "Test Shop";
        Shop shop = new Shop();
        shop.setName(shopName);

        CustomerData customerData1 = new CustomerData();
        customerData1.setAppointments(new ArrayList<>());
        CustomerData customerData2 = new CustomerData();
        customerData2.setAppointments(new ArrayList<>());

        List<CustomerData> customerDataList = new ArrayList<>();
        customerDataList.add(customerData1);
        customerDataList.add(customerData2);

        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        customerData1.getAppointments().add(appointment1);
        customerData2.getAppointments().add(appointment2);
        // when
        when(shopRepo.findByName(shopName)).thenReturn(Optional.of(shop));
        when(customerDataRepo.findByShop(shop)).thenReturn(customerDataList);
        // act
        List<Appointment> appointments = appointlyService.getAppointments(shopName);
        // then
        assertEquals(2, appointments.size());
        assertEquals(appointment1, appointments.get(0));
        assertEquals(appointment2, appointments.get(1));
    }

    @Test
    public void testGetAppointmentsShopDoesNotExist() {
        // given
        String nonExistingShopName = "Non-existing Shop";
        // when
        when(shopRepo.findByName(nonExistingShopName)).thenReturn(Optional.empty());
        // then
        assertThrows(RuntimeException.class, () -> appointlyService.getAppointments(nonExistingShopName));
    }

    // end of getAppointments

    // start to addShop
    @Test
    public void testAddShopAdminDataExist(){
        // given
        UserEntity userEntity = testUtil.createUser();
        Shop shop = testUtil.createShop();
        List<Shop> shops = new ArrayList<>();
        shops.add(shop);
        AdminData adminData = testUtil.createAdminData(userEntity, shops);
        shop.setAdminData(adminData);
        // when
        when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.of(userEntity));
        when(adminDataRepo.findByUserEntity(Mockito.any())).thenReturn(Optional.of(adminData));
        appointlyService.addShop(shop);
        // then
        ArgumentCaptor<Shop> shopArgumentCaptor =
                ArgumentCaptor.forClass(Shop.class);
        verify(shopRepo).save(shopArgumentCaptor.capture());
        Shop shopCaptor = shopArgumentCaptor.getValue();
        // check if the shop is saved correctly
        assertThat(shopCaptor).isEqualTo(shop);

        ArgumentCaptor<AdminData> adminDataArgumentCaptor =
                ArgumentCaptor.forClass(AdminData.class);
        verify(adminDataRepo).save(adminDataArgumentCaptor.capture());
        AdminData adminDataCaptor = adminDataArgumentCaptor.getValue();
        // check if is the admin data saved correctly
        assertThat(adminDataCaptor).isEqualTo(adminData);
    }

    @Test
    public void testAddShopAdminDataNotExist(){
        // given
        UserEntity userEntity = testUtil.createUser();
        Shop shop = testUtil.createShop();
        List<Shop> shops = new ArrayList<>();
        shops.add(shop);
        AdminData adminData = testUtil.createAdminData(userEntity, shops);
        shop.setAdminData(adminData);
        // when
        when(userRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.of(userEntity));
        appointlyService.addShop(shop);
        // then
        ArgumentCaptor<Shop> shopArgumentCaptor =
                ArgumentCaptor.forClass(Shop.class);
        verify(shopRepo).save(shopArgumentCaptor.capture());
        Shop shopCaptor = shopArgumentCaptor.getValue();
        // check if the shop is saved correctly
        assertThat(shopCaptor).isEqualTo(shop);

        ArgumentCaptor<AdminData> adminDataArgumentCaptor =
                ArgumentCaptor.forClass(AdminData.class);
        verify(adminDataRepo).save(adminDataArgumentCaptor.capture());
        AdminData adminDataCaptor = adminDataArgumentCaptor.getValue();
        // check if is the admin data saved correctly
        assertThat(adminDataCaptor).isEqualTo(adminData);
    }

    // end of addShop

    // start of editShop

    @Test
    public void testEditShopSuccess() {
        // given
        UserEntity userEntity = testUtil.createUser();
        Shop shop = testUtil.createShop();
        AdminData adminData = testUtil.createAdminData(userEntity, Collections.singletonList(shop));
        shop.setAdminData(adminData);

        ShopUpdateRequest shopUpdateRequest = testUtil.createShopUpdateRequest();

        when(shopRepo.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(userEntity.getEmail());

        // when
        appointlyService.editShop(shopUpdateRequest);

        // then
        ArgumentCaptor<Shop> shopArgumentCaptor =
                ArgumentCaptor.forClass(Shop.class);
        verify(shopRepo).save(shopArgumentCaptor.capture());
        Shop shopCaptor = shopArgumentCaptor.getValue();
        // check if the shop is saved correctly
        assertThat(shopCaptor.getName()).isEqualTo(shopUpdateRequest.getName());
        assertThat(shopCaptor.getAddress()).isEqualTo(shopUpdateRequest.getAddress());
        assertThat(shopCaptor.getDescription()).isEqualTo(shopUpdateRequest.getDescription());
        assertThat(shopCaptor.getTelephone()).isEqualTo(shopUpdateRequest.getTelephone());
        assertThat(shopCaptor.getCost()).isEqualTo(shopUpdateRequest.getCost());
        assertThat(shopCaptor.getServicesOptions()).isEqualTo(shopUpdateRequest.getServicesOptions());

    }

    @Test
    public void testEditShopUnauthorizedUser() {
        // given
        UserEntity userEntity = testUtil.createUser();
        Shop shop = testUtil.createShop();
        AdminData adminData = testUtil.createAdminData(userEntity, Collections.singletonList(shop));
        shop.setAdminData(adminData);

        ShopUpdateRequest shopUpdateRequest = testUtil.createShopUpdateRequest();

        when(shopRepo.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn("unauthorized@example.com");

        // when and then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> appointlyService.editShop(shopUpdateRequest));
        assertEquals("You don't have the permissions", exception.getMessage());
    }

    @Test
    public void testEditShopShopNotFound() {
        // given
        ShopUpdateRequest shopUpdateRequest = testUtil.createShopUpdateRequest();
        shopUpdateRequest.setId(1L);

        when(shopRepo.findById(shopUpdateRequest.getId())).thenReturn(Optional.empty());

        // when and then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> appointlyService.editShop(shopUpdateRequest));
        assertEquals("Shop doesn't exist", exception.getMessage());
    }

    // end of editShop

    // start of deleteShop

    @Test
    public void testDeleteShopSuccess() {
        // given
        UserEntity userEntity = testUtil.createUser();
        Shop shop = testUtil.createShop();
        List <Shop> shops = new ArrayList<>();
        shops.add(shop);
        AdminData adminData = testUtil.createAdminData(userEntity, shops);
        shop.setAdminData(adminData);
        when(shopRepo.findByName(shop.getName())).thenReturn(Optional.of(shop));
        when(userRepo.findByEmail(userEntity.getEmail())).thenReturn(Optional.of(userEntity));

        // when
        appointlyService.deleteShop(shop.getName());

        // then
        verify(adminDataRepo).delete(adminData);
        verify(shopRepo).delete(shop);
    }

    @Test
    public void testDeleteShopNotFound() {
        // given
        when(shopRepo.findByName("NonExistingShop")).thenReturn(Optional.empty());

        // when and then
        assertThrows(RuntimeException.class, () -> appointlyService.deleteShop("NonExistingShop"));
    }

    // end of deleteShop

    // start searchShopById

    @Test
    public void testSearchShopByIdSuccess() {
        // given
        Shop shop = testUtil.createShop();
        when(shopRepo.findById(1L)).thenReturn(Optional.of(shop));

        // when
        Shop foundShop = appointlyService.searchShopById(1L);

        // then
        assertNotNull(foundShop);
        assertEquals(1L, foundShop.getId());
    }

    @Test
    public void testSearchShopByIdNotFound() {
        // given
        when(shopRepo.findById(1L)).thenReturn(Optional.empty());

        // when and then
        assertThrows(RuntimeException.class, () -> appointlyService.searchShopById(1L));
    }

    // end of searchShopById


}
