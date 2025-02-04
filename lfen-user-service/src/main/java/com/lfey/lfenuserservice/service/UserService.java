package com.lfey.lfenuserservice.service;

import com.lfey.lfenuserservice.entity.User;
import com.lfey.lfenuserservice.exception.DuplicateUserException;
import com.lfey.lfenuserservice.exception.UserNotFoundException;
import com.lfey.lfenuserservice.rabbit.UserEventPublisherService;
import com.lfey.lfenuserservice.repository.UserRepository;
import com.lfey.lfenuserservice.service.code.GenerationAndSendingCodeService;
import com.lfey.lfenuserservice.service.code.VerificationCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserEventPublisherService userEventPublisherService;
    private final UserRepository userRepository;
    private final VerificationCode verificationCode;
    private final GenerationAndSendingCodeService generationAndSendingCodeService;

    @Autowired
    public UserService(UserEventPublisherService userEventPublisherService, UserRepository userRepository,
                       VerificationCode verificationCode, GenerationAndSendingCodeService generationAndSendingService) {
        this.userEventPublisherService = userEventPublisherService;
        this.userRepository = userRepository;
        this.verificationCode = verificationCode;
        this.generationAndSendingCodeService = generationAndSendingService;
    }


    public void registerUser(User user) throws DuplicateUserException {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateUserException("User by email: " + user.getEmail() + " already exists");
        }
        generationAndSendingCodeService.generationAndSending(user);
    }

    public void confirmCode(String email, String code) throws RuntimeException{
        userRepository.save(verificationCode.confirmCode(email, code));
    }

    public User getUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User by email: " + email + " not found")
        );
    }

    public List<User> getUsersByUsername(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("User by username: " + username + " not found")
        );
    }

    public void deleteUserByEmail(String email) throws UserNotFoundException {
        getUserByEmail(email);
        userRepository.deleteByEmail(email);
    }

    public User getUserById(Long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User by id: " + id + " not found")
        );
    }


    @Transactional
    public void updateUsername(String userEmail, String username) {
        User user = userRepository.findByEmail(userEmail).get();
        user.setUsername(username);
        userRepository.save(user);
    }

    // Добавить шифрование пароля
    @Transactional
    public void updatePassword(String userEmail, String password) {
        User user = userRepository.findByEmail(userEmail).get();
        user.setPassword(password);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
}
