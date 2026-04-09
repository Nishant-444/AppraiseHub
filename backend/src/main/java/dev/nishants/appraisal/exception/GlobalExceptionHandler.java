package dev.nishants.appraisal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.nishants.appraisal.dtos.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 404 — resource not found
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ex.getMessage()));
  }

  // 403 — wrong user trying to access something
  @ExceptionHandler(UnauthorizedAccessException.class)
  public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedAccessException ex) {
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error(ex.getMessage()));
  }

  // 409 — duplicate appraisal, duplicate goal, etc.
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException ex) {
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ApiResponse.error(ex.getMessage()));
  }

  // 400 — wrong status transition (e.g. acknowledge before approve)
  @ExceptionHandler(InvalidStatusTransitionException.class)
  public ResponseEntity<ApiResponse<Void>> handleInvalidTransition(InvalidStatusTransitionException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ex.getMessage()));
  }

  // 400 — @Valid validation failures on request DTOs
  // Returns a map of { fieldName: "error message" } so frontend knows exactly
  // what's wrong
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
      MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, "Validation failed", errors));
  }

  // 500 — anything else we didn't specifically handle
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("Something went wrong: " + ex.getMessage()));
  }
}
