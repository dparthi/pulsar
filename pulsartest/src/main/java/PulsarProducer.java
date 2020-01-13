import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.shade.org.apache.avro.Protocol;
import org.apache.pulsar.shade.org.apache.avro.data.Json;

public class PulsarProducer {
    private Producer producer;

    public PulsarProducer(PulsarClient pc, String topicName) throws Exception {
        producer = pc.newProducer()
                .topic(topicName)
                .create();
    }

    public void send(String data) throws Exception {
        producer.send(data.getBytes());
    }
}
