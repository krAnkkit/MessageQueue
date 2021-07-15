package com.navi.queue.impl;

import com.navi.queue.Message;
import com.navi.queue.MessageQueue;
import com.navi.queue.Subscription;
import com.navi.persistence.Persistence;
import com.navi.exceptions.PersistenceException;
import com.navi.retry.Retry;
import com.navi.retry.RetryCallable;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DefaultMessageQueueImpl implements MessageQueue {
    private static final Logger LOGGER = Logger.getGlobal();

    private final String qid;
    private final Persistence pers;

    // We use vector here for thread-safety
    private final Vector<Subscription> subscriptions;

    public DefaultMessageQueueImpl(final String queueId, final Persistence per) {
        qid = queueId;
        pers = per;
        subscriptions = new Vector<>();
        subscriptions.addAll(pers.readSubscriptions());
        for (Message msg : pers.readMessages()) {
            send(msg);
        }
    }

    private void send(final Message msg) {
        ExecutorService exs = Executors.newFixedThreadPool(20);
        List<Callable<Void>> workers = new ArrayList<>();
        synchronized (subscriptions) {
            for (Subscription s : subscriptions) {
                workers.add(new RetryCallable<>(() -> {
                    s.deliver(msg);
                    return null;
                }, new Retry.ExponentialBackoff(3, 2)));
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
    public void publish(final Message msg) {
        try {
            pers.writeMessage(msg);
            send(msg);
        } catch (PersistenceException pex) {
            LOGGER.log(Level.WARNING, "Error writing message to queue", pex);
        }
    }

    @Override
    public synchronized void subscribe(final String endpoint) {
        Subscription s = new Subscription(endpoint);
        subscriptions.add(s);
        pers.writeSubscriptions(subscriptions);
    }

    @Override
    public synchronized void unsubscribe(final String endpoint) {
        List<Subscription> updated = subscriptions.stream().filter(s -> !s.getEndpoint().equals(endpoint)).collect(Collectors.toList());
        pers.writeSubscriptions(updated);
        subscriptions.clear();
        subscriptions.addAll(updated);
    }

}
