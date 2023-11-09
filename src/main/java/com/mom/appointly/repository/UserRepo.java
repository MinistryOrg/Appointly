package com.mom.appointly.repository;

import com.mom.appointly.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<UserEntity, Long> {
    //Optional<UserEntity> findById(Long id);
    Optional<UserEntity> findByEmail(String email);

}
