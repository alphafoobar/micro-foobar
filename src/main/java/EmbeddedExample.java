import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

/**
 * Basic Example showing how to programmatically create, edit, and
 * start an embedded Payara Server.
 *
 * @author Andrew Pielage
 */
public class EmbeddedExample {

    public static void main(String[] args) {
        try {
            GlassFishRuntime runtime = GlassFishRuntime.bootstrap();
            GlassFishProperties glassfishProperties = new GlassFishProperties();
            glassfishProperties.setPort("http-listener", 8080);
            GlassFish glassfish = runtime.newGlassFish(glassfishProperties);
            glassfish.start();
        } catch (GlassFishException ex) {
            Logger.getLogger(EmbeddedExample.class.getName()).log(Level.SEVERE,
                null, ex);
        }
    }
}