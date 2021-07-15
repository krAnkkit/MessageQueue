package com.navi.queue;

public interface MessageQueue {
    void publish(Message msg);
    void subscribe(String endpoint);
    void unsubscribe(String endpoint);
}
