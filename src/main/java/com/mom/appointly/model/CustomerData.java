package com.mom.appointly.model;

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
public class CustomerData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_entity_id")
    private UserEntity userEntity;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @OneToMany(mappedBy = "customerData",  cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    public CustomerData(UserEntity userEntity, Shop shop, List<Appointment> appointment) {
        this.userEntity = userEntity;
        this.shop = shop;
        this.appointments = appointment;
    }
}
