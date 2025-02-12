package com.lfey.lfenuserservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Новый пароль пользователя")
public class UpdatePassword {
    private String password;
}
