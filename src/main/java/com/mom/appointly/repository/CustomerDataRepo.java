package com.mom.appointly.repository;

import com.mom.appointly.model.CustomerData;
import com.mom.appointly.model.Shop;
import com.mom.appointly.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CustomerDataRepo extends JpaRepository<CustomerData, Long> {
    // find the shops
    Optional<CustomerData> findByShopId(Long id);
    Optional<CustomerData> findByShopName(String shopName);
    // find the users
    Optional<CustomerData> findByUserEntityId(Long id);
    Optional<CustomerData> findByUserEntityEmail(String email);
    Optional<CustomerData> findByUserEntityAndShop(UserEntity userEntity, Shop shop);
    Optional<CustomerData> findByShopAndAppointmentsDateAndAppointmentsTime(Shop shop, Date date, Time time);

    List<CustomerData> findByShop(Shop shop);
}
