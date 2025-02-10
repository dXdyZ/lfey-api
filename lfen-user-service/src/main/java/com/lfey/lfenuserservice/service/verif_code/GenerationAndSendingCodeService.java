package com.lfey.lfenuserservice.service.verif_code;

import com.lfey.lfenuserservice.entity.PendingUser;
import com.lfey.lfenuserservice.entity.User;
import com.lfey.lfenuserservice.entity.UserVerification;
import com.lfey.lfenuserservice.rabbit.UserEventPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
public class GenerationAndSendingCodeService {
    private final PendingUserService pendingUserService;
    private final UserEventPublisherService userEventPublisherService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public GenerationAndSendingCodeService(PendingUserService pendingUserService, UserEventPublisherService userEventPublisherService,
                                           PasswordEncoder passwordEncoder) {
        this.pendingUserService = pendingUserService;
        this.userEventPublisherService = userEventPublisherService;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public void generationAndSending(User user) {
        String code = String.format("%06d", new Random().nextInt(999999));
        log.info("generate token from service: {}", code);
        PendingUser pendingUser = PendingUser.builder()
                .code(code)
                .email(user.getEmail())
                .encryptPassword(passwordEncoder.encode(user.getPassword()))
                .localDateTime(LocalDateTime.now().plusMinutes(15))
                .username(user.getUsername())
                .build();
        if (!pendingUserService.existsByEmail(user.getEmail())){
            pendingUserService.savePendingUser(pendingUser);
            userEventPublisherService.publisherCondeEvent(new UserVerification(user.getEmail(), code));
        } else {
            pendingUserService.removePendingUser(user.getEmail());
            pendingUserService.savePendingUser(pendingUser);
            userEventPublisherService.publisherCondeEvent(new UserVerification(user.getEmail(), code));
        }
    }
}
