package com.lfey.lfenuserservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.SqlReturnType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLog {
    private String email;
    private String password;
}
