package com.micronauticals.parcel.service;

import com.micronauticals.parcel.entity.User;
import com.micronauticals.parcel.enums.Role;
import com.micronauticals.parcel.repo.UserRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(authority)
        );
    }

    public boolean registerUser(String username, String rawPassword) {
        Optional<User> existingUser = userRepo.findByUsername(username);
        if (existingUser.isPresent()) {
            return false;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setRole(Role.VENDOR);

        userRepo.save(newUser);
        return true;
    }

    @PostConstruct
    public void createAdminIfNotExist() {
        if (userRepo.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepo.save(admin);
            System.out.println("Admin created");
        }
    }
}
