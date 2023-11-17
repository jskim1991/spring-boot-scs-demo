1. Run Kafka
```shell
docker-compose up
```

2. Call producers
```
curl localhost:8080/produce/orders
curl localhost:8080/produce/deliveries
curl localhost:8080/produce/sink
```

3. Check consumer logs in the console output