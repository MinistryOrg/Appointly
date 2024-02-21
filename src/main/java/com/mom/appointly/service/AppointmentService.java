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
public class AppointmentService {
    private final UserRepo userRepo;
    private final ShopRepo shopRepo;
    private final AppointmentRepo appointmentRepo;
    private final CustomerDataRepo customerDataRepo;

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

        return appointmentRepo.save(existingAppointment);
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

    public void canMakeChanges(UserEntity userEntity) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity connectedUser = userRepo.findByEmail(userEmail).orElseThrow();
        // if the user doesn't own the change he wants to make or is not the admin throws exception
        if (userEntity.getRole().equals(Role.USER)) {
            throw new RuntimeException("You don't have the permissions");
        }
    }
}
