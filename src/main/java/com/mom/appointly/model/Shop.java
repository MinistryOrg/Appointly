package com.mom.appointly.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
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
    private String description;
    @Column(length = 1024)
    private String about;
    private String service;
    private boolean partner;
    private List<String> servicesOptions;
    private List<Integer> cost;
    private List<String> personnel;
    private String backgroundImgPath;
    private List<String> shopImg;
    private List<String> serviceImg;
    private String shopLogo;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shop")
    @JsonIgnore
    private List<CustomerData> customerData;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "admin_data_id")
    @JsonIgnore
    private AdminData adminData;

}
