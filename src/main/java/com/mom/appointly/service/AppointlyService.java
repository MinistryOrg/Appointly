package com.mom.appointly.service;

import com.mom.appointly.model.Appointment;
import com.mom.appointly.model.CustomerData;
import com.mom.appointly.model.Shop;
import com.mom.appointly.model.UserEntity;
import com.mom.appointly.repository.AppointmentRepo;
import com.mom.appointly.repository.CustomerDataRepo;
import com.mom.appointly.repository.ShopRepo;
import com.mom.appointly.repository.UserRepo;
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
    public List<UserEntity> getUsers(){
        return userRepo.findAll();
    }

    public Optional<CustomerData> getCustomerData(String email) {
        return customerDataRepo.findByUserEntityEmail(email);
    }

    public void makeAppointment(String shopName, Appointment appointment) {
        // checks if the user already make an appointment in the specific shop
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepo.findByEmail(userEmail).get();
        Shop shop = shopRepo.findByName(shopName).get();

        if(customerDataRepo.findByUserEntityAndShop(userEntity,shop).isPresent()){
            appointmentRepo.save(appointment);
            System.out.println("Already exist in the database so i need to add the data to the list");
        } else {
            List<Appointment> appointments = new ArrayList<>();
            appointments.add(appointment);
            appointmentRepo.save(appointment);
            CustomerData customerData = new CustomerData(userEntity, shop, appointments);
            customerDataRepo.save(customerData);
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

        shopRepo.save(shop);
    }
}
