package com.navi.persistence.impl;

import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.navi.queue.Message;
import com.navi.persistence.Persistence;
import com.navi.exceptions.PersistenceException;
import com.navi.queue.Subscription;
import com.navi.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DiskPersistence implements Persistence {

    private final Path queueDir;

    public DiskPersistence(final Path queueDir) {
        this.queueDir = queueDir;
        try {
            Path subsPath = Paths.get(queueDir.toAbsolutePath().toString(), "subscriptions");
            if(!subsPath.toFile().exists()) {
                Files.createFile(subsPath);
            }
        } catch(IOException iox) {
            throw new PersistenceException("Error persisting queue data", iox);
        }
    }

    @Override
    public void writeMessage(Message msg) {
        try {
            Path msgPath = Files.createFile(Paths.get(queueDir.toFile().getAbsolutePath(), msg.getId()));
            String payload = Utils.gson.toJson(msg);
            Files.writeString(msgPath, payload, StandardOpenOption.WRITE);
        } catch(IOException iox) {
            throw new PersistenceException("Error writing message to queue", iox);
        }
    }

    @Override
    public void deleteMessage(Message msg) {
        try {
            Files.deleteIfExists(Paths.get(queueDir.toFile().getAbsolutePath(), msg.getId()));
        } catch (IOException e) {
            throw new PersistenceException("Error cleaning up old message from queue", e);
        }
    }

    @Override
    public List<Message> readMessages() {
        return Arrays.stream(Objects.requireNonNull(Paths.get(queueDir.toFile().getAbsolutePath()).toFile().listFiles())).filter(f -> !f.getName().equals("subscriptions")).map(f -> {
            try {
                return Files.readString(f.toPath());
            } catch (IOException e) {
                Utils.logger.log(Level.WARNING, "Error reading contents of "+f.getAbsolutePath(), e);
            }
            return null;
        }).filter(Objects::nonNull).map(s -> Utils.gson.fromJson(s, Message.class)).collect(Collectors.toList());
    }

    @Override
    public void writeSubscriptions(Collection<Subscription> subs) {
        try {
            Path subscriptionsPath = Paths.get(queueDir.toAbsolutePath().toString(), "subscriptions");
            for(Subscription s: subs) {
                String subscription = Utils.gson.toJson(subs);
                Files.writeString(subscriptionsPath, subscription, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch(IOException iox) {
            throw new PersistenceException("Error writing subscriptions", iox);
        }
    }

    @Override
    public List<Subscription> readSubscriptions() {
        try {
            Path subscriptionsPath = Paths.get(queueDir.toAbsolutePath().toString(), "subscriptions");
            String str = Files.readString(subscriptionsPath);
            TypeToken<List<Subscription>> tt = new TypeToken<>() {};
            List<Subscription> subs = Utils.gson.fromJson(str, tt.getType());
            return subs != null ? subs : Collections.emptyList();
       } catch(IOException iox) {
            throw new PersistenceException("Error reading subscriptions", iox);
        }
    }
}
