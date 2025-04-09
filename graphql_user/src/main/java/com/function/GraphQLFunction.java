package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

import java.util.Optional;
import java.util.Map;

public class GraphQLFunction {

    private static final GraphQL graphql = new GraphQLProvider().getGraphQL();

    @FunctionName("GraphQLHandler")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "graphql"
            )
            HttpRequestMessage<Optional<Map<String, Object>>> request,
            final ExecutionContext context
    ) {
        try {
            Map<String, Object> body = request.getBody().orElseThrow(() -> new IllegalArgumentException("Missing body"));

            String query = (String) body.get("query");
            Map<String, Object> variables = (Map<String, Object>) body.getOrDefault("variables", null);

            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                    .query(query)
                    .variables(variables != null ? variables : Map.of())
                    .build();

            ExecutionResult result = graphql.execute(executionInput);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(result.toSpecification())
                    .build();

        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()))
                    .build();
        }
    }
}
