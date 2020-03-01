package dev.jozefowicz.lambda.graalvm.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;
import java.util.UUID;

public class NativePutItemHandler implements RequestHandler<Object, Void> {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");

    private final DynamoDbClient client = DynamoDbClient.builder().httpClientBuilder(UrlConnectionHttpClient.builder()).build();

    @Override
    public Void handleRequest(Object event, Context context) {
        client.putItem(
                PutItemRequest.builder()
                        .item(Map.of("uuid", AttributeValue.builder().s(UUID.randomUUID().toString()).build()))
                        .tableName(TABLE_NAME)
                        .build()
        );
        return null;
    }

}
