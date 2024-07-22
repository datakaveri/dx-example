package com.example.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.json.JsonValue;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.common.models.response.HttpStatusCode.NOT_FOUND;

public class ElasticsearchServiceImpl implements ElasticsearchService{
    private static final Logger LOGGER = LogManager.getLogger(ElasticsearchServiceImpl.class);
    private final ElasticsearchClient client;

    public ElasticsearchServiceImpl(Vertx vertx, JsonObject config){
        CredentialsProvider credentials = new BasicCredentialsProvider();
        credentials.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(config.getString("databaseUser"), config.getString("databasePassword")));
        RestClient restClient = RestClient.builder(new HttpHost(config.getString("host"), 443, "https")).setHttpClientConfigCallback(
                httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentials)).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        this.client = new ElasticsearchClient(transport);
    }

    @Override
    public Future<JsonObject> search(String index, String deviceId) {
        Promise<JsonObject> promise = Promise.promise();
        Query query = new Query.Builder()
                .term(t -> t
                        .field("deviceId")
                        .value(deviceId)
                )
                .build();
        LOGGER.info("q: "+query);
        try {
            GetResponse<ObjectNode> response = client.get(g -> g
                    .index(index)
                    .id(deviceId), ObjectNode.class);
            // Log the raw response for debugging
            LOGGER.info("Search response: {}", response.toString());
            assert response.source() != null;
            if (!response.found()) {
                promise.fail(NOT_FOUND.getDescription());
            } else {
                JsonObject source = JsonObject.mapFrom(response.source());
                LOGGER.info("source: "+source);
                promise.complete(source);
            }
        } catch (Exception e) {
            LOGGER.info("Inside catch..");
            promise.fail(e);
        }
        return promise.future();
    }

    @Override
    public Future<Void> index(String index, String id, JsonObject document) {
        Promise<Void> promise = Promise.promise();
        try {
            IndexRequest<JsonData> request = IndexRequest.of(i -> i
                    .index(index)
                    .id(id)
                    .document(JsonData.of(document.getMap()))
            );
            LOGGER.info("indexReq: "+request);
            client.index(request);
            promise.complete();

        } catch (IOException | ElasticsearchException e) {
            promise.fail(e);
        }
        return promise.future();
    }
    @Override
    public Future<Void> update(String index, String id, JsonObject document) {
        Promise<Void> promise = Promise.promise();
        try {
            UpdateRequest<JsonData, JsonData> request = UpdateRequest.of(u -> u
                    .index(index)
                    .id(id)
                    .doc(JsonData.of(document.getMap()))
            );

            client.update(request, JsonData.class);
            promise.complete();

        } catch (IOException | ElasticsearchException e) {
            promise.fail(e);
        }
        return promise.future();
    }
    @Override
    public Future<Void> delete(String index, String id) {
        Promise<Void> promise = Promise.promise();
        try {
            DeleteRequest request = DeleteRequest.of(d -> d
                    .index(index)
                    .id(id)
            );

            client.delete(request);
            promise.complete();

        } catch (IOException | ElasticsearchException e) {
            promise.fail(e);
        }
        return promise.future();
    }
    @Override
    public Future<JsonObject> fetchAll(String index) {
        Promise<JsonObject> promise = Promise.promise();
        // Create a match_all query
        Query query = new Query.Builder()
                .matchAll(m -> m)
                .build();
        try {
            SearchRequest request = SearchRequest.of(s->s
                    .index(index));
            LOGGER.info("searchReq: "+request);
            SearchResponse<ObjectNode> response = client.search(request, ObjectNode.class);
            LOGGER.info("searchResp: "+response);
            if (response.hits().hits().isEmpty()) {
                promise.fail("No results found");
            } else {
                //LOGGER.info("source: "+response.hits().hits().get(0).source());
                JsonArray docs = new JsonArray();
                response.hits().hits().forEach(hit -> {
                    assert hit.source() != null;
                    docs.add(new JsonObject(hit.source().toString()));
                });
                promise.complete(new JsonObject().put("response", docs));
            }
        } catch (Exception e) {
            LOGGER.info("Inside catch..");
            promise.fail(e);
        }
        return promise.future();
    }
}
