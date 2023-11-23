package com.mom.appointly.controller;

import com.mom.appointly.model.Shop;
import com.mom.appointly.service.AppointlyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointly/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AppointlyService appointlyService;

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        return new ResponseEntity<>(appointlyService.getCustomerData(), HttpStatus.OK);
    }

    @GetMapping("/shops")
    public ResponseEntity<?> getShops() {
        return new ResponseEntity<>(appointlyService.getShops(), HttpStatus.OK);
    }

    @PostMapping("/addShop")
    public void addShop(@RequestBody Shop shop) {
        appointlyService.addShop(shop);
    }

    @PatchMapping("/editShop")
    public ResponseEntity<?> editShop(@RequestParam String shopName, @RequestBody Shop shop) {
        return new ResponseEntity<>(appointlyService.editShop(shopName, shop), HttpStatus.OK);
    }

    @DeleteMapping("/deleteShop")
    private ResponseEntity<?> deleteShop(@RequestParam String shopName) {
        appointlyService.deleteShop(shopName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/appointments")
    private ResponseEntity<?> getAppointments(@RequestParam String shopName) {
        return new ResponseEntity<>(appointlyService.getAppointments(shopName), HttpStatus.OK);
    }
}
