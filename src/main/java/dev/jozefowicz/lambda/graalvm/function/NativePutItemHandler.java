package dev.jozefowicz.lambda.graalvm.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;

public class NativePutItemHandler implements RequestHandler<Measurement, Void> {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");

    private final DynamoDbClient client = DynamoDbClient.builder().httpClientBuilder(UrlConnectionHttpClient.builder()).build();

    @Override
    public Void handleRequest(Measurement measurement, Context context) {
        if (isNull(measurement) || isNull(measurement.getSerialNumber())) {
            context.getLogger().log("Empty measurement");
        } else {
            final Map<String, AttributeValue> persistedMeasurement = Map.of(
                    "uuid", AttributeValue.builder().s(UUID.randomUUID().toString()).build(),
                    "serialNumber", AttributeValue.builder().n(measurement.getSerialNumber()).build(),
                    "temp", AttributeValue.builder().n(Double.toString(measurement.getTemp())).build()
            );
            client.putItem(
                    PutItemRequest.builder()
                            .item(persistedMeasurement)
                            .tableName(TABLE_NAME)
                            .build()
            );
        }
        return null;
    }

}
