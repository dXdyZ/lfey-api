package com.lfey.lfenuserservice.controller;

import com.lfey.lfenuserservice.dto.UserAuth;
import com.lfey.lfenuserservice.dto.UserRegister;
import com.lfey.lfenuserservice.entity.User;
import com.lfey.lfenuserservice.exception.DuplicateUserException;
import com.lfey.lfenuserservice.exception.UserNotFoundException;
import com.lfey.lfenuserservice.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegister user) {
        try {
            userService.registerUser(user);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (DuplicateUserException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<?> deleteUserByEmail(@PathVariable("email")
                                               @Parameter(description = "Электронная почта", example = "user@user.com") String email) {
        try {
            userService.deleteUserByEmail(email);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserByUsername(@RequestParam(name = "username") String username) {
        try {
            return ResponseEntity.ok(userService.getUsersByUsername(username));
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id")
                                         @Parameter(description = "Id пользователя", example = "1") Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/confirm/{email}/{code}")
    public ResponseEntity<?> confirmCode(@PathVariable("email")
                                         @Parameter(description = "Mail пользователя который подтверждает действие", example = "user@user.com") String email,
                                         @PathVariable("code") @Parameter(description = "Код отправленный на почту", example = "123456") String code) {
        try {
            return ResponseEntity.ok(userService.confirmCode(email, code));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserAuth userLog) {
        try {
            return ResponseEntity.ok(userService.loginUser(userLog));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}
