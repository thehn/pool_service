package thehn.hw.poolservice.model;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import thehn.hw.poolservice.exception.EndOfBucketException;
import thehn.hw.poolservice.exception.EndOfPoolException;
import thehn.hw.poolservice.exception.PoolIdNotFoundException;

import java.util.concurrent.ConcurrentHashMap;


@Repository
public class PoolDataRepository {

    private final ConcurrentHashMap<Integer, Pool> db;

    public PoolDataRepository() {
        this.db = new ConcurrentHashMap<>();
    }

    public Mono<String> insertOrAppend(PoolInsertRequest rq) {
        if (db.containsKey(rq.getPoolId())) {
            db.get(rq.getPoolId()).append(rq.getPoolValues());
            return Mono.just(OperationCode.APPENDED.name());
        } else {
            db.put(rq.getPoolId(), new Pool(rq.getPoolValues()));
            return Mono.just(OperationCode.INSERTED.name());
        }
    }

    public double queryQuantile(int poolId, double percentile) throws EndOfBucketException, EndOfPoolException, PoolIdNotFoundException {
        if (db.containsKey(poolId)) {
            return db.get(poolId).calculateQuantile(percentile);
        } else {
            throw new PoolIdNotFoundException();
        }
    }

    public int queryPoolSize(int poolId) throws PoolIdNotFoundException {
        if (db.containsKey(poolId)) {
            return db.get(poolId).getSize();
        } else {
            throw new PoolIdNotFoundException();
        }
    }
}
