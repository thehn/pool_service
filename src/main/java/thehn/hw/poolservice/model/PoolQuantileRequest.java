package thehn.hw.poolservice.model;

public class PoolQuantileRequest {
    private int poolId;
    private double percentile;

    public void setPoolId(int poolId) {
        this.poolId = poolId;
    }

    public void setPoolValues(double percentile) {
        this.percentile = percentile;
    }

    public int getPoolId() {
        return this.poolId;
    }

    public double getPercentile() {
        return this.percentile;
    }
}
