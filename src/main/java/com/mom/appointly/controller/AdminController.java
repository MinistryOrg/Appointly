package com.mom.appointly.controller;

import com.mom.appointly.model.*;
import com.mom.appointly.service.AppointlyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/appointly/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AppointlyService appointlyService;
    @GetMapping("/users")
    public Optional<CustomerData> getUsers(){
       return appointlyService.getCustomerData();
    }
    @PostMapping("/addShop")
    public void addShop(@RequestBody Shop shop){
        appointlyService.addShop(shop);
    }
    @GetMapping("/appointments")
    private ResponseEntity<?> getAppointments(){
        return new ResponseEntity<>(appointlyService.getAppointments(), HttpStatus.OK);
    }
}
