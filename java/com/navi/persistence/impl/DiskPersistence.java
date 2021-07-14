package com.navi.persistence.impl;

import com.navi.queue.Message;
import com.navi.persistence.Persistence;
import com.navi.exceptions.PersistenceException;
import com.navi.queue.Subscription;
import com.navi.utils.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DiskPersistence implements Persistence {

    private static final Logger LOGGER = Logger.getGlobal();

    private Path queueDir;

    public DiskPersistence(final Path queueDir) {
        this.queueDir = queueDir;
        try {
            Path subsPath = Paths.get(queueDir.toAbsolutePath().toString(), "subscriptions");
            if(!subsPath.toFile().exists()) {
                Files.createFile(subsPath);
            }
        } catch(IOException iox) {
            throw new PersistenceException("Error persisting queue data");
        }
    }

    @Override
    public void writeMessage(Message msg) {
        try {
            Path msgPath = Files.createFile(Paths.get(queueDir.toFile().getAbsolutePath(), msg.getId()));
            String payload = JsonUtils.gson.toJson(msg);
            Files.writeString(msgPath, payload, StandardOpenOption.WRITE);
        } catch(IOException iox) {
            throw new PersistenceException("Error writing message to queue");
        }
    }

    @Override
    public void deleteMessage(Message msg) {
        try {
            Files.deleteIfExists(Paths.get(queueDir.toFile().getAbsolutePath(), msg.getId()));
        } catch (IOException e) {
            throw new PersistenceException("Error cleaning up old message from queue");
        }
    }

    @Override
    public List<Message> readMessages() {
        return Arrays.stream(Objects.requireNonNull(Paths.get(queueDir.toFile().getAbsolutePath()).toFile().listFiles())).filter(f -> !f.getName().equals("subscriptions")).map(f -> {
            try {
                return Files.readString(f.toPath());
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error reading contents of "+f.getAbsolutePath(), e);
            }
            return null;
        }).filter(Objects::nonNull).map(s -> JsonUtils.gson.fromJson(s, Message.class)).collect(Collectors.toList());
    }

    @Override
    public void writeSubscriptions(List<Subscription> subs) {
        try {
            Path subscriptionsPath = Paths.get(queueDir.toAbsolutePath().toString(), "subscriptions");
            for(Subscription s: subs) {
                String subscription = JsonUtils.gson.toJson(s)+"\n";
                Files.writeString(subscriptionsPath, subscription, StandardOpenOption.WRITE);
            }
        } catch(IOException iox) {
            throw new PersistenceException("Error writing subscriptions");
        }
    }

    @Override
    public List<Subscription> readSubscriptions() {
        try {
            Path subscriptionsPath = Paths.get(queueDir.toAbsolutePath().toString(), "subscriptions");
            return Files.readAllLines(subscriptionsPath).stream().map(l -> JsonUtils.gson.fromJson(l, Subscription.class)).collect(Collectors.toList());
        } catch(IOException iox) {
            throw new PersistenceException("Error reading subscriptions");
        }
    }
}
