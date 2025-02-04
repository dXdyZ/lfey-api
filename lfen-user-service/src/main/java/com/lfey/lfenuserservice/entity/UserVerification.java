package com.lfey.lfenuserservice.entity;

public record UserVerification(
        String code,
        String email
) {}
