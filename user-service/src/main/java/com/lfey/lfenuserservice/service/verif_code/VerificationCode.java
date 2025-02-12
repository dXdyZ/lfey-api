package com.lfey.lfenuserservice.service.verif_code;

import com.lfey.lfenuserservice.entity.PendingUser;
import com.lfey.lfenuserservice.entity.Role;
import com.lfey.lfenuserservice.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VerificationCode {
    private final PendingUserService pendingUserService;

    @Autowired
    public VerificationCode(PendingUserService pendingUserService) {
        this.pendingUserService = pendingUserService;
    }

    public User confirmCode(String email, String code) throws RuntimeException {
        PendingUser pendingUser = pendingUserService.getPendingUserById(email);
        if (LocalDateTime.now().isAfter(pendingUser.getLocalDateTime())) {
            throw new RuntimeException("Code expired");
        }
        if (!code.equals(pendingUser.getCode())) {
            throw new RuntimeException("Invalid code");
        }
        User user = User.builder()
                .email(pendingUser.getEmail())
                .password(pendingUser.getEncryptPassword())
                .username(pendingUser.getUsername())
                .role(Role.ROLE_USER)
                .build();
        pendingUserService.removePendingUser(pendingUser.getEmail());
        return user;
    }
}
