package kiwi.ergo.foobar.jaxrs;

import javax.annotation.Nullable;
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

    /**
     * Whatever endpoint for name.
     *
     * @param name Nullable.
     * @return Welcome object contains greeting, name and time.
     */
    @GET
    public Welcome whatever(@Nullable @QueryParam(value = "name") String name) {
        logger.info("> incoming request name=\"{}\"", name);
        String newName = name == null ? "duke" : name;
        return new Welcome(String.format("hey, brother: %s!", newName), newName);
    }

}