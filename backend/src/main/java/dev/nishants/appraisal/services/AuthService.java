package dev.nishants.appraisal.services;

import dev.nishants.appraisal.dtos.AuthResponse;
import dev.nishants.appraisal.dtos.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse getCurrentUser(String email);
}
