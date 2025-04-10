package com.function;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.InputStream;
import java.io.InputStreamReader;

public class GraphQLProvider {

    private final GraphQL graphQL;

    public GraphQLProvider() {
        try {
            InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("schema.graphqls");
            if (schemaStream == null) {
                throw new RuntimeException("No se encontró el archivo schema.graphqls en resources.");
            }

            InputStreamReader reader = new InputStreamReader(schemaStream);
            TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(reader);

            UsuarioDataFetcher usuarioDataFetcher = new UsuarioDataFetcher();

            RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                    .type("Query", typeWiring -> typeWiring
                            .dataFetcher("getUser", usuarioDataFetcher.getUser())
                            .dataFetcher("listUsers", usuarioDataFetcher.listUsers()))
                    .type("Mutation", typeWiring -> typeWiring
                            .dataFetcher("createUser", usuarioDataFetcher.createUser())
                            .dataFetcher("updateUser", usuarioDataFetcher.updateUser())
                            .dataFetcher("deleteUser", usuarioDataFetcher.deleteUser()))
                    .build();

            GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
            graphQL = GraphQL.newGraphQL(schema).build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar GraphQL: " + e.getMessage(), e);
        }
    }

    public GraphQL getGraphQL() {
        return graphQL;
    }
}
