# Kafka Health Proof of Concept

## Probes

Kubernetes (K8s), and therefore Openshift (OS), knows if a pod is running, but it does not know if the application inside the container is healthy. Every app will have its own idea of what "healthy" means, K8s provides a mechanism for testing health using `container probes`. Docker images can have health checks configured, but K8s ignores them in favor of its own probes. K8s (and therefore Openshift) defines two probes.

|probe|description|
|-----|-----------|
|readinessProbe|This probe works at Network level if it responses positive then the pod is marked as able to receive network traffic.|
|livenessProbe|This probe works at compute level if it responds non positive, then K8s will replace the pod container with a new one.|

K8s supports different types con container probe, we will use an HTTP GET action, which is perfect for web applications and APIs, an httpGet prove tels K8s to test the specified URL endpoint periodically; if the response has an HTTP status code between 200 and 399, then the probe succeeds; any other status code, or timeout, and it will fail.

