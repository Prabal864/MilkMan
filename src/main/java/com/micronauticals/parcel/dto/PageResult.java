package com.micronauticals.parcel.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

public class PageResult<T> {
    private List<T> items;
    @JsonIgnore
    private Map<String, AttributeValue> lastEvaluatedKey;
    private String nextStartKey; // base64 encoded

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public Map<String, AttributeValue> getLastEvaluatedKey() {
        return lastEvaluatedKey;
    }

    public void setLastEvaluatedKey(Map<String, AttributeValue> lastEvaluatedKey) {
        this.lastEvaluatedKey = lastEvaluatedKey;
    }

    public String getNextStartKey() {
        return nextStartKey;
    }

    public void setNextStartKey(String nextStartKey) {
        this.nextStartKey = nextStartKey;
    }
}