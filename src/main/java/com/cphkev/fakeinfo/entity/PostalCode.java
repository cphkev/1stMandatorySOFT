package com.cphkev.fakeinfo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "postal_code")
public class PostalCode {

    @Id
    @Column(name = "cPostalCode")
    private String postalCode;

    @Column(name = "cTownName")
    private String townName;
}
