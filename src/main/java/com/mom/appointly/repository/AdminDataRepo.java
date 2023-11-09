package com.mom.appointly.repository;

import com.mom.appointly.model.AdminData;
import com.mom.appointly.model.CustomerData;
import com.mom.appointly.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminDataRepo extends JpaRepository<AdminData, Long> {
    Optional<AdminData> findByUserEntity(UserEntity userEntity);
    Optional<AdminData> findByUserEntityEmail(String email);
}
