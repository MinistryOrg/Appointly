package com.mom.appointly.service;

import com.mom.appointly.model.*;
import com.mom.appointly.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointlyService {
    private final UserRepo userRepo;
    private final ShopRepo shopRepo;
    private final AppointmentRepo appointmentRepo;
    private final CustomerDataRepo customerDataRepo;
    private final AdminDataRepo adminDataRepo;

    public List<UserEntity> getUsers(){
        return userRepo.findAll();
    }

    public Optional<CustomerData> getCustomerData() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<AdminData> adminData = adminDataRepo.findByUserEntityEmail(email);
        return customerDataRepo
                .findByShopName(adminData.get().getShops().get(0).getName());// add the mapped by to get the appointments
    }

    public CustomerData makeAppointment(String shopName, Appointment appointment) {
        // checks if the user already make an appointment in the specific shop
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // get the email of the user that is connected
        UserEntity userEntity = userRepo.findByEmail(userEmail).get();
        Shop shop = shopRepo.findByName(shopName).get();
        Optional<CustomerData> customerData = customerDataRepo.findByUserEntityAndShop(userEntity,shop);

        if(customerData.isPresent()){ // check if the user already have make an appointment in this shop to add it to the list
            appointment.setCustomerData(customerData.get());
            appointmentRepo.save(appointment);
            customerData.get().getAppointments().add(appointment);
            return customerDataRepo.save(customerData.get());
        } else {
            List<Appointment> appointments = new ArrayList<>();
            appointments.add(appointment);
            CustomerData customer = customerDataRepo.save(new CustomerData(userEntity, shop, appointments));
            appointment.setCustomerData(customer);
            appointmentRepo.save(appointment);
            return customer;
        }

    }
    // TODO : check if the appointment id is the same with the customer id
    //  that is store to the customer data, so the user to not change other users appointment
    //  if the role is admin and the owner of the shop to be able to change for all the users
    public Appointment editAppointment(Appointment appointment) {
        Optional<Appointment> optionalAppointment = appointmentRepo.findById(appointment.getId());

        if(optionalAppointment.isPresent()){
            return appointmentRepo.save(appointment);
        }

        return null;
    }

    public void cancelAppointment(Appointment appointment) {
        Optional<Appointment> appointmentOptional = appointmentRepo.findById(appointment.getId());

        if (appointmentOptional.isPresent()) {
            Appointment canceledAppointment = appointmentOptional.get();
            CustomerData customerData = canceledAppointment.getCustomerData();
            // Remove the appointment from the  list of appointments
            customerData.getAppointments().remove(canceledAppointment);
            // Set null the customer_data from the canceled appointment to be able to delete
            canceledAppointment.setCustomerData(null);
            // Check if the last appointment for the customer_data
            if (customerData.getAppointments().isEmpty()) {
                customerDataRepo.delete(customerData);
            }
            appointmentRepo.delete(canceledAppointment);
        }
    }

    public Optional<Appointment> getAppointments() {
        return null;
    }

    public void addShop(Shop shop) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepo.findByEmail(userEmail).get();
        Optional<AdminData> adminData = adminDataRepo.findByUserEntity(userEntity);
        if(adminData.isPresent()){
            adminData.get().getShops().add(shop);
            shopRepo.save(shop);
            adminDataRepo.save(adminData.get());
        }else {
            List<Shop> shops = new ArrayList<>();
            shopRepo.save(shop);
            shops.add(shop);
            adminDataRepo.save(new AdminData(null, userEntity, shops));
        }
        // check if already a same name shop exist
    }
}
