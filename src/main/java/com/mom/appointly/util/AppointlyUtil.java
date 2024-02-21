package com.mom.appointly.util;

import com.mom.appointly.model.Role;
import com.mom.appointly.model.UserEntity;
import com.mom.appointly.repository.ShopRepo;
import com.mom.appointly.repository.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.mom.appointly.model.*;
import com.mom.appointly.repository.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
@AllArgsConstructor
@Component
public class AppointlyUtil {
    private final UserRepo userRepo;
    private final ShopRepo shopRepo;
    public void canMakeChanges(UserEntity userEntity) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity connectedUser = userRepo.findByEmail(userEmail).orElseThrow();
        // if the user doesn't own the change he wants to make or is not the admin throws exception
        System.out.println(userEntity.getRole());
        if (userEntity.getRole().equals(Role.USER)) {
            throw new RuntimeException("You don't have the permissions");
        }
    }
    public void checkIfNameAlreadyExist(String name) {
        if (shopRepo.findByName(name).isPresent()) {
            throw new RuntimeException("Name already exist");
        }
    }
}
