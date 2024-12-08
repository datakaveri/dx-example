package com.example.immudb.services;

import com.example.common.models.Query;
import io.codenotary.immudb4j.ImmuClient;
import io.codenotary.immudb4j.sql.SQLValue;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class ImmudbServiceImpl implements ImmudbService {
  private final ImmuClient client;

  public ImmudbServiceImpl(Vertx vertx, ImmuClient immuClient) {
    this.client = immuClient;
  }

  @Override
  public Future<JsonObject> executeQuery(Query query) {
    try {
      client.createDatabase("db1");
      client.openSession("db1", "immudb", "immudb");
       SQLValue sqlValue = new SQLValue(100);
      /*client.sqlExec(query.getSql(),sqlValue);*/
      /*var result = client.sqlQuery("select * from db1",sqlValue);*/
      /*System.out.println(result);*/
    } catch (Exception e) {
      System.out.println(e);
    }
    Promise<JsonObject> promise = Promise.promise();

    return promise.future();
  }

  @Override
  public Future<Void> executeUpdate(Query query) {
    Promise<Void> promise = Promise.promise();

    return promise.future();
  }
}
