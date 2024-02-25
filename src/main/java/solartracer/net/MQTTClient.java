package solartracer.net;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.messages.MqttPublishMessage;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import solartracer.data.DataPoint;
import solartracer.data.DataPointListener;
import solartracer.serial.ShutdownListener;
import solartracer.utils.Constants;

import java.nio.charset.StandardCharsets;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static solartracer.main.Main.COORDINATOR;

public class MQTTClient implements ShutdownListener, Handler<MqttPublishMessage> {

    /** Logger. */
    private static final Logger LOGGER = LogManager.getLogger(MQTTClient.class);

    private final DataPointListener gui;

    private MqttClient client;

    private Vertx vertx;

    public MQTTClient(DataPointListener gui) {
        this.gui = gui;
//        MqttClientOptions options = new MqttClientOptions();
//        options.setSsl(true).setTrustOptions(new PemTrustOptions().addCertPath(Constants.CLIENT_CERT_PATH))
//                .setHostnameVerificationAlgorithm("HTTPS");
        vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(1));
        client = MqttClient.create(vertx);
    }

    public void connect(String broker) {
        if (!client.isConnected()) {
            client.connect(Constants.PORT, broker).onComplete(s -> client.disconnect())
                .onSuccess(succ ->
                {
                    LOGGER.debug("MQTTClient connection successful.");
                    client.publishHandler(this);
                    client.subscribe(Constants.TOPIC, Constants.MQTT_QOS);
                })
                .onFailure(e -> {
                    LOGGER.error("MQTTClient connection failed.");
                    LOGGER.error(e.getMessage());
                });
        }
    }

    public void shutdown() {
        client.disconnect();
        vertx.close();
    }

    @Override
    public void handle(MqttPublishMessage mqttPublishMessage) {
        LOGGER.debug("Msg recv.");
        String dataPointString = mqttPublishMessage.payload().toString(StandardCharsets.UTF_8);
        DataPoint dp = DataPoint.fromString(dataPointString);
        Platform.runLater(() -> gui.dataPointReceived(dp));
    }

}
