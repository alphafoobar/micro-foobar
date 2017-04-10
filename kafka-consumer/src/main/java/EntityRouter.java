import org.apache.kafka.streams.processor.AbstractProcessor;

public class EntityRouter extends AbstractProcessor<String, String> {

    @Override
    public void process(String key, String value) {
        String entity = JsonExtractor.returnJsonValue(value, "entity");
        context().forward(entity, value);
        context().commit();
    }
}
