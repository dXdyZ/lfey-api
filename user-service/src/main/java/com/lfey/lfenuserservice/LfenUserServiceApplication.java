package com.lfey.lfenuserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableCaching
@EnableJpaRepositories(basePackages = "com.lfey.lfenuserservice.repository.jpa")
@EnableRedisRepositories(basePackages = "com.lfey.lfenuserservice.repository.cache")
@SpringBootApplication
public class LfenUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LfenUserServiceApplication.class, args);
    }

}
