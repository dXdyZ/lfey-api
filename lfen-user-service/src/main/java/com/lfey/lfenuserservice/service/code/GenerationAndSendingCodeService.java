package com.lfey.lfenuserservice.service.code;

import com.lfey.lfenuserservice.entity.PendingUser;
import com.lfey.lfenuserservice.entity.User;
import com.lfey.lfenuserservice.entity.UserVerification;
import com.lfey.lfenuserservice.rabbit.UserEventPublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class GenerationAndSendingCodeService {
    private final PendingUserService pendingUserService;
    private final UserEventPublisherService userEventPublisherService;

    @Autowired
    public GenerationAndSendingCodeService(PendingUserService pendingUserService, UserEventPublisherService userEventPublisherService) {
        this.pendingUserService = pendingUserService;
        this.userEventPublisherService = userEventPublisherService;
    }

    // Добавить шифрование пароля
    public void generationAndSending(User user) {
        String code = String.format("%06d", new Random().nextInt(999999));
        pendingUserService.savePendingUser(
                PendingUser.builder()
                        .code(code)
                        .email(user.getEmail())
                        .encryptPassword(user.getPassword())
                        .localDateTime(LocalDateTime.now().plusMinutes(15))
                        .username(user.getUsername())
                        .build()
        );
        userEventPublisherService.publisherCondeEvent(new UserVerification(code, user.getEmail()));
    }
}
