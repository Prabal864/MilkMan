package com.micronauticals.parcel.repo;

import com.micronauticals.parcel.dto.PageResult;
import com.micronauticals.parcel.entity.DeliveryOrder;
import com.micronauticals.parcel.utility.DynamoDbPaginationUtil;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDate;
import java.util.*;

@Repository
public class DeliveryOrderRepo {
    private final DynamoDbTable<DeliveryOrder> table;
    private final DynamoDbPaginationUtil dynamoDbPaginationUtil;

    public DeliveryOrderRepo(DynamoDbEnhancedClient enhancedClient, DynamoDbPaginationUtil dynamoDbPaginationUtil) {
        this.table = enhancedClient.table("TechEazy_Backend", TableSchema.fromBean(DeliveryOrder.class));
        this.dynamoDbPaginationUtil = dynamoDbPaginationUtil;
    }

    public DeliveryOrder save(DeliveryOrder order) {
        order.setPk("VENDOR#" + order.getVendorId());
        order.setSk("ORDER#" + order.getId());
        order.setEntityType("DeliveryOrder");
        table.putItem(order);
        return order;
    }

    public PageResult<DeliveryOrder> findByDeliveryDate(LocalDate deliveryDate, Integer limit, String startKey) {
        List<DeliveryOrder> results = new ArrayList<>();
        Map<String, AttributeValue> exclusiveStartKey = dynamoDbPaginationUtil.decodeStartKey(startKey);

        Expression filter = Expression.builder()
                .expression("deliveryDate = :dd")
                .putExpressionValue(":dd", AttributeValue.builder().s(deliveryDate.toString()).build())
                .build();

        ScanEnhancedRequest.Builder reqBuilder = ScanEnhancedRequest.builder()
                .filterExpression(filter);
        if (limit != null) reqBuilder.limit(limit);

        Map<String, AttributeValue> lastEvaluatedKey = exclusiveStartKey;
        Page<DeliveryOrder> page = null;

        do {
            if (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty()) {
                reqBuilder.exclusiveStartKey(lastEvaluatedKey);
            } else {
                reqBuilder.exclusiveStartKey(null);
            }

            ScanEnhancedRequest request = reqBuilder.build();
            Iterator<Page<DeliveryOrder>> iter = table.scan(request).iterator();
            page = iter.hasNext() ? iter.next() : null;

            if (page != null) {
                results.addAll(page.items());
                lastEvaluatedKey = page.lastEvaluatedKey();
            } else {
                lastEvaluatedKey = null;
            }
        } while (results.isEmpty() && lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty());

        PageResult<DeliveryOrder> result = new PageResult<>();
        result.setItems(results);
        result.setLastEvaluatedKey(lastEvaluatedKey);

        result.setNextStartKey(dynamoDbPaginationUtil.encodeStartKey(lastEvaluatedKey));

        return result;
    }

    public PageResult<DeliveryOrder> findByVendorNameAndDeliveryDate(String vendorName, LocalDate deliveryDate, Integer limit, String startKey) {
        List<DeliveryOrder> matchedItems = new ArrayList<>();
        Map<String, AttributeValue> lastEvaluatedKey = dynamoDbPaginationUtil.decodeStartKey(startKey);

        Expression filter = Expression.builder()
                .expression("vendorName = :vn AND deliveryDate = :dd")
                .putExpressionValue(":vn", AttributeValue.builder().s(vendorName).build())
                .putExpressionValue(":dd", AttributeValue.builder().s(deliveryDate.toString()).build())
                .build();

        boolean done = false;

        while (!done && matchedItems.size() < limit) {
            ScanEnhancedRequest.Builder reqBuilder = ScanEnhancedRequest.builder()
                    .limit(100)
                    .filterExpression(filter);

            if (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty()) {
                reqBuilder.exclusiveStartKey(lastEvaluatedKey);
            }

            ScanEnhancedRequest request = reqBuilder.build();
            Iterator<Page<DeliveryOrder>> iter = table.scan(request).iterator();
            if (iter.hasNext()) {
                Page<DeliveryOrder> page = iter.next();
                matchedItems.addAll(page.items());

                lastEvaluatedKey = page.lastEvaluatedKey();
                if (lastEvaluatedKey == null || page.items().isEmpty()) {
                    done = true;
                }
            } else {
                done = true;
            }
        }

        List<DeliveryOrder> finalItems = matchedItems.size() > limit ? matchedItems.subList(0, limit) : matchedItems;

        PageResult<DeliveryOrder> result = new PageResult<>();
        result.setItems(finalItems);
        result.setLastEvaluatedKey(lastEvaluatedKey);
        result.setNextStartKey(dynamoDbPaginationUtil.encodeStartKey(lastEvaluatedKey));

        return result;
    }

}
