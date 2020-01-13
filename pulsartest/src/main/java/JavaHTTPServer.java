import org.apache.pulsar.client.api.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

// The tutorial can be found just here on the SSaurel's Blog : 
// https://www.ssaurel.com/blog/create-a-simple-http-web-server-in-java
// Each Client Connection will be managed in a dedicated Thread
public class JavaHTTPServer implements Runnable{

    // port to listen connection
    static final int PORT = 9090;
    static final int MAX_REDIS_RETRIES = 100;
    static final int BASE_REDIS_WAITTIME_NANO = 50;

    // verbose mode
    static final boolean verbose = true;

    // Client Connection via Socket Class
    private Socket connect;
    private PulsarProducer producer;
    private String data;

    private static PulsarClient pulsarClient0 = createPulsarClient();

    public JavaHTTPServer(Socket c, PulsarProducer p) {
        connect = c;
        producer = p;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            MyLogger.log("[Server started. Listening for connections on port : " + PORT + "]");

            int numberOfProducers = 1;
            PulsarProducer[] producers = new PulsarProducer[numberOfProducers];
            for (int i = 0; i < numberOfProducers; i++) {
                producers[i] = new PulsarProducer(pulsarClient0, "requests");
            }

            //start the consumer to listen in to the topic before starting the producer
            int numberOfConsumers = 3;
            PulsarConsumer[] consumers = new PulsarConsumer[numberOfConsumers];
            Thread[] pcThreads = new Thread[numberOfConsumers];
            for (int i = 0; i < numberOfConsumers; i++) {
                consumers[i] = new PulsarConsumer(JavaHTTPServer.pulsarClient0);
                pcThreads[i] = new Thread(consumers[i]);
                pcThreads[i].start();
            }

            // we listen until user halts server execution
            int i = 0;
            while (true) {
                JavaHTTPServer myServer = new JavaHTTPServer(serverConnect.accept(), producers[i]);
                // create dedicated thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
                i = (i+1) % numberOfProducers;
            }

        } catch (Exception e) {
            System.err.println("Server Error : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        long requestReceivedTime = System.nanoTime();
        // we manage our particular client connection
        BufferedReader in = null;
        Jedis jedis = new Jedis();
        try {
            // we read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));

            // get first line of the request from the client
            String input = in.readLine();
            // we parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
            // get the request id sent by the client
            //TODO: the substring(1) is used to remove any leading "/"; need to ensure this is a good idea!!!
            String requestData = parse.nextToken().toLowerCase().substring(1);
            StringTokenizer parse1 = new StringTokenizer(requestData, "=");
            String key1 = parse1.nextToken();
            String value1 = parse1.nextToken();
            requestData = value1;

            producer.send(requestData);

            /* Creating JedisPubSub object for subscribing with channels */
//            JedisPubSub jedisPubSub = new JedisPubSub() {
//                @Override
//                public void onMessage(String channel, String message) {
//                    MyLogger.log("[redis message: " + message + "]");
//                    unsubscribe(channel);
//                }
//            };
//            jedis.subscribe(jedisPubSub, requestData);

            data = jedis.get(requestData);
            int numberOfRetries = 0;
            while (null == data && numberOfRetries < MAX_REDIS_RETRIES) {
                numberOfRetries++;
                int waitTime = BASE_REDIS_WAITTIME_NANO * numberOfRetries;
                Thread.sleep(0,waitTime);
                data = jedis.get(requestData);
            }

            PrintWriter out = null;
            BufferedOutputStream dataOut = null;
            // we get character output stream to client (for headers)
            out = new PrintWriter(connect.getOutputStream());
            // get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(connect.getOutputStream());
            String responseData;

            if (null == data) {
                responseData = "Server Error";
                sendErrorResponse(out, dataOut, responseData);
            } else {
                responseData = data;
                sendResponse(out, dataOut, responseData);
            }
            connect.close();
            long latency = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - requestReceivedTime);
            MyLogger.log("[" + latency + "] [" + requestData + "] [" + responseData + "]");
        } catch (Exception e) {
            MyLogger.logError("[Server Error] " + e);
        }
    }

    private void sendResponse (PrintWriter out, OutputStream dataOut, String responseData) throws IOException {
        String content = "application/json";
        out.println("HTTP/1.1 200 OK");
        out.println("Server: Pulsar Deluxe");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + responseData.length());
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer

        dataOut.write(responseData.getBytes(), 0, responseData.length());
        dataOut.flush();
    }

    private void sendErrorResponse (PrintWriter out, OutputStream dataOut, String responseData) throws IOException {
        String content = "text/plain";
        out.println("HTTP/1.1 500 Server Error");
        out.println("Server: Pulsar Deluxe");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + responseData.length());
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer

        dataOut.write(responseData.getBytes(), 0, responseData.length());
        dataOut.flush();
    }

    private static PulsarClient createPulsarClient() {
        PulsarClient client = null;
        try {
            MyLogger.log("[Initialising the pulsar client]");
            ClientBuilder builder = PulsarClient.builder();
            builder.serviceUrl("pulsar://localhost:6650");
            client = builder.build();
        } catch (PulsarClientException e) {
            MyLogger.logError("[Unable to create pulsar client. Quitting]");
            System.exit(1);
        }
        return client;
    }
}