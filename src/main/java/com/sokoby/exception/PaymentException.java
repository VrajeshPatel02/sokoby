package com.sokoby.exception;

public class PaymentException extends RuntimeException {
  private String errorCode;
    public PaymentException(String message, String errorCode){
        super(message);
        this.errorCode = errorCode;
    }
}
