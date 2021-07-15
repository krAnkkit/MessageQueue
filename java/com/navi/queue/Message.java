package com.navi.queue;

import com.google.gson.*;
import com.navi.exceptions.MessageException;
import com.navi.utils.Utils;

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

    public static Message create(String json) throws MessageException {
        try {
            JsonElement jem = JsonParser.parseString(json);
            if (jem.isJsonObject()) {
                return new Message(json);
            } else {
                throw new MessageException("Invalid message body. Only json messages supported : " + json);
            }
        } catch (JsonSyntaxException e) {
            throw new MessageException("Invalid message body. Only json messages supported", e);
        }
    }

}
