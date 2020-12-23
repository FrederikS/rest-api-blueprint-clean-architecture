# Category API

## Run spring app in dev mode

```shell
minikube start
eval $(minikube docker-env)
skaffold dev -p spring --port-forward
skaffold dev -p vertx --port-forward
```
