package com.mom.appointly.repository;

import com.mom.appointly.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepo extends JpaRepository<Shop, Long> {
    //Optional<Shop> findById(Long id);
    Optional<Shop> findByName(String name);
}
