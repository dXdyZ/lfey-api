package com.lfey.lfenuserservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Data
@RedisHash(value = "RedisPendingUser", timeToLive = 1200) //TTL 20 min
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

