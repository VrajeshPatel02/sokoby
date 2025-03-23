package com.sokoby.exception;

import com.sokoby.payload.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MerchantException.class)
    public ResponseEntity<ErrorResponse> handleMerchantException(MerchantException ex) {
        logger.error("Merchant error: {} (Code: {})", ex.getMessage(), ex.getErrorCode());
        ErrorResponse error = new ErrorResponse(ex.getMessage(), ex.getErrorCode());
        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        ErrorResponse error = new ErrorResponse("Internal server error", "SERVER_ERROR");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus determineHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "INVALID_EMAIL", "INVALID_NAME" -> HttpStatus.BAD_REQUEST;
            case "DUPLICATE_MERCHANT" -> HttpStatus.CONFLICT;
            case "MERCHANT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}


