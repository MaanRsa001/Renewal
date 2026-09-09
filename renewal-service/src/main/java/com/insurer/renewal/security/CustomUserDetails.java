package com.insurer.renewal.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wraps AppUser for Spring Security, mapping role codes to "ROLE_x" authorities.
 */
public class CustomUserDetails extends User {

    private final Long userId;

    public CustomUserDetails(AppUser appUser) {
        super(appUser.getUsername(), appUser.getPasswordHash(), appUser.isActive(),
                true, true, true, mapAuthorities(appUser.getRoles()));
        this.userId = appUser.getId();
    }

    public Long getUserId() {
        return userId;
    }

    private static Set<GrantedAuthority> mapAuthorities(Set<String> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toSet());
    }
}
