package com.lfey.lfenuserservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "данные регистрации пользователя")
public class UserRegister {
    private String username;
    private String email;
    private String password;
}
