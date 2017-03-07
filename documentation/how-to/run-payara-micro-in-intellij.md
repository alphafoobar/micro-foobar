Running in Docker seems like the plan.

I'm just connecting a remote debugger by configuring the following:

```
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
```

The full launch details:

```bash
docker run -i -t -v ~/git/micro-foobar/target/:/opt/payara/deployments -p 8282:8080 -p 5005:5005 foobar-payara java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar /opt/payara/payara-micro.jar --deploy /opt/payara/deployments/micro-foobar-0.0.1-SNAPSHOT.war
```

This allows hot replace to work, and the startup time is really quick.