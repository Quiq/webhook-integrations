package com.centricient.sample.api;

import com.fasterxml.jackson.databind.JsonNode;


public class EventPayload {
    private String id;
    private String eventType;
    private JsonNode data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }
}