package com.mom.appointly.controller;

import com.mom.appointly.model.Appointment;
import com.mom.appointly.service.AppointlyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointly/user")
@RequiredArgsConstructor
public class UserController {
    private final AppointlyService appointlyService;

    @PostMapping("/makeAppointment")
    private ResponseEntity<?> makeAppointment(@RequestParam String shopName, @RequestBody Appointment appointment) {
        return new ResponseEntity<>
                (appointlyService.makeAppointment(shopName, appointment)
                        ,HttpStatus.CREATED);
    }

    @PutMapping("/editAppointment")
    private ResponseEntity<?> editAppointment(@RequestBody Appointment appointment) {
        return new ResponseEntity<>
                (appointlyService.editAppointment(appointment),
                        HttpStatus.OK);
    }

    @DeleteMapping("/cancelAppointment")
    private ResponseEntity<?> cancelAppointment(@RequestParam Long id) {
        appointlyService.cancelAppointment(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/dates")
    private ResponseEntity<?> getDates(@RequestParam String shopName) {
        return new ResponseEntity<>(appointlyService.getDates(shopName), HttpStatus.OK);
    }

    @GetMapping("/shopsByLocationService")
    private ResponseEntity<?> getByLocationService(@RequestParam String location, @RequestParam String service){
        return new ResponseEntity<>(appointlyService.searchByLocationAndService(location, service), HttpStatus.OK);
    }

}
