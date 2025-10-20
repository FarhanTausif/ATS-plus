package com.example.ATS_Plus.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import com.example.ATS_Plus.Service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/",
                                "/api/upload/cv",
                                "/api/upload/job-requirement/list",
                                "/result",                  // Allow access to result page
                                "/css/**",                  // Allow access to static resources
                                "/js/**",
                                "/images/**").permitAll()// Allow CV uploads and get job requirement for everyone
                        .anyRequest().hasRole("HR"))         // Require HR role for all other endpoints
                .httpBasic()                            // Enable HTTP Basic authentication
                .and()
                .csrf(AbstractHttpConfigurer::disable);  // Disable CSRF for simplicity

        return http.build();
        //hello world!!!!
    }

}