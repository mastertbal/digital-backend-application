package com.groupa.digitalbackendapplication.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "customers")
@Getter
public class Customer extends User{

    @Column(name = "address")
    private String address;

    @Column(name = "nin", unique = true)
    private String nin;

    @Column(name = "bvn", unique = true)
    private String bvn;
}
