package com.example.elastic.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface ElasticsearchService {
    static ElasticsearchService createProxy(Vertx vertx, String address) {
        return new ElasticsearchServiceVertxEBProxy(vertx, address);
    }

    Future<JsonObject> search(String index, String deviceId);
    Future<JsonObject> fetchAll(String index);
    Future<Void> index(String index, String id, JsonObject document);
    Future<Void> update(String index, String id, JsonObject document);
    Future<Void> delete(String index, String id);
}
