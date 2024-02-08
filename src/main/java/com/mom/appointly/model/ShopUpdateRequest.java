package com.mom.appointly.model;

import java.util.List;

public class ShopUpdateRequest {
    private Long id;
    private String name;
    private String address;
    private String description;
    private String telephone;
    private List<Integer> cost;
    private List<String> servicesOptions;

    public ShopUpdateRequest(Long id,String name, String address, String description, String telephone, List<Integer> cost, List<String> servicesOptions) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
        this.telephone = telephone;
        this.cost = cost;
        this.servicesOptions = servicesOptions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public List<Integer> getCost() {
        return cost;
    }

    public void setCost(List<Integer> cost) {
        this.cost = cost;
    }

    public List<String> getServicesOptions() {
        return servicesOptions;
    }

    public void setServicesOptions(List<String> servicesOptions) {
        this.servicesOptions = servicesOptions;
    }
}