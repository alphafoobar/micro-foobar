package kiwi.ergo.foobar.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import kiwi.ergo.foobar.jaxrs.MessageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("/")
public class Configuration extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    @Override
    public Set<Class<?>> getClasses() {

        Set<Class<?>> resources = new java.util.HashSet<>();

        // Add root resources
        resources.add(MessageResource.class);
        // Add JacksonFeature
//        resources.add(JacksonJsonProvider.class);
//        resources.add(JacksonFeature.class);
        return resources;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>();
//        props.put("jersey.config.disableMoxyJson.server", "true");
//        props.put(CommonProperties.MOXY_JSON_FEATURE_DISABLE, true);
//        props.put(CommonProperties.MOXY_JSON_FEATURE_DISABLE_SERVER, true);

        logger.info("properties requested properties={}", props);

        return props;
    }

}