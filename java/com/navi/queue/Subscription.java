package com.navi.queue;

import com.navi.exceptions.DeliveryException;

public class Subscription {

    private String endpoint;

    public Subscription(final String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void deliver(final Message msg) {
        try {
            System.out.println(endpoint+" : "+msg.raw());
        } catch (Exception dex) {
            throw new DeliveryException("Error delivering message to subscription", dex);
        }
    }

}
