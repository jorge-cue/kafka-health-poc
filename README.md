# Kafka Health Proof of Concept

## Introduction

The concept to test with this project is to try to enable the monitoring of the Kafka connections (listeners and producers) and use it to respond to readiness and liveness probes on Kubernetes (K8s).

### Results so far

So far I am not able to detect when Kafka becomes unavailable neither for listeners nor producers.

## How to run the tests

### Step 1: Get Kafka in your local machine

[Download](https://www.apache.org/dyn/closer.cgi?path=/kafka/2.7.0/kafka_2.13-2.7.0.tgz]) the latest Kafka release and extract it:
```
tar -xzf kafka_2.13-2.7.0.tgz
cd kafka_2.13-2.7.0
```

### Step 2: Start the Kafka environment
Note: Your local environment must have Java 8+ installed.

Open a terminal and run the following commands to start zookeeper.
```
cd (to the directory where you installed Kafka on Step 1)

bin\windows\zookeeper-server-start.bat config\zookeeper.properties
```

Then open a second terminal and run the following commands to start kafka.
```
cd (to the directory where you installed Kafka on Step 1)

bin\windows\kafka-server-start.bat config\server.properties
```

Once all services have successfully launched, you will have a basic Kafka environment running and ready to use.

### Step 3: Run this application

Now run this application, the application creates the topic if required. Open a third terminal and run the following command.

```
gradle bootRun
```

### Step 4: To send a message though the program using postman

Send an HTTP PUT to the address `http://localhost:8080/send/{{message}}` where `{{message}}` should be replaced by the text (URL encoded) you want to send.

For example `PUT http://localhost:8080/send/Hello%20World`

The service shall respond with an HTTP STATUS 202 (Accepted), and the application should log the following entries:

```
 c.e.k.inbound.web.SenderController       : Success sending message: Hello World
	topic topic-1 partition 0 offset 0 key 4f37d6f5-0a57-4e13-8d94-5797d8f9e030 value Hello World
 c.e.k.i.kafka.KafkaHealthPocListener     : processing record: Hello World	topic topic-1 partition 0 offset 0 key 4f37d6f5-0a57-4e13-8d94-5797d8f9e030 value Hello World

```
Fields partition offset key my vary, but the general schema should be the same.

### Step 5: Force an error

Switch to the terminal where Kafka server is running and stop the server hitting Control-C. The server should stop, and the application should detect some kind of error and report it in the log.

## Liveness Probe

This probe is used to validate that the service is able to compute its result is defined as livenessProve, when it reports any HTTP STATUS out of the range 200 to 399 the probe will be flagged as failed, depending on configuration parameters K8s if the failure persists K8s will stop the pod´s container and relaunch a new container in the same pod, expecting that the condition can be solved with this refresh, giving the pod self-healing ability.  

To run the livenessProbe by hand use a `GET http://localhost:8080/health/liveness`; the application will respond with an HTTP status in the range 200 to 399 to pass the test, if the service is not available it will respond an HTTP status 503 (service unavailable) or timeout.

## Readiness Probe

This probe is used to validate that the service is able to receive network traffic and is defined as readinessProbe, when it reports any Http status between 200 and 399 the probe is successful, and the pod is enabled to receive network traffic. When the probe reports any status out of the range, or the call to the probe has a timeout, according to parameters in the probe configuration, the pod is marked as non available, and K8s suspends any network traffic to that pod for a while expecting that it becomes stable again, after a waiting time the probe is retried and the process repeats.

To run the readinessProbe by hand use a `GET http://localhost:8080/health/readiness`; the application will respond with an HTTP status in the range 200 to 399 to pass the test, if the service is not available it will respond an HTTP status 503 (service unavailable) or timeout.

## Probes

Kubernetes (K8s), and therefore Openshift (OS), knows if a pod is running, but it does not know if the application inside the container is healthy. Every app will have its own idea of what "healthy" means, K8s provides a mechanism for testing health using `container probes`. Docker images can have health checks configured, but K8s ignores them in favor of its own probes. K8s (and therefore Openshift) defines two probes.

|probe|description|
|-----|-----------|
|readinessProbe|This probe works at Network level if it responses positive then the pod is marked as able to receive network traffic.|
|livenessProbe|This probe works at compute level if it responds non positive, then K8s will replace the pod container with a new one.|

K8s supports different types con container probe, we will use an HTTP GET action, which is perfect for web applications and APIs, an httpGet prove tels K8s to test the specified URL endpoint periodically; if the response has an HTTP status code between 200 and 399, then the probe succeeds; any other status code, or timeout, is flagged as failed.
