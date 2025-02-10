package com.lfey.lfenuserservice.service;

import com.lfey.lfenuserservice.dto.AuthToken;
import com.lfey.lfenuserservice.dto.UserAuth;
import com.lfey.lfenuserservice.dto.UserRegister;
import com.lfey.lfenuserservice.entity.User;
import com.lfey.lfenuserservice.exception.DuplicateUserException;
import com.lfey.lfenuserservice.exception.PasswordMatchesOldException;
import com.lfey.lfenuserservice.exception.UserNotFoundException;
import com.lfey.lfenuserservice.repository.jpa.UserRepository;
import com.lfey.lfenuserservice.service.auth.AuthService;
import com.lfey.lfenuserservice.service.auth.CustomUserDetails;
import com.lfey.lfenuserservice.service.auth.JwtUtils;
import com.lfey.lfenuserservice.service.verif_code.GenerationAndSendingCodeService;
import com.lfey.lfenuserservice.service.verif_code.VerificationCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final VerificationCode verificationCode;
    private final GenerationAndSendingCodeService generationAndSendingCodeService;
    private final JwtUtils jwtUtils;


    @Autowired
    public UserService(AuthService authService, UserRepository userRepository, VerificationCode verificationCode,
                       GenerationAndSendingCodeService generationAndSendingService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.verificationCode = verificationCode;
        this.generationAndSendingCodeService = generationAndSendingService;
        this.jwtUtils = jwtUtils;
    }

    public void registerUser(UserRegister user) throws DuplicateUserException {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateUserException("User by body: " + user.getEmail() + " already exists");
        }
        generationAndSendingCodeService.generationAndSending(User.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .password(user.getPassword())
                .build());
    }

    @Transactional
    public AuthToken confirmCode(String email, String code) throws RuntimeException{
        User user = userRepository.save(verificationCode.confirmCode(email, code));
        return new AuthToken(jwtUtils.generateToken(jwtUtils.createGetCustomUserDetails(user)));
    }

    public User getUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User by body: " + email + " not found")
        );
    }

    public List<User> getUsersByUsername(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("User by username: " + username + " not found")
        );
    }

    // Добавить обработку несуществующего пользователя по email
    public void deleteUserByEmail(String email) throws UserNotFoundException {
        userRepository.deleteByEmail(email);
    }

    public User getUserById(Long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User by id: " + id + " not found")
        );
    }

    @Transactional
    public AuthToken updateUsername(String userEmail, String username) {
        User user = userRepository.findByEmail(userEmail).get();
        user.setUsername(username);
        userRepository.save(user);
        return new AuthToken(jwtUtils.generateToken(jwtUtils.createGetCustomUserDetails(user)));
    }

    @Transactional
    public void updatePassword(String userEmail, String password) {
        User user = userRepository.findByEmail(userEmail).get();
        if (!user.getPassword().matches(password)) {
            user.setPassword(password);
            generationAndSendingCodeService.generationAndSending(user);
        } else throw new PasswordMatchesOldException("New password must not match the old password");
    }

    public AuthToken loginUser(UserAuth userLog) throws BadCredentialsException {
        return new AuthToken(authService.createToken(userLog));
    }
}
