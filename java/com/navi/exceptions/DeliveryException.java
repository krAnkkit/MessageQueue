package com.navi.exceptions;

public class DeliveryException extends RuntimeException {

    public DeliveryException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
