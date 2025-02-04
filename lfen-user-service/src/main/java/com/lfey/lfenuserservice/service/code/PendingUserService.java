package com.lfey.lfenuserservice.service.code;

import com.lfey.lfenuserservice.entity.PendingUser;
import com.lfey.lfenuserservice.repository.PendingUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PendingUserService {
    private final PendingUserRepository pendingUserRepository;

    @Autowired
    public PendingUserService(PendingUserRepository pendingUserRepository) {
        this.pendingUserRepository = pendingUserRepository;
    }

    public PendingUser getPendingUserById(String email) {
        return pendingUserRepository.findById(email).orElseThrow(
                () -> new RuntimeException("Request not found")
        );
    }

    public void savePendingUser(PendingUser pendingUser) {
        pendingUserRepository.save(pendingUser);
    }
}
