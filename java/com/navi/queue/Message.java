package com.navi.queue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.navi.exceptions.MessageException;
import com.navi.utils.JsonUtils;

public class Message {

    private Message(String json) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.json = json;
    }

    private final String id;
    private final String json;

    public String getId() {
        // TODO use better identification for individual message in case of concurrent multiple publishers
        return id;
    }

    public String raw() {
        return json;
    }

    public static Message create(String json) {
        try {
            Object o = JsonUtils.gson.fromJson(json, Object.class);
            if (o!=null) {
                return new Message(json);
            } else {
                throw new MessageException("Invalid message body. Only json messages supported");
            }
        } catch(Exception e) {
            throw new MessageException("Invalid message body. Only json messages supported", e);
        }
    }

}
