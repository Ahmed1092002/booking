package com.example.booking.security;

import com.example.booking.exception.UnauthorizedException;
import com.example.booking.user.Role;
import com.example.booking.user.User;
import com.example.booking.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the full User entity for the currently authenticated user
     * 
     * @return User entity
     * @throws UnauthorizedException if no user is authenticated
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new UnauthorizedException("Invalid authentication principal");
        }

        String email = ((UserDetails) principal).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found in database"));
    }

    /**
     * Returns the email of the currently authenticated user
     */
    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * Returns the ID of the currently authenticated user
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Returns the roles of the currently authenticated user
     */
    public Set<Role> getCurrentUserRoles() {
        return getCurrentUser().getRoles();
    }

    /**
     * Checks if the current user has a specific role
     */
    public boolean hasRole(Role role) {
        Set<Role> roles = getCurrentUserRoles();
        return roles != null && roles.contains(role);
    }
}
