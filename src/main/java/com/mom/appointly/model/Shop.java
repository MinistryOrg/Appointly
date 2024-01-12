package com.mom.appointly.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String location;
    private String address;
    private String telephone;
    private String openHour;
    private String closeHour;
    private float  rating;
    private String dis;
    private String service;
    private boolean partner;
    private List<String> serviceOptions;
    private List<String> cost;
    private List<String> personnel;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shop")
    @JsonIgnore
    private List<CustomerData> customerData;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "admin_data_id")
    @JsonIgnore
    private AdminData adminData;

}
