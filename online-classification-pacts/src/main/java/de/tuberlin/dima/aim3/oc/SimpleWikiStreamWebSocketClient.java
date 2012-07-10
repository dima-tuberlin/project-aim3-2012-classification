package de.tuberlin.dima.aim3.oc;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import de.tuberlin.dima.aim3.oc.stream.type.WikiEdit;
import de.tuberlin.dima.aim3.oc.stream.type.WikiEdit.RevisionChange;

public class SimpleWikiStreamWebSocketClient {

  private static final Logger LOG = LoggerFactory
      .getLogger("OnlineClassification");

  /**
   * Max number of revisions for each page to be kept.
   */
  private static final int MAX_REVISIONS = 99;

  private static JedisPool pool;

  /**
   * @param args
   */
  public static void main(String[] args) {
    // route socket.io-java-client's logging requests to our framework Logback
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    pool = new JedisPool(new JedisPoolConfig(), "localhost");

    SocketIO socket;
    try {
      socket = new SocketIO("http://localhost:3000/socket.io/1/");
    } catch (MalformedURLException e) {
      throw new RuntimeException("Cannot connect to WebSocket.", e);
    }

    socket.connect(new IOCallback() {
      @Override
      public void onMessage(JSONObject json, IOAcknowledge ack) {
        try {
          LOG.debug("Server said:" + json.toString(2));
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onMessage(String data, IOAcknowledge ack) {
        LOG.debug("Server said: " + data);
      }

      @Override
      public void onError(SocketIOException socketIOException) {
        LOG.error("an Error occured", socketIOException);
      }

      @Override
      public void onDisconnect() {
        LOG.info("Connection terminated.");
      }

      @Override
      public void onConnect() {
        LOG.info("Connection established");
      }

      @Override
      public void on(String event, IOAcknowledge ack, Object... args) {
        if (StringUtils.equals(event, "message")) {
          StringBuffer stringArgs = new StringBuffer();
          if (args.length == 1) {
            JSONObject jsonObject = (JSONObject) args[0];
            Jedis jedis = pool.getResource();
            try {
              String namespace = getNullSafeString(jsonObject, "namespace");
              if (!StringUtils.equals(namespace, "article")) {
                LOG.trace("Skipping event which is no article change");
                return;
              }

              String page = getNullSafeString(jsonObject, "page");
              String wikipedia = getNullSafeString(jsonObject, "wikipedia");
              String flag = getNullSafeString(jsonObject, "flag");
              String inputDelta = getNullSafeString(jsonObject, "delta");
              Integer delta = inputDelta == null ? 0 : Integer
                  .parseInt(inputDelta);
              String user = getNullSafeString(jsonObject, "user");
              String revision = getNullSafeString(jsonObject, "revision");

              WikiEdit wikiEdit = new WikiEdit(page, wikipedia, flag, delta,
                  user, revision);
              String key = wikiEdit.asKey();
              String value = wikiEdit.asValue();

              if (jedis.llen(key) <= 1) {
                LOG.debug("No past revisions found in DB for page '" + key
                    + "'");
              } else {
                LOG.debug("Found existing revisions in DB for page '" + key
                    + "'");
                List<String> wikiEdits = jedis.lrange(key, 0, MAX_REVISIONS);
                for (String wikiEditInput : wikiEdits) {
                  WikiEdit existingWikiEdit = WikiEdit
                      .parse(key, wikiEditInput);

                  boolean foundChange = false;
                  RevisionChange revChange = existingWikiEdit
                      .analyzeChanges(wikiEdit);
                  switch (revChange) {
                  case SAMEUSER_CONTENTCHANGE:
                    LOG.info("Page '" + wikiEdit.getPage()
                        + "' was EDITED AGAIN by user '" + wikiEdit.getUser()
                        + "' in revision ID '" + wikiEdit.getRevision()
                        + "'. Last edit by this user in revision ID '"
                        + existingWikiEdit.getRevision() + "'");
                    foundChange = true;
                    break;
                  case SAMEUSER_CONTENTREVERT:
                    LOG.info("Page '" + wikiEdit.getPage() + "': User '"
                        + wikiEdit.getUser()
                        + "' REVERTED her own change from revision '"
                        + existingWikiEdit.getRevision() + "' in revision ID '"
                        + wikiEdit.getRevision() + "'");
                    foundChange = true;
                    break;
                  case DIFFERENTUSER_CONTENTCHANGE:
                    LOG.trace("Usual content update of page '"
                        + wikiEdit.getPage() + "' by user '"
                        + wikiEdit.getUser() + "' in revision ID '"
                        + wikiEdit.getRevision() + "'");
                    break;
                  case DIFFERENTUSER_CONTENTREVERT:
                    LOG.warn("Found possible flame war for page '"
                        + wikiEdit.getPage() + "' between users '"
                        + wikiEdit.getUser() + "' and '"
                        + existingWikiEdit.getUser() + "' from revision ID '"
                        + wikiEdit.getRevision() + "' to revision ID '"
                        + existingWikiEdit.getRevision() + "'");
                    foundChange = true;
                    break;

                  default:
                    // found no relevant changes
                    break;
                  }
                  if (foundChange) {
                    break;
                  }
                }
              }

              jedis.lpush(key, value);
              jedis.ltrim(key, 0, MAX_REVISIONS);

            } catch (JSONException e1) {
              LOG.error("Failed to parse WikiStreamMessage");
              e1.printStackTrace();
            } finally {
              pool.returnResource(jedis);
            }
          }
        } else {
          LOG.error("Server triggered UNKNOWN event '" + event + "'");
        }
      }

      private String getNullSafeString(JSONObject jsonObject, String key)
          throws JSONException {
        String result = jsonObject.getString(key);
        if (StringUtils.equals("null", result)) {
          result = null;
        }
        return result;
      }
    });

    // This line is cached until the connection is established.
    socket.send("Hi WikiStream!");
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();

    if (pool != null) {
      pool.destroy();
    }
  }
}
