package com.mom.appointly.controller;

import com.mom.appointly.model.Appointment;
import com.mom.appointly.model.Shop;
import com.mom.appointly.model.ShopUpdateRequest;
import com.mom.appointly.service.AppointlyService;
import com.mom.appointly.service.AppointmentService;
import com.mom.appointly.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointly/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AppointlyService appointlyService;
    private final AppointmentService appointmentService;
    private final ShopService shopService;

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        return new ResponseEntity<>(appointlyService.getAdminData(), HttpStatus.OK);
    }

    @GetMapping("/shops")
    public ResponseEntity<?> getShops() {
        return new ResponseEntity<>(shopService.getShops(), HttpStatus.OK);
    }

    @PostMapping("/addShop")
    public void addShop(@RequestBody Shop shop) {
        shopService.addShop(shop);
    }

    @PatchMapping("/editShop")
    public ResponseEntity<?> editShop(@RequestBody ShopUpdateRequest shop) {
        return new ResponseEntity<>(shopService.editShop(shop), HttpStatus.OK);
    }

    @DeleteMapping("/deleteShop")
    private ResponseEntity<?> deleteShop(@RequestParam String shopName) {
        shopService.deleteShop(shopName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/editAppointment")
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

    @GetMapping("/appointments")
    private ResponseEntity<?> getAppointments(@RequestParam String shopName) {
        return new ResponseEntity<>(appointmentService.getAppointments(shopName), HttpStatus.OK);
    }

    @GetMapping("/getShop")
    private ResponseEntity<?> getShop(@RequestParam String email) {
        return new ResponseEntity<>(shopService.getShopByAdminDataEmail(email), HttpStatus.OK);
    }


}
