# Create a new Docker image from local

Creates a new image for this service based on the base image locally.

It also deploys the war on it. The `Dockerfile` needs to be in the root directory, so it can include
 sub directories in the [`COPY` command](https://docs.docker.com/engine/reference/builder/#copy).

# Create

  docker build -t="sample-rest" .
  
# Run

```bash
docker run -i -t -v ~/git/micro-foobar/sample-rest/target/:/opt/payara/deployments -p 8282:8080 -p 5005:5005 foobar-payara java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar /opt/payara/payara-micro.jar --deploy /opt/payara/deployments/sample-rest-0.0.1-SNAPSHOT.war
```

or now simpler:

```bash
docker run -i -t -p 8080:8080 -p 5006:5006 sample-rest java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006 -jar /opt/payara/payara-micro.jar --deploymentDir /opt/payara/deployments
```