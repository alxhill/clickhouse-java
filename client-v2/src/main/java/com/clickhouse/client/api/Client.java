package com.clickhouse.client.api;

import com.clickhouse.client.*;
import com.clickhouse.client.api.internal.SettingsConverter;
import com.clickhouse.client.api.internal.ValidationUtils;
import com.clickhouse.data.ClickHouseColumn;

import java.io.InputStream;
import java.net.SocketException;
import java.util.*;
import com.clickhouse.client.api.metadata.TableSchema;
import com.clickhouse.client.api.internal.TableSchemaParser;
import com.clickhouse.client.api.query.QueryResponse;
import com.clickhouse.client.api.query.QuerySettings;
import com.clickhouse.data.ClickHouseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.helpers.BasicMDCAdapter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Client {
    public static final int TIMEOUT = 30_000;
    private Set<String> endpoints;
    private Map<String, String> configuration;
    private List<ClickHouseNode> serverNodes = new ArrayList<>();
    private static final  Logger LOG = LoggerFactory.getLogger(Client.class);

    private Client(Set<String> endpoints, Map<String,String> configuration) {
        this.endpoints = endpoints;
        this.configuration = configuration;
        this.endpoints.forEach(endpoint -> {
            this.serverNodes.add(ClickHouseNode.of(endpoint, this.configuration));
        });
    }

    public static class Builder {
        private Set<String> endpoints;
        private Map<String, String> configuration;

        public Builder() {
            this.endpoints = new HashSet<>();
            this.configuration = new HashMap<String, String>();
        }

        public Builder addEndpoint(String endpoint) {
            // TODO: validate endpoint
            this.endpoints.add(endpoint);
            return this;
        }

        public Builder addEndpoint(Protocol protocol, String host, int port) {
            String endpoint = String.format("%s://%s:%d", protocol.toString().toLowerCase(), host, port);
            this.addEndpoint(endpoint);
            return this;
        }

        public Builder addConfiguration(String key, String value) {
            this.configuration.put(key, value);
            return this;
        }

        public Builder addUsername(String username) {
            this.configuration.put("user", username);
            return this;
        }

        public Builder addPassword(String password) {
            this.configuration.put("password", password);
            return this;
        }

        public Client build() {
            // check if endpoint are empty. so can not initiate client
            if (this.endpoints.isEmpty()) {
                throw new IllegalArgumentException("At least one endpoint is required");
            }
            // check if username and password are empty. so can not initiate client?
            return new Client(this.endpoints, this.configuration);
        }
    }

    private ClickHouseNode getServerNode() {
        // TODO: implement load balancing using existing logic
        return this.serverNodes.get(0);
    }

    /**
     * Ping the server to check if it is alive
     * @return true if the server is alive, false otherwise
     */
    public boolean ping() {
        return ping(Client.TIMEOUT);
    }

    /**
     * Ping the server to check if it is alive
     * @param timeout timeout in milliseconds
     * @return true if the server is alive, false otherwise
     */
    public boolean ping(int timeout) {
        ClickHouseClient clientPing = ClickHouseClient.newInstance(ClickHouseProtocol.HTTP);
        return clientPing.ping(getServerNode(), timeout);
    }

    /**
     * Register the POJO
     */
    public void register(Class<?> clazz, TableSchema schema) {
        //This is just a placeholder
        //Create a new POJOSerializer with static .serialize(object, columns) methods
    }

    /**
     * Insert data into ClickHouse using a POJO
     */
    public Future<InsertResponse> insert(String tableName,
                                     List<Object> data,
                                     InsertSettings settings,
                                     List<ClickHouseColumn> columns) throws ClickHouseException, SocketException {
        //Lookup the Serializer for the POJO
        //Call the static .serialize method on the POJOSerializer for each object in the list
        return null;//This is just a placeholder
    }

    /**
     * Insert data into ClickHouse using a binary stream
     */
    public Future<InsertResponse> insert(String tableName,
                                     InputStream data,
                                     InsertSettings settings) throws ClickHouseException, SocketException {
        return null;//This is just a placeholder
    }


    /**
     * Sends data query to the server and returns a reference to a result descriptor.
     * Control is returned when server accepted the query and started processing it.
     * <br/>
     * The caller should use {@link ClickHouseParameterizedQuery} to render the `sqlQuery` with parameters.
     *
     *
     * @param sqlQuery - complete SQL query.
     * @param settings
     * @return
     */
    public Future<QueryResponse> query(String sqlQuery, Map<String, Object> qparams, QuerySettings settings) {
        ClickHouseClient client = createClient();
        ClickHouseRequest<?> request = client.read(getServerNode());
        request.settings(SettingsConverter.toRequestSettings(settings.getAllSettings()));
        request.query(sqlQuery, settings.getQueryID());
        request.format(ClickHouseFormat.valueOf(settings.getFormat()));
        if (qparams != null && !qparams.isEmpty()) {
            request.params(qparams);
        }
        MDC.put("queryId", settings.getQueryID());
        LOG.debug("Executing request: {}", request);
        return CompletableFuture.completedFuture(new QueryResponse(client, request.execute()));
    }

    public TableSchema getTableSchema(String table, String database) {
        try (ClickHouseClient clientQuery = createClient()) {
            ClickHouseRequest request = clientQuery.read(getServerNode());
            // XML - because java has a built-in XML parser. Will consider CSV later.
            request.query("DESCRIBE TABLE " + table + " FORMAT " + ClickHouseFormat.TSKV.name());
            TableSchema tableSchema = new TableSchema();
            try {
                return new TableSchemaParser().createFromBinaryResponse(clientQuery.execute(request).get(), table, database);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get table schema", e);
            }
        }
    }


    private ClickHouseClient createClient() {
        ClickHouseConfig clientConfig = new ClickHouseConfig();
        return ClickHouseClient.builder().config(clientConfig)
                .nodeSelector(ClickHouseNodeSelector.of(ClickHouseProtocol.HTTP))
                .build();
    }

    private static final Set<String> COMPRESS_ALGORITHMS = ValidationUtils.whiteList("LZ4", "LZ4HC", "ZSTD", "ZSTDHC", "NONE");

    public static Set<String> getCompressAlgorithms() {
        return COMPRESS_ALGORITHMS;
    }

    private static final Set<String> OUTPUT_FORMATS = ValidationUtils.whiteList("Native", "JSON", "JSONCompact",
            "TabSeparated", "TabSeparatedRaw", "TabSeparatedWithNames", "TabSeparatedWithNamesAndTypes", "Pretty",
            "PrettyCompact", "PrettyCompactMonoBlock", "PrettyNoEscapes", "PrettySpace", "PrettySpaceNoEscapes",
            "PrettyCompactSpace", "PrettyCompactSpaceNoEscapes", "PrettyNoEscapesAndQuoting",
            "PrettySpaceNoEscapesAndQuoting", "PrettyCompactNoEscapesAndQuoting",
            "PrettyCompactSpaceNoEscapesAndQuoting", "PrettyNoEscapesAndQuotingMonoBlock",
            "PrettySpaceNoEscapesAndQuotingMonoBlock", "PrettyCompactNoEscapesAndQuotingMonoBlock",
            "PrettyCompactSpaceNoEscapesAndQuotingMonoBlock", "PrettyNoEscapesAndQuotingSpace",
            "PrettySpaceNoEscapesAndQuotingSpace", "PrettyCompactNoEscapesAndQuotingSpace",
            "PrettyCompactSpaceNoEscapesAndQuotingSpace", "PrettyNoEscapesAndQuotingSpaceMonoBlock",
            "PrettySpaceNoEscapesAndQuotingSpaceMonoBlock", "PrettyCompactNoEscapesAndQuotingSpaceMonoBlock",
            "PrettyCompactSpaceNoEscapesAndQuotingSpaceMonoBlock", "PrettyNoEscapesAndQuotingSpaceWithEscapes",
            "PrettySpaceNoEscapesAndQuotingSpaceWithEscapes", "PrettyCompactNoEscapesAndQuotingSpaceWithEscapes",
            "PrettyCompactSpaceNoEscapesAndQuotingSpaceWithEscapes",
            "PrettyNoEscapesAndQuotingSpaceWithEscapesMonoBlock",
            "PrettySpaceNoEscapesAndQuotingSpaceWithEscapesMonoBlock",
            "PrettyCompactNoEscapesAndQuotingSpaceWithEscapesMonoBlock",
            "PrettyCompactSpaceNoEscapesAndQuotingSpaceWithEscapesMonoBlock",
            "PrettyNoEscapesAndQuotingSpaceWithEscapesAndNulls",
            "PrettySpaceNoEscapesAndQuotingSpaceWithEscapesAndNulls",
            "PrettyCompactNoEscapesAndQuotingSpaceWithEscapesAndNulls",
            "PrettyCompactSpaceNoEscapesAndQuotingSpaceWithEscapesAndNulls",
            "PrettyNoEscapesAndQuotingSpaceWithEscapesAndNullsMonoBlock",
            "PrettySpaceNoEscapesAndQuotingSpaceWithEscapesAndNullsMonoBlock",
            "PrettyCompactNoEscapesAndQuotingSpaceWithEscapesAndNull");

    public static Set<String> getOutputFormats() {
        return OUTPUT_FORMATS;
    }
}
