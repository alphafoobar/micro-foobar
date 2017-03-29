import info.batey.kafka.unit.KafkaUnit;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.Test;

/**
 * Created by James on 21/03/2017.
 */
public class EntityDataLoaderTest {

    @Test
    public void startStreams() throws Exception {
        KafkaUnit kafkaUnitServer = new KafkaUnit("localhost:5000", "localhost:9092");
        Properties streamsConfiguration = new Properties();
        streamsConfiguration
            .put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
        streamsConfiguration
            .put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUnitServer.getKafkaConnect());

        Producer<byte[], String> producer = new KafkaProducer<>(streamsConfiguration, new ByteArraySerializer(), new StringSerializer());

        kafkaUnitServer.startup();
        kafkaUnitServer.createTopic(EntityDataLoader.TOPIC_NAME);

        // System under test
        new EntityDataLoader().startStreams();

        String json = "{\"date\":{\"string\":\"2017-03-20\"},\"time\":{\"string\":\"20:04:13:563\"},\"event_nr\":1572470,\"interface\":\"Transaction Manager\",\"event_id\":5001,\"date_time\":1490040253563,\"entity\":\"Transaction Manager\",\"state\":0,\"msg_param_1\":{\"string\":\"ISWSnk\"},\"msg_param_2\":{\"string\":\"Application startup\"},\"msg_param_3\":null,\"msg_param_4\":null,\"msg_param_5\":null,\"msg_param_6\":null,\"msg_param_7\":null,\"msg_param_8\":null,\"msg_param_9\":null,\"long_msg_param_1\":null,\"long_msg_param_2\":null,\"long_msg_param_3\":null,\"long_msg_param_4\":null,\"long_msg_param_5\":null,\"long_msg_param_6\":null,\"long_msg_param_7\":null,\"long_msg_param_8\":null,\"long_msg_param_9\":null,\"last_sent\":{\"long\":1490040253563},\"transmit_count\":{\"int\":1},\"team_id\":null,\"app_id\":{\"int\":4},\"logged_by_app_id\":{\"int\":4},\"entity_type\":{\"int\":3},\"binary_data\":null}";

        ProducerRecord<byte[], String> keyedMessage = new ProducerRecord<>(EntityDataLoader.TOPIC_NAME, "key".getBytes(), json);
        Future<RecordMetadata> future = producer.send(keyedMessage);


        RecordMetadata recordMetadata = future.get(5, TimeUnit.SECONDS);
        System.out.println(recordMetadata);
        kafkaUnitServer.shutdown();
    }

}