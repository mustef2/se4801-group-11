package com.sustainabilitytracker.entities;

import com.sustainabilitytracker.enums.CompanySize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "industry")
    private String industry;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "size")
    @Enumerated(EnumType.STRING)
    private CompanySize size;

    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "company")
    List<Department> departments;

    @OneToMany(mappedBy = "company")
    private List<User> users = new ArrayList<>();

    @Column(name = "phone")
    private String phone;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;


}