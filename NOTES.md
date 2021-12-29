Project reactor
Project Reactor Reference Guide 


Connecting to Mongo DB
- K8S (minikube)
- set nodeport
- run minikube service list
- get url
- dev container - appPort: [nodeportForMongo]
- install MongoDB plugin
- create connection - url from minikube service list (need to be the host path)
-  mongodb://urlfromServiceList

Spring Mongo
- set url 