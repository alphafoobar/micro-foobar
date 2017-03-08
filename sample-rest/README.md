# Sample working payara micro JSON based REST service 

## Maven build

```bash
mvn clean install
```

## Create docker image

```bash
docker build -t="sample-rest" -f=docker/Dockerfile .
```

## Run docker image

The docker image contains the war (it's copied on image build). This is running with debug on.

```bash
docker run -i -t -p 8080:8080 -p 5006:5006 sample-rest java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006 -jar /opt/payara/payara-micro.jar --deploymentDir /opt/payara/deployments
```

## Test

> http://localhost:8080/sample-rest/message?name=ral

# Repeat

You can also run a newly built artifact by declaring it on launch.

```bash
docker run -i -t -v ~/git/micro-foobar/sample-rest/target/:/opt/payara/deployments -p 8282:8080 -p 5006:5006 sample-rest java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006 -jar /opt/payara/payara-micro.jar --deploy /opt/payara/deployments/sample-rest-0.0.1-SNAPSHOT.war
```

In this case the resource would be accessible as:

> http://localhost:8080/sample-rest-0.0.1-SNAPSHOT/message?name=ral
