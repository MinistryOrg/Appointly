package com.mom.appointly.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
    private String type;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shop")
    @JsonIgnore
    private List<CustomerData> customerData;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "admin_data_id")
    @JsonIgnore
    private AdminData adminData;

}
