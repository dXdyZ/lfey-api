package com.lfey.lfenuserservice.service;

import com.lfey.lfenuserservice.dto.AuthToken;
import com.lfey.lfenuserservice.dto.UpdatePassword;
import com.lfey.lfenuserservice.dto.UserAuth;
import com.lfey.lfenuserservice.dto.UserRegister;
import com.lfey.lfenuserservice.entity.User;
import com.lfey.lfenuserservice.exception.DuplicateUserException;
import com.lfey.lfenuserservice.exception.PasswordMatchesOldException;
import com.lfey.lfenuserservice.exception.UserNotFoundException;
import com.lfey.lfenuserservice.exception.UsernameMatchesOldException;
import com.lfey.lfenuserservice.repository.jpa.UserRepository;
import com.lfey.lfenuserservice.service.auth.AuthService;
import com.lfey.lfenuserservice.service.auth.CustomUserDetails;
import com.lfey.lfenuserservice.service.auth.JwtUtils;
import com.lfey.lfenuserservice.service.verif_code.GenerationAndSendingCodeService;
import com.lfey.lfenuserservice.service.verif_code.VerificationCode;
import org.hibernate.annotations.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final VerificationCode verificationCode;
    private final GenerationAndSendingCodeService generationAndSendingCodeService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;


    @Autowired
    public UserService(AuthService authService, UserRepository userRepository, VerificationCode verificationCode,
                       GenerationAndSendingCodeService generationAndSendingService, JwtUtils jwtUtils,
                       PasswordEncoder passwordEncoder, CacheManager cacheManager) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.verificationCode = verificationCode;
        this.generationAndSendingCodeService = generationAndSendingService;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.cacheManager = cacheManager;
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

        Objects.requireNonNull(cacheManager.getCache("userByEmail")).put(user.getEmail(), user);
        Objects.requireNonNull(cacheManager.getCache("userById")).put(user.getId(), user);

        return new AuthToken(jwtUtils.generateToken(jwtUtils.createGetCustomUserDetails(user)));
    }

    @Cacheable(value = "userByEmail", key = "#email")
    public User getUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User by body: " + email + " not found")
        );
    }

    @Cacheable(value = "userByUsername", key = "#username")
    public List<User> getUsersByUsername(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("User by username: " + username + " not found")
        );
    }

    // Добавить обработку несуществующего пользователя по email
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "userById", key = "#result.id"),
                    @CacheEvict(value = "userByEmail", key = "#result.email"),
                    @CacheEvict(value = "userByUsername", allEntries = true)
            }
    )
    public User deleteUserByEmail(String email) throws UserNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User by email: " + email + " not found")
        );
        userRepository.deleteByEmail(email);
        return user;
    }

    @Cacheable(value = "userById", key = "#id")
    public User getUserById(Long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User by id: " + id + " not found")
        );
    }

    @Transactional
    public AuthToken updateUsername(String userEmail, String username) throws UsernameMatchesOldException, UserNotFoundException {
        User user = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new UserNotFoundException("User has been deleted")
        );
        if (!user.getUsername().equals(username)) {
            user.setUsername(username);
            userRepository.save(user);

            //Обновляем кеш
            Objects.requireNonNull(cacheManager.getCache("userById")).put(user.getId(), user);
            Objects.requireNonNull(cacheManager.getCache("userByEmail")).put(user.getEmail(), user);
            Objects.requireNonNull(cacheManager.getCache("userByUsername")).clear();

            return new AuthToken(jwtUtils.generateToken(jwtUtils.createGetCustomUserDetails(user)));
        } else {
            throw new UsernameMatchesOldException("New username must not match old name");
        }
    }

    @Transactional
    public void updatePassword(String userEmail, UpdatePassword password) throws PasswordMatchesOldException {
        User user = userRepository.findByEmail(userEmail).get();
        if (!passwordEncoder.matches(password.getPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password.getPassword()));
            generationAndSendingCodeService.generationAndSending(user);
        } else throw new PasswordMatchesOldException("New password must not match the old password");
    }

    public AuthToken loginUser(UserAuth userLog) throws BadCredentialsException {
        return new AuthToken(authService.createToken(userLog));
    }
}
