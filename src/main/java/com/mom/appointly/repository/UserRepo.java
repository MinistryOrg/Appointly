package com.mom.appointly.repository;

import com.mom.appointly.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {
}
