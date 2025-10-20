package com.example.ATS_Plus.Service;

import com.example.ATS_Plus.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUserName(username);
        if (user == null || !user.isActivated()) {
            throw new UsernameNotFoundException("User not found or not activated: " + username);
        }

        String role = user.getRole() != null && !user.getRole().isEmpty() ? user.getRole() : "NORMAL";

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserName())
                .password("{noop}" + user.getPassword())  // {noop} tells Spring to use raw password
                .roles(role)
                .build();
    }
}
