package com.micronauticals.parcel.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class DynamoDbPaginationUtil {

    public String encodeStartKey(Map<String, AttributeValue> key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> simpleMap = attributeValueMapToSimpleMap(key);
            String json = mapper.writeValueAsString(simpleMap);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public Map<String, AttributeValue> decodeStartKey(String startKey) {
        if (startKey == null || startKey.isEmpty()){
            System.out.println("decodeStartKey: Received null or empty startKey");
            return null;}
        try {
            String json = new String(Base64.getDecoder().decode(startKey), StandardCharsets.UTF_8);
            System.out.println("decodeStartKey: Decoded JSON: " + json);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> simpleMap = mapper.readValue(json, new TypeReference<>() {});
            System.out.println("decodeStartKey: Converted back to AttributeValue map: " + simpleMap);
            return simpleMapToAttributeValueMap(simpleMap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> attributeValueMapToSimpleMap(Map<String, AttributeValue> attributeValueMap) {
        Map<String, Object> simpleMap = new HashMap<>();
        for (Map.Entry<String, AttributeValue> entry : attributeValueMap.entrySet()) {
            AttributeValue val = entry.getValue();
            if (val.s() != null) {
                simpleMap.put(entry.getKey(), val.s());
            } else if (val.n() != null) {
                simpleMap.put(entry.getKey(), val.n());
            } else if (val.bool() != null) {
                simpleMap.put(entry.getKey(), val.bool());
            } else if (val.ss() != null) {
                simpleMap.put(entry.getKey(), val.ss());
            } else if (val.ns() != null) {
                simpleMap.put(entry.getKey(), val.ns());
            } else {
                simpleMap.put(entry.getKey(), null);
            }
        }
        return simpleMap;
    }

    public Map<String, AttributeValue> simpleMapToAttributeValueMap(Map<String, Object> simpleMap) {
        Map<String, AttributeValue> attributeValueMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : simpleMap.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String) {
                attributeValueMap.put(entry.getKey(), AttributeValue.builder().s((String) val).build());
            } else if (val instanceof Boolean) {
                attributeValueMap.put(entry.getKey(), AttributeValue.builder().bool((Boolean) val).build());
            } else if (val instanceof Number) {
                attributeValueMap.put(entry.getKey(), AttributeValue.builder().n(val.toString()).build());
            } else if (val instanceof Collection) {
                Collection<?> col = (Collection<?>) val;
                if (!col.isEmpty() && col.iterator().next() instanceof String) {
                    attributeValueMap.put(entry.getKey(), AttributeValue.builder().ss((Collection<String>) col).build());
                } else if (!col.isEmpty() && col.iterator().next() instanceof Number) {
                    List<String> ns = new ArrayList<>();
                    for (Object o : col) ns.add(o.toString());
                    attributeValueMap.put(entry.getKey(), AttributeValue.builder().ns(ns).build());
                }
            } else {
                attributeValueMap.put(entry.getKey(), AttributeValue.builder().nul(true).build());
            }
        }
        return attributeValueMap;
    }

}
