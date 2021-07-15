package com.navi.queue;

import com.navi.exceptions.DeliveryException;
import com.navi.utils.Utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Stream;

public class Subscription {

    private final String endpoint;

    public Subscription(final String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void deliver(final Message msg) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder().uri(new URI(getEndpoint())).POST(HttpRequest.BodyPublishers.ofString(msg.raw())).build();
            HttpResponse<Stream<String>> resp = client.send(req, HttpResponse.BodyHandlers.ofLines());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                Utils.logger.log(Level.INFO, String.format("Delivered message %s to subscription %s", msg.getId(), endpoint));
            } else {
                throw new DeliveryException(String.format("Error delivering message %s to %s", msg.getId(), endpoint));
            }
        } catch (IOException | InterruptedException | URISyntaxException dex) {
            throw new DeliveryException(String.format("Error delivering message %s to %s", msg.getId(), endpoint), dex);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint);
    }
}
