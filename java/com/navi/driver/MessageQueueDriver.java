package com.navi.driver;

import com.navi.queue.Message;
import com.navi.queue.MessageQueue;
import com.navi.queue.QueueManager;

public class MessageQueueDriver {

    public static void main(String[] args) {
        MessageQueue q = QueueManager.get("ank2");
        q.subscribe("abc");
        q.subscribe("bcd");
        q.publish(Message.create("{'a':'a'}"));
        q.unsubscribe("abc");
        q.publish(Message.create("{'b':'b'}"));
        q.subscribe("dabc");
        q.publish(Message.create("{'c':'c'}"));
        return;
    }
}
