package com.navi.exceptions;

public class MessageException extends RuntimeException {
    public MessageException(final String msg) {
        super(msg);
    }

    public MessageException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
