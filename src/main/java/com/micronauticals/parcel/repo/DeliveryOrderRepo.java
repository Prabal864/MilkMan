package com.micronauticals.parcel.repo;

import com.micronauticals.parcel.dto.PageResult;
import com.micronauticals.parcel.entity.DeliveryOrder;
import com.micronauticals.parcel.utility.DynamoDbPaginationUtil;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${aws.dynamodb.table.name}")
    private String tableName;

    public DeliveryOrderRepo(DynamoDbEnhancedClient enhancedClient, DynamoDbPaginationUtil dynamoDbPaginationUtil) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(DeliveryOrder.class));
        this.dynamoDbPaginationUtil = dynamoDbPaginationUtil;
    }

    public DeliveryOrder save(DeliveryOrder order) {
        order.setPk("VENDOR#" + order.getVendorId());
        order.setSk("ORDER#" + order.getId());
        order.setEntityType("DeliveryOrder");
        table.putItem(order);
        return order;
    }


    public PageResult<DeliveryOrder> findByVendorNameAndDeliveryDate(
            String vendorName, LocalDate deliveryDate, Integer limit, String startKey) {

        DynamoDbIndex<DeliveryOrder> dateIndex = table.index("deliveryDate-vendorName-index");

        QueryConditional queryConditional;
        if (vendorName != null && !vendorName.isEmpty()) {
            // Query for a specific vendor on a specific date
            queryConditional = QueryConditional.keyEqualTo(
                    Key.builder()
                            .partitionValue(deliveryDate.toString())
                            .sortValue(vendorName)
                            .build()
            );
        } else {
            // Query for all vendors on a specific date
            queryConditional = QueryConditional.keyEqualTo(
                    Key.builder()
                            .partitionValue(deliveryDate.toString())
                            .build()
            );
        }

        QueryEnhancedRequest.Builder reqBuilder = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional);
        if (limit != null) reqBuilder.limit(limit);

        Map<String, AttributeValue> exclusiveStartKey = dynamoDbPaginationUtil.decodeStartKey(startKey);
        if (exclusiveStartKey != null && !exclusiveStartKey.isEmpty()) {
            reqBuilder.exclusiveStartKey(exclusiveStartKey);
        }

        QueryEnhancedRequest request = reqBuilder.build();
        PageIterable<DeliveryOrder> pages = (PageIterable<DeliveryOrder>) dateIndex.query(request);

        Iterator<Page<DeliveryOrder>> iter = pages.iterator();
        Page<DeliveryOrder> page = iter.hasNext() ? iter.next() : null;
        Map<String, AttributeValue> nextKey = (page != null) ? page.lastEvaluatedKey() : null;

        List<DeliveryOrder> results = (page != null) ? new ArrayList<>(page.items()) : new ArrayList<>();

        PageResult<DeliveryOrder> result = new PageResult<>();
        result.setItems(results);
        result.setLastEvaluatedKey(nextKey);
        result.setNextStartKey(dynamoDbPaginationUtil.encodeStartKey(nextKey));

        return result;
    }

}
