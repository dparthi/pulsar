import org.apache.pulsar.client.api.*;
import redis.clients.jedis.Jedis;

public class PulsarConsumer implements Runnable {
    private PulsarClient pulsarClient;

    public PulsarConsumer(PulsarClient pc) {
        pulsarClient = pc;
    }

    @Override
    public void run() {
        try {
            MyLogger.log("[Initialising the consumer for topic 'requests']");
            Consumer consumer = pulsarClient.newConsumer()
                    .topic("requests")
                    .subscriptionName("requests-subscription")
                    .subscriptionType(SubscriptionType.Shared)
                    .subscribe();

            Jedis jedis = new Jedis();
            boolean flag = true;
            int count = 0;
            while (flag) {
                Message msg = consumer.receive();
                try {
                    String requestData = new String(msg.getData());
                    // Do something with the message
//                    MyLogger.log("[Received from requests: " + requestData + "]");

                    try {
                        jedis.set(requestData, "{response:" + requestData + "}");
//                        jedis.publish(requestData,"received " +requestData);
//                        Producer producer = pulsarClient.newProducer(Schema.STRING)
//                                .topic(requestData)
//                                .create();
//                        producer.send("{response:" + requestData + "}");

                        // Acknowledge the message so that it can be deleted by the message broker
                        consumer.acknowledge(msg);
                    } catch (Exception e) {
                        // Message failed to process, redeliver later
                        MyLogger.logError("[Error while writing to " + requestData + " topic: " + e.getMessage() + "]");
                        consumer.negativeAcknowledge(msg);
                    }
                } catch (Exception e) {
                    // Message failed to process, redeliver later
                    MyLogger.logError("[Error while reading from request topic: " + e.getMessage() + "]");
                    consumer.negativeAcknowledge(msg);
                }
            }
            consumer.close();
            jedis.close();
        } catch (Exception e) {
            MyLogger.logError("[Error while trying to subscribe/receive from requests topic: " + e.getMessage() + "]");
        }
    }
}