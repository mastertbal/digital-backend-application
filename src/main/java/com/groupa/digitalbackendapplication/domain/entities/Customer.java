package com.groupa.digitalbackendapplication.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "customers")
@Getter
@Setter
@ToString
public class Customer extends User{

    @Column(name = "transaction_pin")
    private String transactionCode;

    @Column(name = "address", nullable = false, length = 100)
    private String address;

    @Column(name = "nin", unique = true,  length = 100)
    private String nin;

    @Column(name = "bvn", unique = true, length = 100)
    private String bvn;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Account> accounts = new HashSet<>();
}
