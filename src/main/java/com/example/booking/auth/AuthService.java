package com.example.booking.auth;

import com.example.booking.auth.dto.JwtAuthResponse;
import com.example.booking.auth.dto.LoginDto;
import com.example.booking.auth.dto.RegisterDto;
import com.example.booking.security.JwtTokenProvider;
import com.example.booking.user.Role;
import com.example.booking.user.User;
import com.example.booking.user.UserMapper;
import com.example.booking.user.UserRepository;
import com.example.booking.user.dto.UserResponseDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    public AuthService(AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
    }

    public JwtAuthResponse login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponseDto userDto = userMapper.toResponseDto(user);

        return new JwtAuthResponse(token, userDto);
    }

    public UserResponseDto register(RegisterDto registerDto) {
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }

        User user = new User();
        user.setFullName(registerDto.getFullName());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Set<Role> roles = new HashSet<>();
        if (registerDto.getRoles() != null) {
            registerDto.getRoles().forEach(role -> {
                try {
                    roles.add(Role.valueOf(role));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid roles
                }
            });
        }
        if (roles.isEmpty()) {
            roles.add(Role.ROLE_CUSTOMER);
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }
}
