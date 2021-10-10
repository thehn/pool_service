package thehn.hw.poolservice.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import thehn.hw.poolservice.exception.EndOfBucketException;
import thehn.hw.poolservice.exception.EndOfPoolException;
import thehn.hw.poolservice.exception.PoolIdNotFoundException;
import thehn.hw.poolservice.model.PoolDataRepository;
import thehn.hw.poolservice.model.PoolInsertRequest;
import thehn.hw.poolservice.model.PoolQuantileRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PoolController {

    private final PoolDataRepository repository;
    static final Gson GSON = new Gson();

    @Autowired
    public PoolController(PoolDataRepository repository) {
        this.repository = repository;
    }

    @PostMapping(value = "/v1/pool", produces = "application/json; charset=utf-8")
    private Mono<String> add(@RequestBody PoolInsertRequest rq) {
        return repository.insertOrAppend(rq);
    }

    @PostMapping(value = "/v1/pool/quantile", produces = "application/json; charset=utf-8")
    private Mono<String> quantile(@RequestBody PoolQuantileRequest rq) throws EndOfBucketException, EndOfPoolException {
        double q = 0;
        try {
            q = repository.queryQuantile(rq.getPoolId(), rq.getPercentile());
            int size = repository.queryPoolSize(rq.getPoolId());
            Map<String, Object> tmp = new HashMap<>();
            tmp.put("quantile", q);
            tmp.put("poolSize", size);
            return Mono.just(GSON.toJson(tmp));
        } catch (PoolIdNotFoundException e) {
            return Mono.just("poolId not found. Please check the input again");
        }
    }
}
