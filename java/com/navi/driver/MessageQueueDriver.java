package com.navi.driver;

import com.navi.exceptions.MessageException;
import com.navi.exceptions.SubscriptionException;
import com.navi.queue.MessageQueue;
import com.navi.queue.QueueManager;

public class MessageQueueDriver {

    public static void main(String[] args) throws SubscriptionException, MessageException {
        MessageQueue q = QueueManager.get("q8");
        q.subscribe("http://amazon.com");
        q.publish("{'a':'a'}");
        q.subscribe("http://facebook.com");
        q.unsubscribe("abc");
        q.publish("{'b':'b'}");
        q.unsubscribe("http://amazon.com");
        q.publish("{'d':'d'}");
    }
}
