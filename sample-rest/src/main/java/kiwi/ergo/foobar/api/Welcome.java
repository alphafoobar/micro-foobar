package kiwi.ergo.foobar.api;

import java.time.ZonedDateTime;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * MOXy JSON serialization requires POJOs implement serializable and have a default constructor.
 */
@XmlRootElement
public class Welcome {

    private String greeting;
    private String name;
    private ZonedDateTime dateTime;

    public Welcome() {}

    /**
     * Create Welcome. Default constructor required  for JSON deserialization.
     *
     * @param greeting Hello.
     * @param name World..
     */
    public Welcome(String greeting, String name) {
        this.greeting = greeting;
        this.name = name;
        this.dateTime = ZonedDateTime.now();
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
