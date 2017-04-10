import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;

public class EntityDataLoader {

    public static final String TOPIC_NAME = "postilion-events";

    final Properties streamsConfiguration = new Properties();
    // In the subsequent lines we define the processing topology of the Streams application.
    final KStreamBuilder builder = new KStreamBuilder();
    final KafkaStreams streams;

    // Read the input Kafka topic into a KStream instance.
    final KStream<byte[], String> textLinesKStream;

    EntityDataLoader() {
        streamsConfiguration
            .put(StreamsConfig.APPLICATION_ID_CONFIG, "map-function-lambda-example");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        streamsConfiguration
            .put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
        streamsConfiguration
            .put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // Set up serializers and deserializers, which we will use for overriding the default Serdes
        // specified above.
        textLinesKStream = builder.stream(Serdes.ByteArray(), Serdes.String(), TOPIC_NAME);
        streams = new KafkaStreams(builder, streamsConfiguration);

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    void startStreams() {
        // Logs each message and tries to JSON parse it
        textLinesKStream
            // Make sure entity exists
            .filter((key, value) -> JsonExtractor.returnJsonValue(value, "entity") != null)
            // translate to new key value pairs
            .map((key, value) -> new KeyValue<>(JsonExtractor.returnJsonValue(value, "entity"),
                value))
            .foreach((key, value) -> {
                System.out.print(key + ":" + value);
                System.out.println(JsonExtractor.returnJsonValue(value, "entity"));
            });
        // send to another topic
        textLinesKStream.to("another-topic");

        streams.start();
    }

    public static void main(final String[] args) throws Exception {
        new EntityDataLoader().startStreams();
    }
}