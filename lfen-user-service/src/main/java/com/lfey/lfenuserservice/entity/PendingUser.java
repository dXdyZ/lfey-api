package com.lfey.lfenuserservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingUser {
    @Id
    private String email;
    private String username;
    private String code;
    private String encryptPassword;
    private LocalDateTime localDateTime;
}

