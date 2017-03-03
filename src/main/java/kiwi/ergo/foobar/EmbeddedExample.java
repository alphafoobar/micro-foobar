package kiwi.ergo.foobar;

import fish.payara.micro.BootstrapException;
import fish.payara.micro.PayaraMicro;

/**
 * Basic Example showing how to programmatically create, edit, and
 * start an embedded Payara Server.
 *
 * @author Andrew Pielage
 */
public class EmbeddedExample {

    public static void main(String[] args) throws BootstrapException {
        PayaraMicro.bootstrap();
    }
}