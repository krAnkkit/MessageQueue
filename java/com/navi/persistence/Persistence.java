package com.navi.persistence;

import com.navi.queue.Message;
import com.navi.queue.Subscription;

import java.util.List;

public interface Persistence {

    void writeMessage(Message msg);
    void deleteMessage(Message msg);
    List<Message> readMessages();
    void writeSubscriptions(List<Subscription> subs);
    List<Subscription> readSubscriptions();
}
