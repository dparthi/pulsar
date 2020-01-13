import org.apache.pulsar.client.api.*;

public class PulsarConsumerDriver {
    public static void main(String[] args) {
        try {
            System.out.println("Initialising the client...");
            ClientBuilder builder = PulsarClient.builder();
            builder.serviceUrl("pulsar://localhost:6650");
            PulsarClient client = builder.build();

            System.out.println("Initialising the consumer for txns...");
            Consumer consumer = client.newConsumer()
                    .topic("txns")
                    .subscriptionName("txns-subscription")
                    .subscribe();

            boolean flag = true;
            int count = 0;
            while (flag) {
                Message msg = consumer.receive();
                try {
                    // Do something with the message
                    System.out.printf("Received:\t%s\n", new String(msg.getData()));

                    // Acknowledge the message so that it can be deleted by the message broker
                    consumer.acknowledge(msg);
                } catch (Exception e) {
                    // Message failed to process, redeliver later
                    System.out.println("Message lost. Trying to receive again...");
                    consumer.negativeAcknowledge(msg);
                }
            }
            consumer.close();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}