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

    public Shop(String name, String location, String address, String telephone, String openHour, String closeHour, float rating, String dis, String service, boolean partner, List<String> serviceOptions, List<String> cost, List<String> personnel) {
        this.name = name;
        this.location = location;
        this.address = address;
        this.telephone = telephone;
        this.openHour = openHour;
        this.closeHour = closeHour;
        this.rating = rating;
        this.dis = dis;
        this.service = service;
        this.partner = partner;
        this.serviceOptions = serviceOptions;
        this.cost = cost;
        this.personnel = personnel;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shop")
    @JsonIgnore
    private List<CustomerData> customerData;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "admin_data_id")
    @JsonIgnore
    private AdminData adminData;

}
