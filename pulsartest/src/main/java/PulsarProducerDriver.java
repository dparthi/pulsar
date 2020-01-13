import org.apache.pulsar.client.api.*;

public class PulsarProducerDriver {
    public static void main(String[] args) {
        try {
            System.out.println("Initialising the client...");
            ClientBuilder builder = PulsarClient.builder();
            builder.serviceUrl("pulsar://localhost:6650");
            PulsarClient client = builder.build();

            System.out.println("Initialising the producer for my-topic...");
            Producer<byte[]> producer = client.newProducer()
                    .topic("my-topic")
                    .create();

            boolean flag = true;
            int count = 0;
            while (flag) {
                String message = "Current count: " + (count++);
                producer.send(message.getBytes());
                System.out.printf("Sending:\t%s\n", message);
            }
            producer.close();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
