package kiwi.ergo.foobar.api;

import java.io.Serializable;

/**
 * MOXy JSON serialization requires POJOs implement serializable and have a default constructor.
 */
public class Welcome implements Serializable {

    private String greeting;
    private String name;

    public Welcome() {}

    public Welcome(String greeting, String name) {
        this.greeting = greeting;
        this.name = name;
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
}
