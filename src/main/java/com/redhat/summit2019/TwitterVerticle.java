package com.redhat.summit2019;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterVerticle extends AbstractVerticle {

  Twitter twitter;

  @Override
  public void start(Future<Void> startFuture) {

    twitter = TwitterFactory.getSingleton();
    EventBus eventBus = vertx.eventBus();
    MessageConsumer<JsonObject> consumer = eventBus.consumer(EventBusConstants.ADDRESS);

    consumer.handler(message -> {

      String action = message.body().getJsonObject(EventBusConstants.MESSAGE_KEY).getString(EventBusConstants.ACTION);

      switch (action) {
        case EventBusConstants.ACTIONS_STATUS_UPDATE:
          tweet(message);
          break;
        default:
          message.fail(EventBusConstants.EventBusErrors.UNKNOWN_ADDRESS.errorCode, EventBusConstants.EventBusErrors.UNKNOWN_ADDRESS.errorMessage);
      }
    });

    startFuture.complete();

  }

  private void tweet(Message<JsonObject> message) {
    System.out.println("TwitterVerticle.tweet: " + message.body());
    JsonObject replyJson = message.body().getJsonObject(EventBusConstants.MESSAGE_KEY);

    StatusUpdate statusUpdate = new StatusUpdate(replyJson.getString(EventBusConstants.PARAMETERS_STATUS));

    System.out.println(statusUpdate.toString());

    vertx.executeBlocking((Future<Object> future) -> {
      try {
        Status status = twitter.updateStatus(statusUpdate);
        System.out.println(status.toString());
        future.complete();
      } catch (TwitterException e) {
        e.printStackTrace();
        future.complete(e.getMessage());
      }
    }, res -> {
      if (res.succeeded()) {
        message.reply(new JsonObject().put("outcome", EventBusConstants.RESULT_SUCCESS));
      } else {
        message.reply(new JsonObject().put("outcome", EventBusConstants.RESULT_FAILURE).put(EventBusConstants.RESULT_FAILURE_MESSAGE, res.cause()));
      }
    });
  }

}
