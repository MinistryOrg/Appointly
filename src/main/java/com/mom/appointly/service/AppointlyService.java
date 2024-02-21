package com.mom.appointly.service;

import com.mom.appointly.model.*;
import com.mom.appointly.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    public AdminData getAdminData() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName(); // return the email of the user that is connected
        Optional<AdminData> adminData = adminDataRepo.findByUserEntityEmail(email); // get the admin data that is connected
        return adminData.orElseThrow(() -> new NoSuchElementException("AdminData not found"));
    }

    public CustomerData makeAppointment(String shopName, Appointment appointment) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // get the email of the user that is connected
        UserEntity userEntity = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Optional<Shop> shop = shopRepo.findByName(shopName);
        if (shop.isPresent()) {
            Optional<CustomerData> customerData = customerDataRepo.findByUserEntityAndShop(userEntity, shop.get());
            // check if the user already have make an appointment in this shop to add it to the list
            if (customerData.isPresent() &&
                    canMakeAppointment(shop.get(), appointment)) {
                appointment.setCustomerData(customerData.get());
                appointmentRepo.save(appointment);
                customerData.get().getAppointments().add(appointment);
                return customerDataRepo.save(customerData.get());
            } else if (canMakeAppointment(shop.get(), appointment)) { // if is the first appointment of the user
                List<Appointment> appointments = new ArrayList<>();
                appointments.add(appointment);
                CustomerData customer = customerDataRepo.save(new CustomerData(1L, userEntity, shop.get(), appointments));
                appointment.setCustomerData(customer);
                appointmentRepo.save(appointment);
                return customer;
            } else {
                throw new RuntimeException("Appointment already exist"); // it means the appointment is already exist and returns 403 forbidden
            }
        } else {
            throw new RuntimeException("Shop doesn't exist");
        }
    }

    public Appointment editAppointment(Appointment appointment) {
        Optional<Appointment> optionalAppointment = appointmentRepo.findById(appointment.getId());

        Appointment existingAppointment = optionalAppointment.orElseThrow(() -> new NoSuchElementException("Appointment not found"));

        canMakeChanges(existingAppointment.getCustomerData().getUserEntity());
        existingAppointment.setTime(appointment.getTime());
        existingAppointment.setDate(appointment.getDate());
        System.out.println(appointment.getDate());
        return appointmentRepo.save(existingAppointment);
    }

    public void canMakeChanges(UserEntity userEntity) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity connectedUser = userRepo.findByEmail(userEmail).orElseThrow();
        // if the user doesn't own the change he wants to make or is not the admin throws exception
        if (userEntity.getRole().equals(Role.USER)) { // add one more check to check if is the owner of the shop
            throw new RuntimeException("You don't have the permissions");
        }
    }

    public boolean canMakeAppointment(Shop shop, Appointment appointment) {
        return customerDataRepo.findByShopAndAppointmentsDateAndAppointmentsTime(shop,
                        appointment.getDate(),
                        appointment.getTime())
                .isEmpty();
    }

    public void cancelAppointment(Long id) {
        Optional<Appointment> appointmentOptional = appointmentRepo.findById(id);
        Appointment canceledAppointment = appointmentOptional.orElseThrow(() -> new NoSuchElementException("Appointment not found"));

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

    }

    public List<Appointment> getAppointments(String shopName) {
        // Fetch the shop by name
        Optional<Shop> shopOptional = shopRepo.findByName(shopName);
        // Check if the shop exists
        Shop shop = shopOptional.orElseThrow(() -> new NoSuchElementException("Shop not found"));
        List<CustomerData> customerDataList = customerDataRepo.findByShop(shop);
        List<Appointment> appointments = new ArrayList<>();
        // Iterate through customer data and collect appointments
        for (CustomerData customerData : customerDataList) {
            appointments.addAll(customerData.getAppointments());
        }
        return appointments;
    }

    public void addShop(Shop shop) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepo.findByEmail(userEmail).orElseThrow();
        Optional<AdminData> adminData = adminDataRepo.findByUserEntity(userEntity);

        checkIfNameAlreadyExist(shop.getName());

        if (adminData.isPresent()) { // if the admin already have a shop in the app add it the new one to the list
            adminData.get().getShops().add(shop);
            shop.setAdminData(adminData.get());
            shopRepo.save(shop);
            adminDataRepo.save(adminData.get());
        } else { // if is the first shop that the admin create, add a new AdminData to the database
            List<Shop> shops = new ArrayList<>();
            AdminData newAdminData = new AdminData(1L, userEntity, shops);
            shop.setAdminData(newAdminData);
            shopRepo.save(shop);
            shops.add(shop);
            adminDataRepo.save(newAdminData);
        }
    }

    public Shop editShop(ShopUpdateRequest shop) {
        Optional<Shop> shopOptional = shopRepo.findById(shop.getId());
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (shopOptional.isPresent()) { // shop exist
            String ownerEmail = shopOptional.get().getAdminData().getUserEntity().getEmail();
            if (userEmail.equals(ownerEmail)) { // checks if is the owner of the shop
                shopOptional.get().setCost(shop.getCost());
                shopOptional.get().setServicesOptions(shop.getServicesOptions());
                shopOptional.get().setName(shop.getName());
                shopOptional.get().setAddress(shop.getAddress());
                shopOptional.get().setDescription(shop.getDescription());
                shopOptional.get().setTelephone(shop.getTelephone());
                shopRepo.save(shopOptional.get());
                return shopOptional.get();
            } else {
                throw new RuntimeException("You don't have the permissions");
            }
        } else {
            throw new RuntimeException("Shop doesn't exist");
        }
    }

    public void deleteShop(String shopName) {
        Optional<Shop> shopOptional = shopRepo.findByName(shopName);
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        canMakeChanges(userRepo.findByEmail(userEmail).orElseThrow(() -> new NoSuchElementException("AdminData not found")));
        Shop canceledShop = shopOptional.orElseThrow(() -> new NoSuchElementException("Shop not found"));
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

    public Shop searchShopById(Long id) {
        Optional<Shop> shopOptional = shopRepo.findById(id);
        return shopOptional.orElseThrow(() -> new NoSuchElementException("Shop not found"));
    }

    public List<Shop> getShops() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminDataRepo.findByUserEntityEmail(userEmail).orElseThrow().getShops();
    }

    public List<Shop> searchByLocationAndService(String location, String service) {
        List<Shop> shopList = shopRepo.findShopByLocationAndService(location, service);
        if (!shopList.isEmpty()) {
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
        Map<String, List<String>> datesAndTime = new HashMap<>();

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Appointment appointment : getAppointments(shopName)) {
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
        return datesAndTime;
    }

    public Shop getShopByAdminDataEmail(String email) {
        Optional<Shop> shopOptional = shopRepo.findByAdminData_UserEntity_Email(email);
        return shopOptional.orElseThrow(() -> new NoSuchElementException("Shop not found"));
    }

}