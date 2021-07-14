package com.navi.queue;

import com.navi.queue.Retry;

public class Subscription {

    private String endpoint;

    public Subscription(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

}
