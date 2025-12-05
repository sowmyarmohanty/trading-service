package com.trading.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
public class DatabaseConfig {

    // Disabled custom initializer since Spring Boot's built-in R2DBC initializer
    // handles schema.sql and data.sql automatically when spring.sql.init.mode=always
    
    // @Bean
    // public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
    //     ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    //     initializer.setConnectionFactory(connectionFactory);

    //     ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    //     populator.addScript(new ClassPathResource("schema.sql"));
    //     populator.addScript(new ClassPathResource("data.sql"));

    //     initializer.setDatabasePopulator(populator);

    //     return initializer;
    // }
}
