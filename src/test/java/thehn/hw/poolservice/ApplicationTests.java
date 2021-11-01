package thehn.hw.poolservice;

import com.google.gson.Gson;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import thehn.hw.poolservice.controller.PoolController;
import thehn.hw.poolservice.exception.EndOfBucketException;
import thehn.hw.poolservice.exception.EndOfPoolException;
import thehn.hw.poolservice.exception.PoolIdNotFoundException;
import thehn.hw.poolservice.model.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationTests {

    private final PoolController controller;
    private final PoolDataRepository repository;

    @Autowired
    public ApplicationTests(PoolController controller, PoolDataRepository repository) {
        this.controller = controller;
        this.repository = repository;
    }

    @Test
    @Order(1)
    void contextLoads() {
        Assertions.assertThat(controller).isNotNull();
    }

    @Test
    @Order(2)
    void dbLoads() {
        Assertions.assertThat(repository).isNotNull();
    }

    @Test
    @Order(3)
    void testInsert() throws PoolIdNotFoundException {
        PoolInsertRequest request = new PoolInsertRequest();
        request.setPoolId(123456);
        request.setPoolValues(new int[]{5, 1, 2, 4, 3});
        String response = controller.add(request);
        Assertions.assertThat(response).isEqualTo(OperationCode.INSERTED.name());
        Assertions.assertThat(repository.queryPoolSize(request.getPoolId())).isEqualTo(5);
    }

    @Test
    @Order(4)
    void testAppend() throws PoolIdNotFoundException {
        PoolInsertRequest request = new PoolInsertRequest();
        request.setPoolId(123456);
        request.setPoolValues(new int[]{0, 8, 6, 7, 9});
        String response = controller.add(request);
        Assertions.assertThat(response).isEqualTo(OperationCode.APPENDED.name());
        Assertions.assertThat(repository.queryPoolSize(request.getPoolId())).isEqualTo(10);
    }

    @Test
    @Order(5)
    void testQuantileCalculation() throws PoolIdNotFoundException, EndOfBucketException, EndOfPoolException {
        QuantileResult res = repository.queryQuantile(123456, 20);
        Assertions.assertThat(res.getPoolSize()).isEqualTo(10);
        Assertions.assertThat(res.getQuantile()).isEqualTo(1.8d);
    }

}
