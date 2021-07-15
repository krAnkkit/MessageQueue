package com.navi.queue;

import com.navi.exceptions.PersistenceException;
import com.navi.persistence.impl.DiskPersistence;
import com.navi.queue.impl.DefaultMessageQueueImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class QueueManager {

    private static final QueueManager INSTANCE = new QueueManager();

    private Path queueDir;
    private Map<String, MessageQueue> qMap;

    private QueueManager(){
        try {
            qMap = new HashMap<>();
            queueDir = Paths.get(System.getProperty("user.dir"), "NaviMessageQueue");
            if (!Files.exists(queueDir)) {
                queueDir = Files.createDirectories(queueDir);
            }
            for(File qDir: queueDir.toFile().listFiles()) {
                String qName = qDir.getName();
                MessageQueue mq = new DefaultMessageQueueImpl(qName, new DiskPersistence(qDir.toPath()));
                qMap.put(qName, mq);
            }
        } catch(IOException iox) {
            throw new PersistenceException("Error initializing storage for message queue", iox);
        }
    }

    public static MessageQueue get(String queueId) {
        try {
            MessageQueue mq = INSTANCE.qMap.get(queueId);
            if (mq == null) {
                mq = new DefaultMessageQueueImpl(queueId, new DiskPersistence(Files.createDirectory(Paths.get(INSTANCE.queueDir.toFile().getAbsolutePath(), queueId))));
            }
            return mq;
        } catch(IOException iox) {
            throw new PersistenceException("Error creating new queue", iox);
        }
    }
}
