package dev.nishants.appraisal.services;

import java.util.List;

import dev.nishants.appraisal.dtos.CreateUserRequest;
import dev.nishants.appraisal.dtos.UpdateUserRequest;
import dev.nishants.appraisal.dtos.UserResponse;

public interface UserService {

  UserResponse createUser(CreateUserRequest request);

  UserResponse getMe(Long userId);

  UserResponse getUserById(Long userId);

  List<UserResponse> getAllUsers();

  List<UserResponse> getTeamByManager(Long managerId);

  List<UserResponse> getMyTeam();

  UserResponse updateUser(Long userId, UpdateUserRequest request);

  void deleteUser(Long userId);
}
