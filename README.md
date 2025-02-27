## SCSB-Core

The SCSB Middleware codebase and components are all licensed under the Apache 2.0 license, with the exception of a set of API design components (JSF, JQuery, and Angular JS), which are licensed under MIT X11. 

SCSB-CORE is a microservice application that provides the core functionalities for all scenarios. All the core functionalities are handled and processed in this application. Other major processes handled are Accession, Deaccession, Transfer, Submit Collection, Bulk Request Process, Accession Reconciliation process, Status Reconciliation process, Daily Reconciliation process.

## Software Required

  - Java 11
  - Docker 19.03.13  

## Prerequisite

1.**Cloud Config Server**

Dspring.cloud.config.uri=http://phase4-scsb-config-server:8888

## Build

Download the Project , navigate inside project folder and build the project using below command

**./gradlew clean build -x test**

## Docker Image Creation

Naviagte Inside project folder where Dockerfile is present and Execute the below command

**sudo docker build -t phase4-scsb-core .**

## Docker Run

User the below command to Run the Docker

**sudo docker run --name phase4-scsb-core -v /data:/recap-vol -p 9097:9097  --label collect_logs_with_filebeat="true" --label decode_log_event_to_json_object="true" -e "ENV= -XX:+HeapDumpOnOutOfMemoryError  -XX:HeapDumpPath=/recap-vol/scsb-core/heapdump/ -Dorg.apache.activemq.SERIALIZABLE_PACKAGES="*"  -Dspring.cloud.config.uri=http://phase4-scsb-config-server:8888 "  --network=scsb  -d phase4-scsb-core**
