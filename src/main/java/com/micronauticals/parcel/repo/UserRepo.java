package com.micronauticals.parcel.repo;

import com.micronauticals.parcel.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepo {
    private final DynamoDbTable<User> table;

    public UserRepo(DynamoDbEnhancedClient enhancedClient, @Value("${aws.dynamodb.table.name}") String tableName){
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(User.class));
    }

    public User save(User user) {
        user.setPk("USER#" + user.getId());
        user.setSk("PROFILE");
        user.setEntityType("User");
        table.putItem(user);
        return user;
    }

    public Optional<User> findByUsername(String username) {
        List<User> results = new ArrayList<>();
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .filterExpression(
                        Expression.builder()
                                .expression("username = :uname")
                                .putExpressionValue(":uname", AttributeValue.builder().s(username).build())
                                .build()
                ).build();

        table.scan(request).items().forEach(results::add);
        return results.stream().findFirst();
    }
}