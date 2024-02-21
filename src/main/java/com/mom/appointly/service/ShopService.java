package com.mom.appointly.service;

import com.mom.appointly.model.*;
import com.mom.appointly.repository.AdminDataRepo;
import com.mom.appointly.repository.ShopRepo;
import com.mom.appointly.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final UserRepo userRepo;
    private final ShopRepo shopRepo;
    private final AdminDataRepo adminDataRepo;

    public void addShop(Shop shop) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepo.findByEmail(userEmail).orElseThrow();
        Optional<AdminData> adminData = adminDataRepo.findByUserEntity(userEntity);

        checkIfNameAlreadyExist(shop.getName());

        if (adminData.isPresent()) { // if the admin already have a shop in the app add it the new one to the list
            adminData.get().getShops().add(shop);
            shop.setAdminData(adminData.get());
            shopRepo.save(shop);
            adminDataRepo.save(adminData.get());
        } else { // if is the first shop that the admin create, add a new AdminData to the database
            List<Shop> shops = new ArrayList<>();
            AdminData newAdminData = new AdminData(1L, userEntity, shops);
            shop.setAdminData(newAdminData);
            shopRepo.save(shop);
            shops.add(shop);
            adminDataRepo.save(newAdminData);
        }
    }

    public Shop editShop(ShopUpdateRequest shop) {
        Optional<Shop> shopOptional = shopRepo.findById(shop.getId());
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (shopOptional.isPresent()) { // shop exist
            String ownerEmail = shopOptional.get().getAdminData().getUserEntity().getEmail();
            if (userEmail.equals(ownerEmail)) { // checks if is the owner of the shop
                shopOptional.get().setCost(shop.getCost());
                shopOptional.get().setServicesOptions(shop.getServicesOptions());
                shopOptional.get().setName(shop.getName());
                shopOptional.get().setAddress(shop.getAddress());
                shopOptional.get().setDescription(shop.getDescription());
                shopOptional.get().setTelephone(shop.getTelephone());
                shopRepo.save(shopOptional.get());
                return shopOptional.get();
            } else {
                throw new RuntimeException("You don't have the permissions");
            }
        } else {
            throw new RuntimeException("Shop doesn't exist");
        }
    }

    public void deleteShop(String shopName) {
        Optional<Shop> shopOptional = shopRepo.findByName(shopName);
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        canMakeChanges(userRepo.findByEmail(userEmail).orElseThrow(() -> new NoSuchElementException("AdminData not found")));
        Shop canceledShop = shopOptional.orElseThrow(() -> new NoSuchElementException("Shop not found"));
        AdminData adminData = canceledShop.getAdminData();
        // Remove the shop from the  list of appointments
        adminData.getShops().remove(canceledShop);
        // Set null the admin_data from the canceled appointment to be able to delete
        canceledShop.setAdminData(null);
        // Check if the last appointment for the customer_data
        if (adminData.getShops().isEmpty()) {
            adminDataRepo.delete(adminData);
        }
        shopRepo.delete(canceledShop);

    }

    public Shop searchShopById(Long id) {
        Optional<Shop> shopOptional = shopRepo.findById(id);
        return shopOptional.orElseThrow(() -> new NoSuchElementException("Shop not found"));
    }

    public List<Shop> getShops() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminDataRepo.findByUserEntityEmail(userEmail).orElseThrow().getShops();
    }

    public List<Shop> searchByLocationAndService(String location, String service) {
        List<Shop> shopList = shopRepo.findShopByLocationAndService(location, service);
        if (!shopList.isEmpty()) {
            return shopList;
        }
        throw new RuntimeException("Shop doesn't exist");
    }

    public Shop getShopByAdminDataEmail(String email) {
        Optional<Shop> shopOptional = shopRepo.findByAdminData_UserEntity_Email(email);
        return shopOptional.orElseThrow(() -> new NoSuchElementException("Shop not found"));
    }

    public void canMakeChanges(UserEntity userEntity) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity connectedUser = userRepo.findByEmail(userEmail).orElseThrow();
        // if the user doesn't own the change he wants to make or is not the admin throws exception
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
