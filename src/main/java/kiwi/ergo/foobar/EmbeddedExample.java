package kiwi.ergo.foobar;

import fish.payara.micro.BootstrapException;
import fish.payara.micro.PayaraMicro;

/**
 * Basic Example showing how to programmatically create, edit, and
 * start an embedded Payara Server.
 *
 * Unrequired when declaring the launch war on the command line.
 *
 * @author Andrew Pielage
 */
public class EmbeddedExample {

    public static void main(String[] args) throws BootstrapException {
        PayaraMicro.bootstrap();
    }
}