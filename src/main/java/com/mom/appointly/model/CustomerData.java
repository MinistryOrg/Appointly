package com.mom.appointly.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

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
    @JoinColumn(name = "shop_id") // in this shop got this number of  appointments
    private Shop shop;

    @OneToMany(mappedBy = "customerData", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    @Override // for the tests
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerData that = (CustomerData) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userEntity, that.userEntity) &&
                Objects.equals(shop, that.shop) &&
                Objects.equals(((CustomerData) o).appointments, that.appointments);
    }

}
