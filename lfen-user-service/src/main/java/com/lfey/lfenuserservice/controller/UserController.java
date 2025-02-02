package com.lfey.lfenuserservice.controller;

import com.lfey.lfenuserservice.entity.User;
import com.lfey.lfenuserservice.exception.DuplicateUserException;
import com.lfey.lfenuserservice.exception.UserNotFoundException;
import com.lfey.lfenuserservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Регистрирует нового пользователя.
     *
     * <p>Этот метод принимает объект {@link User} в теле запроса и регистрирует его в системе.
     * Если пользователь с таким email уже существует, выбрасывается исключение
     * {@link DuplicateUserException}, и метод возвращает ответ с кодом 400 (Bad Request)
     * и сообщением об ошибке.</p>
     *
     * <p>Пример запроса:</p>
     * <pre>{@code
     * POST /register
     * {
     *   "email": "user@example.com",
     *   "password": "securePassword123",
     *   "email": "user@example.com"
     * }
     * }</pre>
     *
     * <p>Пример успешного ответа:</p>
     * <pre>{@code
     * HTTP/1.1 201 Created
     * }</pre>
     *
     * <p>Пример ответа с ошибкой:</p>
     * <pre>{@code
     * HTTP/1.1 400 Bad Request
     * Пользователь с email user@example.com уже зарегистрирован.
     * }</pre>
     *
     * @param user Объект пользователя для регистрации. Не может быть {@code null}.
     * @return Ответ с кодом 201 (Created), если регистрация прошла успешно,
     *         или ответ с кодом 400 (Bad Request) и сообщением об ошибке, если пользователь уже существует.
     * @throws DuplicateUserException Если пользователь с таким email уже зарегистрирован.
     * @see User
     * @see DuplicateUserException
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (DuplicateUserException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<?> deleteUserByEmail(@PathVariable("email") String email) {
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
    public ResponseEntity<?> getUserById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
