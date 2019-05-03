package com.redhat.summit2019;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Date;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class TwitterVerticleTest{

    private Vertx vertx;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        vertx.exceptionHandler(context.exceptionHandler());
        vertx.deployVerticle(TwitterVerticle.class.getName(),
        context.asyncAssertSuccess());
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }
    @Test(timeout = 1000L)
    public void testSendingATweet(TestContext tc) {

        Async async = tc.async();

        String replyText = new StringBuilder()
        .append(" Verily, ye be a crook-pated, paunchy hedge-pig! ")
        .append(Date.from(Instant.now())).toString();
  
        JsonObject message = new JsonObject()
            .put(EventBusConstants.MESSAGE_KEY, new JsonObject()
            .put(EventBusConstants.ACTION, EventBusConstants.ACTIONS_STATUS_UPDATE)
            .put(EventBusConstants.PARAMETERS_STATUS, replyText));
            System.out.println("testSendingATweet: " + Json.encodePrettily(message));

              DeliveryOptions deliveryOptions = new DeliveryOptions()
                .setSendTimeout(600000);
              vertx.<JsonObject>eventBus().send( EventBusConstants.ADDRESS, message, deliveryOptions, ar -> {
                if (ar.succeeded()) {
                  System.out.println(ar.result().toString());
                  tc.assertNotNull(ar.result().body());
                  tc.assertTrue(ar.result().body().toString().contains(EventBusConstants.RESULT_SUCCESS));
                  async.complete();
                }else{
                    System.out.println(ar.result().toString());
                    tc.assertTrue(ar.succeeded());
                    async.complete();
                }
              });
        
        
          }
        

}