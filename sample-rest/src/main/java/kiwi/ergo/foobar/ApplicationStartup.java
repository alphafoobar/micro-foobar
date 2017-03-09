package kiwi.ergo.foobar;

import fish.payara.micro.PayaraMicro;
import fish.payara.micro.PayaraMicroRuntime;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@SuppressWarnings("unused")
@Singleton
//@javax.ejb.Startup
public class ApplicationStartup {

    /**
     * Not used.
     */
    @PostConstruct
    public void start() {
        final PayaraMicroRuntime pmRuntime = PayaraMicro.getInstance().getRuntime();
        pmRuntime.run(
            "healthcheck-configure", "--enabled=true", "--dynamic=true");
        pmRuntime.run(
            "healthcheck-configure-service",
            "--serviceName=healthcheck-cpu", "--enabled=true",
            "--time=5", "--unit=SECONDS", "--dynamic=true");
        pmRuntime.run(
            "healthcheck-configure-service-threshold",
            "--serviceName=healthcheck-cpu",
            "--thresholdCritical=90", "--thresholdWarning=50",
            "--thresholdGood=0", "--dynamic=true");
        pmRuntime.run(
            "healthcheck-configure-service",
            "--serviceName=healthcheck-machinemem",
            "--enabled=true", "--dynamic=true", "--time=5","--unit=SECONDS");
        pmRuntime.run(
            "healthcheck-configure-service-threshold",
            "--serviceName=healthcheck-machinemem",
            "--thresholdCritical=90", "--thresholdWarning=50", "--thresholdGood=0",
            "--dynamic=true");
    }

}