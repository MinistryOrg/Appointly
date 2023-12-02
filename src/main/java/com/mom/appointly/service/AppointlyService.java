package com.mom.appointly.service;

import com.mom.appointly.model.*;
import com.mom.appointly.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Time;
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
//        return customerDataRepo
//                .findByShopName(adminData.get().getShops().get(0).getName());// add the mapped by to get the appointments
        return adminData.get();
    }

    public CustomerData makeAppointment(String shopName, Appointment appointment) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // get the email of the user that is connected
        UserEntity userEntity = userRepo.findByEmail(userEmail).get();
        Shop shop = shopRepo.findByName(shopName).get();
        Optional<CustomerData> customerData = customerDataRepo.findByUserEntityAndShop(userEntity, shop);

        if (customerData.isPresent() && appointmentRepo.findAppointmentByDateAndTime(appointment.getDate(), appointment.getTime()).isEmpty()) { // check if the user already have make an appointment in this shop to add it to the list
            appointment.setCustomerData(customerData.get());
            appointmentRepo.save(appointment);
            customerData.get().getAppointments().add(appointment);
            return customerDataRepo.save(customerData.get());
        } else if (appointmentRepo.findAppointmentByDateAndTime(appointment.getDate(), appointment.getTime()).isEmpty()) { // if is the first appointment of the user
            List<Appointment> appointments = new ArrayList<>();
            appointments.add(appointment);
            CustomerData customer = customerDataRepo.save(new CustomerData(userEntity, shop, appointments));
            appointment.setCustomerData(customer);
            appointmentRepo.save(appointment);
            return customer;
        }
        throw new RuntimeException(); // it means the appointment is already exist and returns 403 forbidden
    }

    // TODO : check if the appointment id is the same with the customer id
    //  that is store to the customer data, so the user to not change other users appointment
    //  if the role is admin and the owner of the shop to be able to change for all the users
    public Appointment editAppointment(Appointment appointment) {
        Optional<Appointment> optionalAppointment = appointmentRepo.findById(appointment.getId());

        if (optionalAppointment.isPresent()) {
            return appointmentRepo.save(appointment);
        }

        throw new RuntimeException(); // appointment doesn't exit for edit and returns 403 forbidden
    }

    public void cancelAppointment(Long id) {
        Optional<Appointment> appointmentOptional = appointmentRepo.findById(id);

        if (appointmentOptional.isPresent()) { // check if the appointment exist
            Appointment canceledAppointment = appointmentOptional.get();
            CustomerData customerData = canceledAppointment.getCustomerData();
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
    }

    public List<Appointment> getAppointments(String shopName) {
        if (customerDataRepo.findByShopName(shopName).isPresent()) {
            return customerDataRepo.findByShopName(shopName).get().getAppointments();
        }
        throw new RuntimeException();
    }

    public void addShop(Shop shop) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepo.findByEmail(userEmail).get();
        Optional<AdminData> adminData = adminDataRepo.findByUserEntity(userEntity);

        checkIfNameAlreadyExist(shop.getName());
        
        if (adminData.isPresent()) { // if the admin already have a shop in the app add it the new one to the list
            adminData.get().getShops().add(shop);
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
            if (userEmail.equals(ownerEmail)) {
                shopRepo.save(shop);
                return shop;
            }
        }
        throw new RuntimeException();
    }

    public void deleteShop(String shopName) {
        Optional<Shop> shopOptional = shopRepo.findByName(shopName);

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

    public List<Shop> getShops() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminDataRepo.findByUserEntityEmail(userEmail).get().getShops();
    }

    public List<Shop> searchByLocationAndService(String location, String service){
        List <Shop> shopList =  shopRepo.findShopByLocationAndService(location, service);
        if(!shopList.isEmpty()){
            return shopList;
        }
        throw new RuntimeException();
    }

    public List<Shop> searchByName(String shopName){
        // get all the shops
        // return the shops that contains the characters
        return null;
    }

    public void checkIfNameAlreadyExist(String name) {
        if (shopRepo.findByName(name).isPresent()) {
            throw new RuntimeException();
        }
    }

    public Object getDates(String shopName) {
        Optional<CustomerData> customerData = customerDataRepo.findByShopName(shopName);
        Map<Date, List<Time>> datesAndTime = new HashMap<>();
        if (customerData.isPresent()) {
            for (Appointment appointment : customerData.get().getAppointments()) {
                if (datesAndTime.containsKey(appointment.getDate())) { // if the specific day already have an appointment add to the key of the date
                    datesAndTime.get(appointment.getDate()).add(appointment.getTime());
                } else {
                    List<Time> timeList = new ArrayList<>();
                    timeList.add(appointment.getTime());
                    datesAndTime.put(appointment.getDate(), timeList);
                }
            }
        }
        return datesAndTime;
    }

}
