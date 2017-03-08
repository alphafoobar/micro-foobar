FROM foobar-payara

# Create an alias to allow easier debug running
# RUN echo alias java-debug=\'java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005\' >> /etc/profile.d/aliases.sh

# Default payara ports to expose
EXPOSE 5005 5006 8009 8080 8282

COPY target/sample-rest-0.0.1-SNAPSHOT.war /opt/payara/deployments/sample-rest.war

# To create
# docker build -t="sample-rest" -f=Dockerfile ..

