package com.navi.queue;

import com.navi.exceptions.MessageException;
import com.navi.exceptions.SubscriptionException;

public interface MessageQueue {
    void publish(String msg) throws MessageException;
    void subscribe(String endpoint) throws SubscriptionException;
    void unsubscribe(String endpoint);
}
