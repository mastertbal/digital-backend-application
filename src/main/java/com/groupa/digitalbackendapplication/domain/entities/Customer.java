package com.groupa.digitalbackendapplication.domain.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer extends User{

    @Column(name = "address")
    private String address;

    @Column(name = "account_type")
    @Enumerated(value = EnumType.STRING)
    private AccountType accountType;

    @Column(name = "nin", unique = true)
    private String nin;

    @Column(name = "bvn", unique = true)
    private String bvn;
}
