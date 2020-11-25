# Category API

## Run spring app in dev mode

```shell
minikube start -p rest-api-blueprint-clean-architecture
eval $(minikube docker-env)
skaffold dev -p spring --port-forward
```

### TODO

- ALPS root information