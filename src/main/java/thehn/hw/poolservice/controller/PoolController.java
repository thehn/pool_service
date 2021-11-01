package thehn.hw.poolservice.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import thehn.hw.poolservice.exception.EndOfBucketException;
import thehn.hw.poolservice.exception.EndOfPoolException;
import thehn.hw.poolservice.exception.PoolIdNotFoundException;
import thehn.hw.poolservice.model.PoolDataRepository;
import thehn.hw.poolservice.model.PoolInsertRequest;
import thehn.hw.poolservice.model.PoolQuantileRequest;
import thehn.hw.poolservice.model.QuantileResult;


@RestController
public class PoolController {

    private final PoolDataRepository repository;
    static final Gson GSON = new Gson();

    @Autowired
    public PoolController(PoolDataRepository repository) {
        this.repository = repository;
    }

    @PostMapping(value = "/v1/pool", produces = "application/json; charset=utf-8")
    public String add(@RequestBody PoolInsertRequest rq) {
        return repository.insertOrAppend(rq);
    }

    @PostMapping(value = "/v1/pool/quantile", produces = "application/json; charset=utf-8")
    public String quantile(@RequestBody PoolQuantileRequest rq) {
        if (rq.getPercentile() < 0d || rq.getPercentile() > 100d)
            return "percentile out of bound: [0:100]";
        try {
            QuantileResult res = repository.queryQuantile(rq.getPoolId(), rq.getPercentile());
            return GSON.toJson(res);
        } catch (PoolIdNotFoundException e) {
            return "poolId not found. Please check the input again";
        } catch (EndOfBucketException | EndOfPoolException e) {
            return "Error: " + e.getMessage();
        }
    }
}
