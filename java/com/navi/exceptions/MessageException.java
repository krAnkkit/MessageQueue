package com.navi.exceptions;

public class MessageException extends Exception {
    public MessageException(final String msg) {
        super(msg);
    }

    public MessageException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
