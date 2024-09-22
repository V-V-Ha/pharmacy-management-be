package com.fu.pha.exception;
import com.fu.pha.dto.response.ServiceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseException.class)
    public ResponseEntity<ServiceResponse<Object>> handleResponseException(ResponseException ex) {
        ServiceResponse<Object> response = new ServiceResponse<>(
                ex.getStatus().value(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, ex.getStatus());
    }
}
