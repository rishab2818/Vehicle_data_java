import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONObject;
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final int MAX_THREADS = 100;
    private static final int MAX_MESSAGES = 1000;
    private static int processedMessageCount = 0;

    public static void main(String[] args) {
        start();
    }

    public static void start() {
        KafkaManager kafkaManager = new KafkaManager();
        MqttManager mqttManager = new MqttManager();

        try {
            kafkaManager.setup();
            mqttManager.setup();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to setup Kafka or MQTT", e);
            cleanup(kafkaManager, mqttManager);
            return; // Terminate the program if setup fails
        }

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        boolean running = true;

        while (running) {
            try {
                byte[] message = mqttManager.listenToMessage("transport/data");

                    // Process message in a separate thread
                if (message != null) {
                    // If message is not null, process it using executor
                    System.out.println("Hello, World!");

                    executor.execute(() -> {

                        MessageHandler.handleMessage(message, kafkaManager);
                    });
                }

            } catch (Exception e) {
                System.out.println("Hello, World fuck!");
                LOGGER.log(Level.SEVERE, "Error occurred while listening to MQTT", e);
                running = false; // Terminate the program on error
            }
        }

        // Shutdown executor service
        executor.shutdown();
        try {
            // Wait for all threads to finish execution
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Executor service interrupted", e);
        }

        cleanup(kafkaManager, mqttManager);
    }

    public static void cleanup(KafkaManager kafkaManager, MqttManager mqttManager) {
        try {
            kafkaManager.cleanup();
            mqttManager.cleanup();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to clean up Kafka or MQTT", e);
        }
    }
   //code ends here
}
