package com.mom.appointly.service;

import com.mom.appointly.model.AdminData;
import com.mom.appointly.repository.AdminDataRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AppointlyService {
    private final AdminDataRepo adminDataRepo;

    public AdminData getAdminData() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName(); // return the email of the user that is connected
        Optional<AdminData> adminData = adminDataRepo.findByUserEntityEmail(email); // get the admin data that is connected
        return adminData.orElseThrow(() -> new NoSuchElementException("AdminData not found"));
    }

}