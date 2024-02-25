package solartracer.net;

import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.codes.MqttSubAckReasonCode;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import solartracer.data.DataPoint;
import solartracer.data.DataPointListener;
import solartracer.gui.GuiController;
import solartracer.serial.ShutdownListener;
import solartracer.utils.Constants;

import java.util.*;
import java.util.concurrent.*;

import static solartracer.main.Main.COORDINATOR;

public class MQTTServer implements ShutdownListener, DataPointListener {

    /** Logger. */
    private static final Logger LOGGER = LogManager.getLogger(MQTTServer.class);
    private final BooleanProperty running = new SimpleBooleanProperty(false);
    private final Queue<DataPoint> dataPointQueue = new ConcurrentLinkedQueue<>();
    private Vertx vertx;
    private MqttServer server;
    private final List<MqttSubAckReasonCode> reasonCodes = new ArrayList<>();

    public MQTTServer(GuiController gui) {
        //DeploymentOptions options = new DeploymentOptions().setInstances(1);
       // vertx.deployVerticle("solartracer.net.MQTTVerticle", options);

        //TODO Setup SSL security.
//        MqttServerOptions serverOpts = new MqttServerOptions();
//        serverOpts.setPort(Constants.PORT).setKeyCertOptions(
//                new PemKeyCertOptions().setKeyPath(Constants.KEY_PATH).setCertPath(Constants.CERT_PATH));
        gui.getCircle().fillProperty().bind(
                Bindings.when(running)
                        .then(Color.GREEN)
                        .otherwise(Color.RED));
    }

    public void connect() {
        if (running.get()) {
            return;
        }
        vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(1));
        server = MqttServer.create(vertx);
        server.endpointHandler(endpoint -> {
                    endpoint.subscribeHandler( s -> {
                        for (MqttTopicSubscription mts : s.topicSubscriptions()) {
                            reasonCodes.add(MqttSubAckReasonCode.qosGranted(mts.qualityOfService()));
                        }
                        endpoint.subscribeAcknowledge(s.messageId(), reasonCodes, MqttProperties.NO_PROPERTIES);
                        COORDINATOR.scheduleAtFixedRate(() -> {
                            if (running.get()) {
                                sendMessages(endpoint);
                            }
                        }, 0, 1, TimeUnit.SECONDS);
                    });
                    endpoint.unsubscribeHandler(u -> {
                       for (String topic : u.topics()) {
                           LOGGER.debug("Unsubscription for topic {}", topic);
                       }
                       endpoint.unsubscribeAcknowledge(u.messageId());
                    });
                    endpoint.publishHandler(clientMsg -> {
                        if (clientMsg.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                            endpoint.publishAcknowledge(clientMsg.messageId());
                        } else if (clientMsg.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                            endpoint.publishReceived(clientMsg.messageId());
                        }
                    }).publishReleaseHandler(endpoint::publishComplete);
                    // accept connection from the remote client
                    endpoint.accept(false);

                })
                .listen(Constants.PORT)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        LOGGER.debug("MQTT server is listening on port {}", ar.result().actualPort());
                        running.set(true);
                    } else {
                        LOGGER.error("Error on starting the server: {}", ar.cause().getMessage());
                        running.set(false);
                    }
                });
    }

    public void sendMessages(MqttEndpoint endpoint) {
        LOGGER.debug("Sending...");
        while (!dataPointQueue.isEmpty()) {
            endpoint.publish(Constants.TOPIC,
                    Buffer.buffer(dataPointQueue.poll().toString()),
                    MqttQoS.AT_LEAST_ONCE,
                    false,
                    false);
        }
    }

    @Override
    public void dataPointReceived(DataPoint dataPoint) {
        dataPointQueue.add(dataPoint);
    }

    @Override
    public void shutdown() {
        if (running.get()) {
            server.close().onComplete(s -> {
                LOGGER.debug("MQTT Shutdown.");
                running.set(false);
            });
        }
        if (vertx != null) {
            vertx.close();
        }
    }

}
