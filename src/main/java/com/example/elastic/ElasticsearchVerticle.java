package com.example.elastic;

import com.example.elastic.service.ElasticsearchService;
import com.example.elastic.service.ElasticsearchServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ElasticsearchVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(ElasticsearchVerticle.class);
    private ElasticsearchService elasticsearchService;
    private ServiceBinder binder;
    private MessageConsumer<JsonObject> consumer;

    @Override
    public void start(Promise<Void> startPromise){
        String ELASTICSEARCH_HOST = "744f49daa6ea4812aaca3e7378f24a23.us-central1.gcp.cloud.es.io";
        String ELASTICSEARCH_USERNAME = "provide_valid_username";
        String ELASTICSEARCH_PASSWORD = "XXXXX";
        JsonObject config = new JsonObject()
                .put("host", ELASTICSEARCH_HOST)
                .put("databaseUser", ELASTICSEARCH_USERNAME)
                .put("databasePassword", ELASTICSEARCH_PASSWORD);
        elasticsearchService = new ElasticsearchServiceImpl(vertx, config);
        binder = new ServiceBinder(vertx);
        consumer = binder.setAddress("elastic.service").register(ElasticsearchService.class, elasticsearchService);

        if(consumer!=null){
            LOGGER.info("Successfully registered ElasticsearchService at address 'elastic.service'");
            startPromise.complete();
        } else {
            LOGGER.error("Failed to register ElasticsearchService at address 'elastic.service'");
            startPromise.fail("Failed to register ElasticsearchService");
        }
    }
    @Override
    public void stop(){
        binder.unregister(consumer);
    }
}
