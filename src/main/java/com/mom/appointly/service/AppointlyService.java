package com.mom.appointly.service;

import com.mom.appointly.model.*;
import com.mom.appointly.repository.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
public class AppointlyService {
    private final UserRepo userRepo;
    private final ShopRepo shopRepo;
    private final AdminDataRepo adminDataRepo;

    public AdminData getAdminData() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName(); // return the email of the user that is connected
        Optional<AdminData> adminData = adminDataRepo.findByUserEntityEmail(email); // get the admin data that is connected
        return adminData.orElseThrow(() -> new NoSuchElementException("AdminData not found"));
    }

    public void canMakeChanges(UserEntity userEntity) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity connectedUser = userRepo.findByEmail(userEmail).orElseThrow();
        // if the user doesn't own the change he wants to make or is not the admin throws exception
        System.out.println(userEntity.getRole());
        System.out.println(userEntity.getRole().equals(Role.USER));
        if (userEntity.getRole().equals(Role.USER)) { // add one more check to check if is the owner of the shop
            throw new RuntimeException("You don't have the permissions");
        }
    }

}