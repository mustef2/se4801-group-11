package com.sustainabilitytracker.security;

import com.sustainabilitytracker.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!user.getIsActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new User(
                user.getEmail(),
                user.getPassword(),
                List.of(authority)
        );
    }
}
