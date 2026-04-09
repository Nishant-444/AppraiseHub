package dev.nishants.appraisal.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nishants.appraisal.dtos.AuthResponse;
import dev.nishants.appraisal.dtos.LoginRequest;
import dev.nishants.appraisal.entity.User;
import dev.nishants.appraisal.exception.ResourceNotFoundException;
import dev.nishants.appraisal.repository.UserRepository;
import dev.nishants.appraisal.security.JwtUtil;
import dev.nishants.appraisal.services.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException if wrong password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmailWithDetails(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .jobTitle(user.getJobTitle())
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .managerId(user.getManager() != null ? user.getManager().getId() : null)
                .managerName(user.getManager() != null ? user.getManager().getFullName() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailWithDetails(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .jobTitle(user.getJobTitle())
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .managerId(user.getManager() != null ? user.getManager().getId() : null)
                .managerName(user.getManager() != null ? user.getManager().getFullName() : null)
                .build();
    }
}
