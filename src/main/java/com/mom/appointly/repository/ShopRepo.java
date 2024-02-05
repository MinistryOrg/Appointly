package com.mom.appointly.repository;

import com.mom.appointly.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopRepo extends JpaRepository<Shop, Long> {
    Optional<Shop> findByName(String name);

    Optional<Shop> findShopByCustomerDataShop(Shop shop);
    List<Shop> findShopByLocationAndService(String location, String service);
    Optional<Shop> findByAdminData_UserEntity_Email(String email);
}
