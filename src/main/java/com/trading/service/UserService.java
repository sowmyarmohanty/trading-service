package com.trading.service;

import com.trading.model.User;
import com.trading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> registerUser(String username, String email, String password) {
        log.debug("Registering new user: {}", username);

        return userRepository.findByUsernameOrEmail(username, email)
                .flatMap(existingUser -> Mono.<User>error(
                        new IllegalArgumentException("Username or email already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    User newUser = new User(username, email, passwordEncoder.encode(password));
                    return userRepository.save(newUser);
                }));
    }

    public Mono<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Mono<Boolean> validateCredentials(String username, String password) {
        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .defaultIfEmpty(false);
    }
}
