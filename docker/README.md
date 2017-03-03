Some basic dockerfile examples
==============================

Use with Docker http://www.docker.io

To build an image with docker is pretty simple:

    cd rethinkdb
    docker build -t="rethinkdb" .

Then to run that image and attach to it at the same time:

    docker run -i -t rethinkdb
    
Or to run it in the background
  
    docker run -d rethinkdb
    
-----------------------------------
    
For more details on how to run a (payara docker image)[https://hub.docker.com/r/payara/micro/]

# To get started:

## Docker pull command

    docker pull payara/micro

## Quick start

    docker run -p 8080:8080 payara/micro java -jar /opt/payara/payara-micro.jar
