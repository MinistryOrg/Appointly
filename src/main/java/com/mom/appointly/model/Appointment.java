package com.mom.appointly.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.sql.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String service;
    private float cost;
    private Date date;
    private Time time;
    private String personnel;
    @Transient
    private String userFirstname;
    @Transient
    private String userLastname;

    @ManyToOne
    @JoinColumn(name = "customer_data_id")
    @JsonIgnore
    private CustomerData customerData;
    // constructor for the tests
    public Appointment(String service, float cost, Date date, Time time, String personnel) {
        this.service = service;
        this.cost = cost;
        this.date = date;
        this.time = time;
        this.personnel = personnel;
    }

    public String getUserFirstname() {
        return customerData != null ? customerData.getUserEntity().getFirstname(): null;
    }

    public String getUserLastname() {
        return customerData != null ? customerData.getUserEntity().getLastname(): null;
    }
}
