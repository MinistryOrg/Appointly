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

    public void makeAppointment(String shopName, Appointment appointment) {
        // checks if the user already make an appointment in the specific shop
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepo.findByEmail(userEmail).get();
        Shop shop = shopRepo.findByName(shopName).get();
        Optional<CustomerData> customerData = customerDataRepo.findByUserEntityAndShop(userEntity,shop);
        if(customerData.isPresent()){ // check if the user already have make an appointment in this shop to add it to the list
            appointmentRepo.save(appointment);
            customerData.get().getAppointments().add(appointment);
            customerDataRepo.save(customerData.get());
        } else {
            List<Appointment> appointments = new ArrayList<>();
            appointments.add(appointment);
            appointmentRepo.save(appointment);
            customerDataRepo.save(new CustomerData(userEntity, shop, appointments));
        }

    }

    public void editAppointment(Appointment appointment) {
    }

    public void cancelAppointment(Appointment appointment) {
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
