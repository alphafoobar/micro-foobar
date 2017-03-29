import org.apache.kafka.streams.processor.AbstractProcessor;

/**
 * Created by James on 21/03/2017.
 */
public class EntityRouter  extends AbstractProcessor<String, String> {

    @Override
    public void process(String key, String value) {
        String entity = JSONExtractor.returnJSONValue(value, "entity");
        context().forward(entity, value);
        context().commit();
    }
}
