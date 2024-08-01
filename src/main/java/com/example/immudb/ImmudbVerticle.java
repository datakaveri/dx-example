package com.example.immudb;

import com.example.immudb.services.ImmudbService;
import com.example.immudb.services.ImmudbServiceImpl;
import io.codenotary.immudb4j.ImmuClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImmudbVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbVerticle.class);
  private MessageConsumer<JsonObject> consumer;
  private ServiceBinder binder;

  @Override
  public void start(Promise<Void> startPromise) {

    try {

      /*JsonObject config =
      new JsonObject()
          .put("url", "jdbc:immudb://localhost:3322/defaultdb")
          .put("driver_class", "org.immutab.jdbc.Driver")
          .put("user", "user")
          .put("password", "password");*/

      ImmuClient immuClient =
          ImmuClient.newBuilder().withServerUrl("localhost").withServerPort(3322).build();

      /*immuClient.login("immudb", "immudb");
      immuClient.useDatabase("mydatabase");*/
      LOGGER.debug("connected to immudb");
      /* ImmudbService service = new ImmudbServiceImpl(vertx, config);*/
      ImmudbService service = new ImmudbServiceImpl(vertx, immuClient);
      binder = new ServiceBinder(vertx);
      consumer = binder.setAddress("immudb.service").register(ImmudbService.class, service);
      LOGGER.info("Immudb verticle started.");
      startPromise.complete();
    } catch (Exception e) {
      LOGGER.error("Failed to connect to immudb", e);
      startPromise.fail(e);
    }
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
