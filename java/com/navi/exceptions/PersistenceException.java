package com.navi.exceptions;

public class PersistenceException extends RuntimeException {

    public PersistenceException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
