package app.cluttermap.exception;

import java.security.GeneralSecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import app.cluttermap.exception.auth.InvalidAuthenticationException;
import app.cluttermap.exception.auth.UserNotFoundException;
import app.cluttermap.exception.org_unit.OrgUnitNotFoundException;
import app.cluttermap.exception.project.ProjectLimitReachedException;
import app.cluttermap.exception.project.ProjectNotFoundException;
import app.cluttermap.exception.room.RoomNotFoundException;
import io.jsonwebtoken.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {
    // Logger supports different levels (INFO, DEBUG, ERROR, WARN)
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
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

    @ExceptionHandler({ ProjectNotFoundException.class })
    public ResponseEntity<Object> handleProjectNotFoundException(ProjectNotFoundException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ RoomNotFoundException.class })
    public ResponseEntity<Object> handleRoomNotFoundException(RoomNotFoundException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ OrgUnitNotFoundException.class })
    public ResponseEntity<Object> handleOrgUnitNotFoundException(OrgUnitNotFoundException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }
}

// https://medium.com/@aedemirsen/spring-boot-global-exception-handler-842d7143cf2a