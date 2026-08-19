package com.groupa.digitalbackendapplication.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@Table(name = "Admin")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User{

    @Column(name = "adminId", nullable = false)
    private String adminId;

    @Column(name = "address", nullable = false)
    private String address;


}
