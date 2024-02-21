package com.mom.appointly.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.sql.Time;

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

    public String getUserFirstname() {
        return customerData != null ? customerData.getUserEntity().getFirstname(): null;
    }

    public String getUserLastname() {
        return customerData != null ? customerData.getUserEntity().getLastname(): null;
    }
}
