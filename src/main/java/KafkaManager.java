import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaManager {
    private static final Logger LOGGER = Logger.getLogger(KafkaManager.class.getName());
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private KafkaProducer<String, String> producer;

    public void setup() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    public void sendMessage(String topic, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
        producer.send(record);
    }

    public void cleanup() {
        if (producer != null) {
            producer.close();
        }
    }
}
