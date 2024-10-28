package app.cluttermap.exception;

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

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
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