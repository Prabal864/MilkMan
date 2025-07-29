package com.micronauticals.parcel.repo;

import com.micronauticals.parcel.dto.PageResult;
import com.micronauticals.parcel.entity.Parcel;
import com.micronauticals.parcel.utility.DynamoDbPaginationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;

@Repository
public class ParcelRepo {
    private final DynamoDbPaginationUtil dynamoDbPaginationUtil;
    private final DynamoDbTable<Parcel> table;

    public ParcelRepo(DynamoDbPaginationUtil dynamoDbPaginationUtil, DynamoDbEnhancedClient enhancedClient,@Value("${aws.dynamodb.table.name}") String tableName) {
        this.dynamoDbPaginationUtil = dynamoDbPaginationUtil;
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(Parcel.class));
    }

    public Parcel save(Parcel parcel) {
        parcel.setPk("ORDER#" + parcel.getOrderId());
        parcel.setSk("PARCEL#" + parcel.getTrackingId());
        parcel.setEntityType("Parcel");
        table.putItem(parcel);
        return parcel;
    }

    public Optional<Parcel> findByTrackingId(String trackingId) {
        String trackingIdStr = String.valueOf(trackingId);
        List<Parcel> results = new ArrayList<>();
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .filterExpression(
                        Expression.builder()
                                .expression("trackingId = :tid")
                                .putExpressionValue(":tid", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(trackingIdStr).build())
                                .build()
                ).build();

        table.scan(request).items().forEach(results::add);
        return results.stream().findFirst();
    }

    public PageResult<Parcel> findAllPaginated(Integer limit, String startKey) {
        DynamoDbIndex<Parcel> entityTypeIndex = table.index("entityType-index");
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue("Parcel").build());

        QueryEnhancedRequest.Builder reqBuilder = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional);
        if (limit != null) reqBuilder.limit(limit);

        Map<String, AttributeValue> exclusiveStartKey = dynamoDbPaginationUtil.decodeStartKey(startKey);
        if (exclusiveStartKey != null && !exclusiveStartKey.isEmpty()) {
            reqBuilder.exclusiveStartKey(exclusiveStartKey);
        }

        QueryEnhancedRequest request = reqBuilder.build();
        PageIterable<Parcel> pages = (PageIterable<Parcel>) entityTypeIndex.query(request);

        Iterator<Page<Parcel>> iter = pages.iterator();
        Page<Parcel> page = iter.hasNext() ? iter.next() : null;
        Map<String, AttributeValue> nextKey = (page != null) ? page.lastEvaluatedKey() : null;

        List<Parcel> results = (page != null) ? new ArrayList<>(page.items()) : new ArrayList<>();

        PageResult<Parcel> result = new PageResult<>();
        result.setItems(results);
        result.setLastEvaluatedKey(nextKey);
        result.setNextStartKey(dynamoDbPaginationUtil.encodeStartKey(nextKey));
        return result;
    }
}