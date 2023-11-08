package com.mom.appointly.repository;

import com.mom.appointly.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepo extends JpaRepository<Shop, Long> {
}
