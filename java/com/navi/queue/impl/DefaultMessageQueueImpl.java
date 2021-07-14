package com.navi.queue.impl;

import com.navi.queue.Message;
import com.navi.queue.MessageQueue;
import com.navi.queue.Subscription;
import com.navi.persistence.Persistence;
import com.navi.exceptions.PersistenceException;
import com.navi.persistence.impl.DiskPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DefaultMessageQueueImpl implements MessageQueue {
    private static final Logger LOGGER = Logger.getGlobal();

    private String qid;
    private Persistence pers;
    private List<Subscription> subscriptions;

    public DefaultMessageQueueImpl(final String queueId, final Persistence per) {
        qid = queueId;
        pers = per;
        subscriptions = pers.readSubscriptions();
        for(Message msg: pers.readMessages()) {
            send(msg);
        }
    }

    private void send(final Message msg) {
        ExecutorService exs = Executors.newFixedThreadPool(20);
        List<Callable<Void>> workers = new ArrayList<>();
        synchronized (subscriptions) {
            for (Subscription s : subscriptions) {
                workers.add(()->{
                    try {
                        // FIXME make network call to specified endpoint
                        System.out.println(s.getEndpoint()+" : "+msg.raw());
                        return null;
                    } finally {
//                        sem.release();
                    }
                });
            }
        }
        try {
            exs.invokeAll(workers);
            pers.deleteMessage(msg);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Error sending message to subscribers", e);
        }
    }

    @Override
    public String getId() {
        return qid;
    }

    @Override
    public void publish(final Message msg) {
        try {
            pers.writeMessage(msg);
            send(msg);
        } catch(PersistenceException pex) {
            LOGGER.log(Level.WARNING, "Error writing message to queue", pex);
        } finally {
            // Retry
        }
    }

    @Override
    public void subscribe(final String endpoint) {
        synchronized (subscriptions) {
            Subscription s = new Subscription(endpoint);
            subscriptions.add(s);
            pers.writeSubscriptions(subscriptions);
        }
    }

    @Override
    public void unsubscribe(final String endpoint) {
        synchronized (subscriptions) {
            subscriptions = subscriptions.stream().filter(s-> !s.getEndpoint().equals(endpoint)).collect(Collectors.toList());
            pers.writeSubscriptions(subscriptions);
        }
    }

}
