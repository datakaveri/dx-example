// package com.example.api;

// import com.example.postgres.PostgresVerticle;
// import io.vertx.core.AbstractVerticle;
// import io.vertx.core.Vertx;
// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;

// public class MainVerticle extends AbstractVerticle {
//     private static final Logger LOGGER = LogManager.getLogger(MainVerticle.class);

//     public static void main(String[] args) {
//         Vertx vertx = Vertx.vertx();
//         vertx.deployVerticle(new MainVerticle());
//     }

//     @Override
//     public void start() {
//         // Deploy PostgresVerticle
//         vertx.deployVerticle(new PostgresVerticle(), res -> {
//             if (res.succeeded()) {
//                 LOGGER.info("PostgresVerticle deployed successfully");
//             } else {
//                 LOGGER.error("Failed to deploy PostgresVerticle: " + res.cause().getMessage());
//             }
//         });
//     }
// }


package com.example.api;
import io.vertx.core.Promise;
import com.example.postgres.PostgresVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.vertx.spi.cluster.infinispan.InfinispanClusterManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(MainVerticle.class);

    public static void main(String[] args) {


        
    }

    @Override
    public void start(Promise<Void> startPromise) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
            .host(System.getenv("INFINISPAN_HOST"))
            .port(Integer.parseInt(System.getenv("INFINISPAN_PORT")))
            .security()
            .authentication()
            .username(System.getenv("INFINISPAN_USER"))
            .password(System.getenv("INFINISPAN_PASS"));

        InfinispanClusterManager clusterManager = new InfinispanClusterManager(builder.build());

        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);



        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                // Deploy PostgresVerticle
                vertx.deployVerticle(new PostgresVerticle(), newres -> {
                    if (newres.succeeded()) {
                        LOGGER.info("PostgresVerticle deployed successfully");
                        LOGGER.info("Deployment id is: " + newres.result());
                        startPromise.complete();
                        LOGGER.info(vertx.eventBus());
                    } else {
                        LOGGER.error("Failed to deploy PostgresVerticle: " + newres.cause().getMessage());
                    }
                });
                
            } else {
                LOGGER.error("Cluster up failed: " + res.cause());
            }
        });
        
    }
}
