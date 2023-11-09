package com.mom.appointly.repository;

import com.mom.appointly.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppointmentRepo extends JpaRepository<Appointment, Long> {
    //Optional<Appointment> findById(Long id);
}
