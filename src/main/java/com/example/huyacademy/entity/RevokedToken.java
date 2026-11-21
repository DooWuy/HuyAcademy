package com.example.huyacademy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {

    @Id
    @Column(length = 128)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
