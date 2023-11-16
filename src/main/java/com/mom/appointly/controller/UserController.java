package com.mom.appointly.controller;

import com.mom.appointly.model.Appointment;
import com.mom.appointly.service.AppointlyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/appointly/user")
@RequiredArgsConstructor
public class UserController {
    private final AppointlyService appointlyService;

    @PostMapping("/makeAppointment")
    private ResponseEntity<?> makeAppointment(@RequestParam String shopName, @RequestBody Appointment appointment){
        return new ResponseEntity<>
                (appointlyService.makeAppointment(shopName, appointment)
                , HttpStatus.CREATED);
    }

    @PutMapping("/editAppointment")
    private ResponseEntity<?> editAppointment(@RequestBody Appointment appointment){
        System.out.println("Trying to edit");
        return new ResponseEntity<>
                (appointlyService.editAppointment(appointment),
                        HttpStatus.OK);
    }

    @DeleteMapping("/cancelAppointment")
    private ResponseEntity<?> cancelAppointment(@RequestBody Appointment appointment){
        appointlyService.cancelAppointment(appointment);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/dates")
    private ResponseEntity<?> getDates(@RequestParam String shopName){
        return new ResponseEntity<>(appointlyService.getDates(shopName), HttpStatus.OK);
    }

}
