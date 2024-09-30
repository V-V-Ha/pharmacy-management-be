package com.fu.pha.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import com.fu.pha.dto.response.MessageResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage resourceNotFoundException(ResourceNotFoundException ex , WebRequest request) {
        return new ErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                toExactlyPath(request.getDescription(false)));
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage badRequestException(BadRequestException ex , WebRequest request) {
        return new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                toExactlyPath(request.getDescription(false)));
    }


    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<MessageResponse> handleBadCredentialsException(BadCredentialsException ex) {
        MessageResponse errorResponse = new MessageResponse(Message.INVALID_USERNAME, HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LockedException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public ResponseEntity<MessageResponse> handleLockedException(LockedException ex) {
        MessageResponse errorResponse = new MessageResponse(Message.ACCOUNT_LOCKED, HttpStatus.LOCKED.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.LOCKED);
    }

    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage handleDisabledException(DisabledException ex,WebRequest request) {
        return new ErrorMessage(
                HttpStatus.FORBIDDEN.value(),
                Message.ACCOUNT_DISABLED,
                toExactlyPath(request.getDescription(false)));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<MessageResponse> handleAccessDeniedException(AccessDeniedException ex) {
        MessageResponse errorResponse = new MessageResponse(Message.ACCESS_DENIED, HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<MessageResponse> handleGenericException(Exception ex) {
        MessageResponse errorResponse = new MessageResponse(Message.OTHER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String toExactlyPath(String input) {
        return input.replace("uri=", "");
    }


}



