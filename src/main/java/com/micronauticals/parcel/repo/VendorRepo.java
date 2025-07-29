package com.micronauticals.parcel.repo;

import com.micronauticals.parcel.entity.Vendor;
import com.micronauticals.parcel.enums.SubscriptionType;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VendorRepo {
    private final DynamoDbTable<Vendor> table;

    public VendorRepo(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("TechEazy_Backend", TableSchema.fromBean(Vendor.class));
    }

    public Vendor save(Vendor vendor) {
        vendor.setPk("VENDOR#" + vendor.getId());
        vendor.setSk("PROFILE");
        vendor.setEntityType("Vendor");
        vendor.setVendorName(vendor.getVendorName());
        vendor.setSubscriptionType(SubscriptionType.FREE);
        table.putItem(vendor);
        return vendor;
    }

    public Vendor findByName(String name) {
        List<Vendor> results = new ArrayList<>();
        Expression filter = Expression.builder()
                .expression("entityType = :etype AND vendorName = :vname")
                .putExpressionValue(":etype", AttributeValue.builder().s("Vendor").build())
                .putExpressionValue(":vname", AttributeValue.builder().s(name).build())
                .build();

        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .filterExpression(filter)
                .build();

        table.scan(request).items().forEach(results::add);
        return results.stream().findFirst().orElse(null);
    }

}