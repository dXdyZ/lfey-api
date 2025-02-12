package com.lfey.lfenuserservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthToken {
    private String token;
}
