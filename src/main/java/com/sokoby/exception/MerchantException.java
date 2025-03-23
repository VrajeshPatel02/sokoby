package com.sokoby.exception;

import lombok.Getter;

@Getter
public class MerchantException extends RuntimeException {
    private final String errorCode;

    public MerchantException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}
