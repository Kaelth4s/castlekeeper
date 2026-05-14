package org.kaelth4s.castlekeeper.server.exception;

import lombok.extern.slf4j.Slf4j;
import org.kaelth4s.castlekeeper.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return new ApiError(
                404,
                "Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", message);
        return new ApiError(
                400,
                "Validation Failed",
                message,
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMalformedJson(HttpMessageNotReadableException ex, WebRequest request) {
        String detail = ex.getMessage();
        if (detail != null && detail.length() > 200) {
            detail = detail.substring(0, 200);
        }
        log.warn("Malformed JSON: {}", detail);
        return new ApiError(
                400,
                "Bad Request",
                "Malformed JSON body: " + detail,
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return new ApiError(
                400,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneral(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        return new ApiError(
                500,
                "Internal Server Error",
                "An unexpected error occurred",
                request.getDescription(false).replace("uri=", "")
        );
    }
}
