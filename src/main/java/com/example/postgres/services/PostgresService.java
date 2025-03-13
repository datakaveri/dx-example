package com.example.postgres.services;

import com.example.postgres.models.*;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface PostgresService {

    Future<QueryResult> insert(InsertQuery query);
    Future<QueryResult> update(UpdateQuery query);
    Future<QueryResult> search(SelectQuery query, int limit, int offset);
    Future<QueryResult> delete(DeleteQuery query);

    static PostgresService createProxy(Vertx vertx, String address) {
        return new PostgresServiceVertxEBProxy(vertx, address);
    }
}
