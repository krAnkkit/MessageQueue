package com.navi.exceptions;

public class SubscriptionException extends Exception {
    public SubscriptionException(final String msg) {
        super(msg);
    }

    public SubscriptionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
