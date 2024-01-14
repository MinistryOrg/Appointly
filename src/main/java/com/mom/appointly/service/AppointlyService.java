package com.mom.appointly.service;

import com.mom.appointly.model.*;
import com.mom.appointly.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AppointlyService {
    private final UserRepo userRepo;
    private final ShopRepo shopRepo;
    private final AppointmentRepo appointmentRepo;
    private final CustomerDataRepo customerDataRepo;
    private final AdminDataRepo adminDataRepo;

    public AdminData getCustomerData() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName(); // return the email of the user that is connected
        Optional<AdminData> adminData = adminDataRepo.findByUserEntityEmail(email); // get the admin data that is connected
        return adminData.get();
    }

    public CustomerData makeAppointment(String shopName, Appointment appointment) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // get the email of the user that is connected
        UserEntity userEntity = userRepo.findByEmail(userEmail).get();
        Shop shop = shopRepo.findByName(shopName).get();
        Optional<CustomerData> customerData = customerDataRepo.findByUserEntityAndShop(userEntity, shop);
        // check if the user already have make an appointment in this shop to add it to the list
        if (customerData.isPresent() &&
               canMakeAppointment(shop,appointment)) {
            appointment.setCustomerData(customerData.get());
            appointmentRepo.save(appointment);
            customerData.get().getAppointments().add(appointment);
            return customerDataRepo.save(customerData.get());
        } else if (canMakeAppointment(shop,appointment)) { // if is the first appointment of the user
            List<Appointment> appointments = new ArrayList<>();
            appointments.add(appointment);
            CustomerData customer = customerDataRepo.save(new CustomerData(userEntity, shop, appointments));
            appointment.setCustomerData(customer);
            appointmentRepo.save(appointment);
            return customer;
        }
        throw new RuntimeException("Appointment already exist"); // it means the appointment is already exist and returns 403 forbidden

        }

    public Appointment editAppointment(Appointment appointment) {
        Optional<Appointment> optionalAppointment = appointmentRepo.findById(appointment.getId());
        if (optionalAppointment.isPresent()) {
            // check if the user that calls the api can edit the specific appointment
            canMakeChanges(optionalAppointment.get().getCustomerData().getUserEntity());
            return appointmentRepo.save(appointment);
        }

        throw new RuntimeException("Appointment doesn't exist"); // appointment doesn't exit for edit and returns 403 forbidden
    }

    public void canMakeChanges(UserEntity userEntity){
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity connectedUser = userRepo.findByEmail(userEmail).get();
        // if the user doesn't own the change he wants to make or is not the admin throws exception
        if(!userEntity.equals(connectedUser) || userEntity.getRole().equals(Role.USER)){
            throw new RuntimeException("You don't have the permissions");
        }
    }

    public boolean canMakeAppointment(Shop shop, Appointment appointment){
        return customerDataRepo.findByShopAndAppointmentsDateAndAppointmentsTime(shop,
                appointment.getDate(),
                        appointment.getTime())
                .isEmpty();
    }

    public void cancelAppointment(Long id) {
        Optional<Appointment> appointmentOptional = appointmentRepo.findById(id);

        if (appointmentOptional.isPresent()) { // check if the appointment exist
            Appointment canceledAppointment = appointmentOptional.get();
            CustomerData customerData = canceledAppointment.getCustomerData();
            canMakeChanges(customerData.getUserEntity());
            // Remove the appointment from the  list of appointments
            customerData.getAppointments().remove(canceledAppointment);
            // Set null the customer_data from the canceled appointment to be able to delete
            canceledAppointment.setCustomerData(null);
            // Check if the last appointment for the customer_data
            if (customerData.getAppointments().isEmpty()) { // remove the customer data from the database if is the last appointment
                customerDataRepo.delete(customerData);
            }
            appointmentRepo.delete(canceledAppointment);
            return;
        }
        throw new RuntimeException("The appointment doesn't exist");
    }

    public List<Appointment> getAppointments(String shopName) {
        if (customerDataRepo.findByShopName(shopName).isPresent()) {
            return customerDataRepo.findByShopName(shopName).get().getAppointments();
        }
        throw new RuntimeException("Shop doesn't exit");
    }

    public void addShop(Shop shop) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepo.findByEmail(userEmail).get();
        Optional<AdminData> adminData = adminDataRepo.findByUserEntity(userEntity);

        checkIfNameAlreadyExist(shop.getName());
        
        if (adminData.isPresent()) { // if the admin already have a shop in the app add it the new one to the list
            adminData.get().getShops().add(shop);
            shop.setAdminData(adminData.get());
            shopRepo.save(shop);
            adminDataRepo.save(adminData.get());
        } else { // if is the first shop that the admin create, add a new AdminData to the database
            List<Shop> shops = new ArrayList<>();
            AdminData newAdminData = new AdminData(null, userEntity, shops);
            shop.setAdminData(newAdminData);
            shopRepo.save(shop);
            shops.add(shop);
            adminDataRepo.save(newAdminData);
        }
    }

    public Shop editShop(String shopName, Shop shop) {
        Optional<Shop> shopOptional = shopRepo.findByName(shopName);
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        checkIfNameAlreadyExist(shop.getName()); // check if a shop with the same name exist
        if (shopOptional.isPresent()) { // shop exist
            String ownerEmail = shopOptional.get().getAdminData().getUserEntity().getEmail();
            if (userEmail.equals(ownerEmail)) { // checks if is the owner of the shop
                shopRepo.save(shop);
                return shop;
            }
        }
        throw new RuntimeException("Edit failed");
    }

    public void deleteShop(String shopName) {
        Optional<Shop> shopOptional = shopRepo.findByName(shopName);
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        canMakeChanges(userRepo.findByEmail(userEmail).get());
        if (shopOptional.isPresent()) {
            Shop canceledShop = shopOptional.get();
            AdminData adminData = canceledShop.getAdminData();
            // Remove the shop from the  list of appointments
            adminData.getShops().remove(canceledShop);
            // Set null the admin_data from the canceled appointment to be able to delete
            canceledShop.setAdminData(null);
            // Check if the last appointment for the customer_data
            if (adminData.getShops().isEmpty()) {
                adminDataRepo.delete(adminData);
            }
            shopRepo.delete(canceledShop);
        }
    }

    public Shop searchShopById(Long id){
        Optional<Shop> shopOptional = shopRepo.findById(id);
        if(shopOptional.isPresent()){
            return shopOptional.get();
        }
        throw new RuntimeException(" The shop doesn't exist");
    }

    public List<Shop> getShops() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminDataRepo.findByUserEntityEmail(userEmail).get().getShops();
    }

    public List<Shop> searchByLocationAndService(String location, String service){
        List <Shop> shopList =  shopRepo.findShopByLocationAndService(location, service);
        if(!shopList.isEmpty()){
            return shopList;
        }
        throw new RuntimeException("Shop doesn't exist");
    }

    public void checkIfNameAlreadyExist(String name) {
        if (shopRepo.findByName(name).isPresent()) {
            throw new RuntimeException("Name already exist");
        }
    }

    public Object getDates(String shopName) {
        Optional<CustomerData> customerData = customerDataRepo.findByShopName(shopName);
        Map<String, List<String>> datesAndTime = new HashMap<>();

        if (customerData.isPresent()) {
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Appointment appointment : customerData.get().getAppointments()) {
                LocalDate appointmentDate = appointment.getDate().toLocalDate();

                // Check if the appointment date is on or after the current date
                if (!appointmentDate.isBefore(currentDate)) {
                    String formattedDate = appointmentDate.format(dateFormatter);

                    if (datesAndTime.containsKey(formattedDate)) {
                        // If the specific day already has an appointment, add to the key of the date
                        datesAndTime.get(formattedDate).add(appointment.getTime().toString());
                    } else {
                        List<String> timeList = new ArrayList<>();
                        timeList.add(appointment.getTime().toString());
                        datesAndTime.put(formattedDate, timeList);
                    }
                }
            }
        }

        return datesAndTime;
    }
}
