package kiwi.ergo.foobar.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import kiwi.ergo.foobar.api.Welcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("message")
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {

    private static final Logger logger = LoggerFactory.getLogger(MessageResource.class);

    @Context
    private UriInfo context;

    @GET
    @Produces("application/json")
    public Welcome whatever(@QueryParam(value = "name") String name) {
        logger.info("incoming request name=\"{}\"", name);
        logger.info("jersey.config.server.disableMoxyJson=\"{}\"", System.getProperty("jersey.config.server.disableMoxyJson"));
        String newName = name == null ? "duke" : name;
        return new Welcome(String.format("hey, %s!", newName), newName);
    }

}