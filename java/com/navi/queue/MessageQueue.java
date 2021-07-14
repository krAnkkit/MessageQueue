package com.navi.queue;

public interface MessageQueue {
    String getId();
    void publish(Message msg);
    void subscribe(String endpoint);
    void unsubscribe(String endpoint);
}
