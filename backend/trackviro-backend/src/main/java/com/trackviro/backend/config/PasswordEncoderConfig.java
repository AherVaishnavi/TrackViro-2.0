package com.trackviro.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * NOT full security configuration — Step 4 explicitly excludes
 * JWT/security, and there is no SecurityFilterChain, no
 * authentication, no endpoint rules here.
 *
 * This is the one bean the service layer cannot compile-run without:
 * UserServiceImpl.registerUser() and both of ProfileServiceImpl's
 * password-changing methods @Autowired a PasswordEncoder. Spring
 * @Service beans are singletons created eagerly at application
 * startup, not lazily on first use — so without a bean definition
 * somewhere, the application context fails to start the moment those
 * two services are instantiated, even though the code compiles fine.
 *
 * BCryptPasswordEncoder matches exactly what the old
 * Corporate_Expense_Tracker project used, so if you ever import user
 * data from expense_db into trackviro, those existing password hashes
 * would still verify correctly against this encoder.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
