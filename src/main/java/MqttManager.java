import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MqttManager {
    private static final Logger LOGGER = Logger.getLogger(MqttManager.class.getName());
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = MqttClient.generateClientId();
    private IMqttClient mqttClient;

    public MqttManager() {
        init();
    }

    private void init() {
        try {
            mqttClient = new MqttClient(BROKER_URL, CLIENT_ID, new MemoryPersistence());
        } catch (MqttException e) {
            LOGGER.log(Level.SEVERE, "Failed to create MQTT client", e);
        }
    }

    public void setup() {
        if (mqttClient != null && !mqttClient.isConnected()) {
            try {
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                mqttClient.connect(connOpts);
                LOGGER.info("Connected to MQTT broker");
            } catch (MqttException e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to MQTT broker", e);
            }
        }
    }

    public byte[] listenToMessage(String topic) {
        final byte[][] newMessage = {null};
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.subscribe(topic, new IMqttMessageListener() {
                    @Override
                    public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
                        if (message != null) {
                            newMessage[0] = message.getPayload();
                        }


                    }

                });
            } catch (MqttException e) {
                LOGGER.log(Level.SEVERE, "Failed to subscribe to topic: " + topic, e);
            }
        } else {
            LOGGER.warning("MQTT client is not connected, cannot subscribe to topic: " + topic);
        }
        return newMessage[0];
    }

    public void cleanup() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
                LOGGER.info("Disconnected from MQTT broker");
            } catch (MqttException e) {
                LOGGER.log(Level.SEVERE, "Failed to disconnect from MQTT broker", e);
            }
        }
    }
}
