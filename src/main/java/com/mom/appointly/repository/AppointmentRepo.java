package com.mom.appointly.repository;

import com.mom.appointly.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Time;
import java.util.Date;
import java.util.Optional;

public interface AppointmentRepo extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findAppointmentByDateAndTime(Date date, Time time);
    Optional<Appointment> findAppointmentByDate(Date date);
}
