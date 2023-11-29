package com.mom.appointly.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.util.Date;

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
    private String type;
    private float cost;
    private Date date;
    private Time time;
    private String personnel;
    @ManyToOne
    @JoinColumn(name = "customer_data_id")
    @JsonIgnore
    private CustomerData customerData;

}
