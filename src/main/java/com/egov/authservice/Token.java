package com.egov.authservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tokens")
public class Token
{
    @Id
    @Size(max = 20)
    @Column(name = "token", nullable = false, length = 20)
    private String token;

    @Column(name = "userid")
    private Long userid;

    @Size(max = 10)
    @Column(name = "status", length = 10)
    private String status;

}