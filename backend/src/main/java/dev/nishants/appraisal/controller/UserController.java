package dev.nishants.appraisal.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.nishants.appraisal.dtos.ApiResponse;
import dev.nishants.appraisal.dtos.CreateUserRequest;
import dev.nishants.appraisal.dtos.UpdateUserRequest;
import dev.nishants.appraisal.dtos.UserResponse;
import dev.nishants.appraisal.services.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  // POST /api/users
  @PostMapping
  public ResponseEntity<ApiResponse<UserResponse>> createUser(
      @Valid @RequestBody CreateUserRequest request) {

    UserResponse response = userService.createUser(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("User created successfully", response));
  }

  // GET /api/users
  @GetMapping
  public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
    return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
  }

  // GET /api/users/me?userId=1
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> getMe(@RequestParam Long userId) {
    return ResponseEntity.ok(ApiResponse.success(userService.getMe(userId)));
  }

  // GET /api/users/team
  @GetMapping("/team")
  public ResponseEntity<ApiResponse<List<UserResponse>>> getMyTeam() {
    return ResponseEntity.ok(ApiResponse.success(userService.getMyTeam()));
  }

  // GET /api/users/{id}
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
  }

  // GET /api/users/manager/{managerId}/team
  @GetMapping("/manager/{managerId}/team")
  public ResponseEntity<ApiResponse<List<UserResponse>>> getTeam(
      @PathVariable Long managerId) {
    return ResponseEntity.ok(ApiResponse.success(userService.getTeamByManager(managerId)));
  }

  // PUT /api/users/{id}
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> updateUser(
      @PathVariable Long id,
      @RequestBody UpdateUserRequest request) {

    UserResponse response = userService.updateUser(id, request);
    return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
  }

  // DELETE /api/users/{id}
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
  }
}
