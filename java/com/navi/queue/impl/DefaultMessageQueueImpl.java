package com.navi.queue.impl;

import com.navi.exceptions.MessageException;
import com.navi.exceptions.SubscriptionException;
import com.navi.queue.Message;
import com.navi.queue.MessageQueue;
import com.navi.queue.Subscription;
import com.navi.persistence.Persistence;
import com.navi.exceptions.PersistenceException;
import com.navi.retry.Retry;
import com.navi.retry.RetryCallable;
import com.navi.utils.Utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DefaultMessageQueueImpl implements MessageQueue {

    private final Persistence pers;

    private final Set<Subscription> subscriptions;

    private final ExecutorService exs = Executors.newFixedThreadPool(20);

    public DefaultMessageQueueImpl(final Persistence per) {
        pers = per;
        subscriptions = Collections.synchronizedSet(new HashSet<>());
        subscriptions.addAll(pers.readSubscriptions());
        for (Message msg : pers.readMessages()) {
            send(msg, new ArrayList<>(subscriptions));
        }
    }

    private void send(final Message msg, Collection<Subscription> subs) {
        new Thread(() -> {
            List<Callable<Void>> workers = new ArrayList<>();
            for (Subscription s : subs) {
                workers.add(new RetryCallable<>(() -> {
                    s.deliver(msg);
                    return null;
                }, new Retry.ExponentialBackoff(3, 2)));
            }
            try {
                // We call invokeAny to make sure atleast one subscriber received the message before cleaning up the
                // message from the queue. If none receive the message, we do not cleanup.
                exs.invokeAny(workers);
                pers.deleteMessage(msg);
            } catch (InterruptedException | ExecutionException e) {
                Utils.logger.log(Level.WARNING, "Error sending message to subscribers", e);
            }
        }).start();
    }

    @Override
    public void publish(final String str) throws MessageException {
        try {
            Message msg = Message.create(str);
            pers.writeMessage(msg);
            send(msg, new ArrayList<>(subscriptions));
        } catch (PersistenceException pex) {
            Utils.logger.log(Level.WARNING, "Error writing message to queue", pex);
            throw new MessageException("Error writing message to queue", pex);
        }
    }

    @Override
    public synchronized void subscribe(final String endpoint) throws SubscriptionException {
        try {
            URL url = new URL(endpoint);
            Subscription s = new Subscription(endpoint);
            subscriptions.add(s);
            pers.writeSubscriptions(subscriptions);
        } catch (MalformedURLException ex) {
            throw new SubscriptionException("Invalid endpoint url for subscription");
        }

    }

    @Override
    public synchronized void unsubscribe(final String endpoint) {
        List<Subscription> updated = subscriptions.stream().filter(s -> !s.getEndpoint().equals(endpoint)).collect(Collectors.toList());
        pers.writeSubscriptions(updated);
        subscriptions.clear();
        subscriptions.addAll(updated);
    }

}
