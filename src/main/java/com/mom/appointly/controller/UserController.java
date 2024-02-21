package com.mom.appointly.controller;

import com.mom.appointly.model.Appointment;
import com.mom.appointly.service.AppointlyService;
import com.mom.appointly.service.AppointmentService;
import com.mom.appointly.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointly/user")
@RequiredArgsConstructor
public class UserController {
    private final AppointlyService appointlyService;
    private final AppointmentService appointmentService;
    private final ShopService shopService;
    @PostMapping("/makeAppointment")
    private ResponseEntity<?> makeAppointment(@RequestParam String shopName, @RequestBody Appointment appointment) {
        return new ResponseEntity<>
                (appointmentService.makeAppointment(shopName, appointment)
                        ,HttpStatus.CREATED);
    }

    @PutMapping("/editAppointment")
    private ResponseEntity<?> editAppointment(@RequestBody Appointment appointment) {
        return new ResponseEntity<>
                (appointmentService.editAppointment(appointment),
                        HttpStatus.OK);
    }

    @DeleteMapping("/cancelAppointment")
    private ResponseEntity<?> cancelAppointment(@RequestParam Long id) {
        appointmentService.cancelAppointment(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/dates")
    private ResponseEntity<?> getDates(@RequestParam String shopName) {
        return new ResponseEntity<>(appointmentService.getDates(shopName), HttpStatus.OK);
    }

    @GetMapping("/shopsByLocationService")
    private ResponseEntity<?> getByLocationService(@RequestParam String location, @RequestParam String service){
        return new ResponseEntity<>(shopService.searchByLocationAndService(location, service), HttpStatus.OK);
    }

}
