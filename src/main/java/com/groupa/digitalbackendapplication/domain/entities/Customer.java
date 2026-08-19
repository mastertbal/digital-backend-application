package com.groupa.digitalbackendapplication.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "customers")
@Getter
@Setter
@ToString
public class Customer extends User{

    @Column(name = "address", nullable = false, length = 100)
    private String address;

    @Column(name = "nin", unique = true,  length = 25)
    private String nin;

    @Column(name = "bvn", unique = true, length = 25)
    private String bvn;
}
