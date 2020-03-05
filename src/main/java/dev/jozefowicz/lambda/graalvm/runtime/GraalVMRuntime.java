package dev.jozefowicz.lambda.graalvm.runtime;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.TimeZone;

public class GraalVMRuntime {

    private static final String AWS_LAMBDA_RUNTIME_API = System.getenv("AWS_LAMBDA_RUNTIME_API");
    private static final String LAMBDA_NEXT_INVOCATION_ENDPOINT = "http://" + AWS_LAMBDA_RUNTIME_API + "/2018-06-01/runtime/invocation/next";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final RequestHandler requestHandler;
    private final Class<?> eventClass;

    public GraalVMRuntime(RequestHandler requestHandler, Class<?> eventClass) {
        this.requestHandler = requestHandler;
        this.eventClass = eventClass;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        this.httpClient = HttpClient.newHttpClient();
    }

    public void execute() {
        System.out.println("Bootstrap time: " + ManagementFactory.getRuntimeMXBean().getUptime());
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        while (true) {
            try {
                final var startTimestamp = System.currentTimeMillis();
                final HttpRequest newInvocationRequest = HttpRequest.newBuilder().uri(new URI(LAMBDA_NEXT_INVOCATION_ENDPOINT)).timeout(Duration.ofSeconds(2)).build();
                final HttpResponse<String> invocationRequest = httpClient.send(newInvocationRequest, HttpResponse.BodyHandlers.ofString());
                var invocationID = invocationRequest.headers().map().get("Lambda-Runtime-Aws-Request-Id").get(0);
                final Object input = this.objectMapper.readValue(invocationRequest.body(), eventClass);

                final Object result = requestHandler.handleRequest(input, new NoOpContext());

                final var invocationResultEndpoint = "http://" + AWS_LAMBDA_RUNTIME_API + "/2018-06-01/runtime/invocation/" + invocationID + "/response";
                final HttpRequest invocationResultRequest = HttpRequest
                        .newBuilder()
                        .uri(new URI(invocationResultEndpoint))
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(result)))
                        .build();
                final HttpResponse<String> invocationResultResponse = httpClient.send(invocationResultRequest, HttpResponse.BodyHandlers.ofString());

                System.out.println("Lambda result endpoint response: " + invocationResultResponse.body());
                System.out.println("Execution time: " + (System.currentTimeMillis() - startTimestamp));
            } catch (Exception e) {
                System.out.println(String.format("Exception while executing handler logic: %s", e.getLocalizedMessage()));
                e.printStackTrace();
            }
        }
    }

}
