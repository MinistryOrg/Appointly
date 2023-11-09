package com.mom.appointly.controller;

import com.mom.appointly.model.Appointment;
import com.mom.appointly.service.AppointlyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final AppointlyService appointlyService;

    @PostMapping("/makeAppointment")
    private void makeAppointment(@RequestParam String shopName, @RequestBody Appointment appointment){
        appointlyService.makeAppointment(shopName, appointment);
    }

    @PatchMapping("/editApointment")
    private void editAppointment(@RequestBody Appointment appointment){
        appointlyService.editAppointment(appointment);
    }

    @DeleteMapping("/cancelAppointment")
    private void cancelAppointment(@RequestBody Appointment appointment){
        appointlyService.cancelAppointment(appointment);
    }

    @GetMapping("/appointments")
    private Optional<Appointment> getAppointments(){
        return appointlyService.getAppointments();
    }

}
