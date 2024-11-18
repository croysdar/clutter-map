package app.cluttermap.exception;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import app.cluttermap.exception.auth.InvalidAuthenticationException;
import app.cluttermap.exception.auth.UserNotFoundException;
import app.cluttermap.exception.item.ItemLimitReachedException;
import app.cluttermap.exception.org_unit.OrgUnitLimitReachedException;
import app.cluttermap.exception.project.ProjectLimitReachedException;
import app.cluttermap.exception.room.RoomLimitReachedException;
import io.jsonwebtoken.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {
    // Logger supports different levels (INFO, DEBUG, ERROR, WARN)
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ValidationErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<IllegalArgumentResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        IllegalArgumentResponse errorResponse = new IllegalArgumentResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Access Denied");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        logger.info(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
    }

    @ExceptionHandler({ GeneralSecurityException.class, IOException.class })
    public ResponseEntity<String> handleGoogleAuthExceptions(Exception ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "An error occurred during token verification.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    @ExceptionHandler({ InvalidAuthenticationException.class })
    public ResponseEntity<Object> handleInvalidAuthenticationException(InvalidAuthenticationException exception) {
        return new ResponseEntity<>("Unauthorized access.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ UserNotFoundException.class })
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException exception) {
        return new ResponseEntity<>("Unauthorized access.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ ProjectLimitReachedException.class })
    public ResponseEntity<Object> handleProjectLimitReachedException(ProjectLimitReachedException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ ResourceNotFoundException.class })
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ RoomLimitReachedException.class })
    public ResponseEntity<Object> handleRoomLimitReachedException(RoomLimitReachedException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ OrgUnitLimitReachedException.class })
    public ResponseEntity<Object> handleOrgUnitLimitReachedException(OrgUnitLimitReachedException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ ItemLimitReachedException.class })
    public ResponseEntity<Object> handleItemLimitReachedException(ItemLimitReachedException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

// https://medium.com/@aedemirsen/spring-boot-global-exception-handler-842d7143cf2a