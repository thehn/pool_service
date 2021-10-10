# pool_service

## TCB homework

### Requirements:

- JDK 1.8 (required)
- Docker (optional)

### Build & run executable jar file

<code>
./mvnw install -U </br>
java -jar target/*.jar
</code>

### Build & deploy docker image

<code>
./mvnw install spring-boot:build-image </br>
docker run -d -p 8080:8080 poolservice:0.0.1-SNAPSHOT
</code>

### Test 

1. insert / append data to a pool <br>
   <code>
   curl --request POST 'localhost:8080/v1/pool' \
   --header 'Content-Type: application/json' \
   --data-raw '{
   "poolId":2,
   "poolValues":[5,1,3,2,4]
   }'
    </code>
2. query quantile value of a pool <br>
   <code>curl --request POST 'localhost:8080/v1/pool/quantile' \
   --header 'Content-Type: application/json' \
   --data-raw '{
   "poolId":2,
   "percentile":12.3
   }'</code>