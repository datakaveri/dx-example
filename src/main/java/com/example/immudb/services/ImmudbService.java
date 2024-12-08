package com.example.immudb.services;

import com.example.common.models.Query;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface ImmudbService {
    
    Future<JsonObject> executeQuery(Query query);
    
    
    Future<Void> executeUpdate(Query query);

    static ImmudbService createProxy(Vertx vertx, String address) {
        return new ImmudbServiceVertxEBProxy(vertx, address);
    }
}
