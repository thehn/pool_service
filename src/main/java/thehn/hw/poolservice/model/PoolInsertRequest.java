package thehn.hw.poolservice.model;

public class PoolInsertRequest {
    private int poolId;
    private int[] poolValues;

    public void setPoolId(int poolId) {
        this.poolId = poolId;
    }

    public void setPoolValues(int[] poolValues) {
        this.poolValues = poolValues;
    }

    public int getPoolId() {
        return this.poolId;
    }

    public int[] getPoolValues() {
        return this.poolValues;
    }
}
