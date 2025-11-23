package com.trading.repository;

import com.trading.model.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    
    Mono<User> findByUsername(String username);
    
    Mono<User> findByEmail(String email);
    
    @Query("SELECT * FROM users WHERE username = :username OR email = :email")
    Mono<User> findByUsernameOrEmail(String username, String email);
}
