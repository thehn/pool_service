package thehn.hw.poolservice.model;

public class QuantileResponse {
    private double quantile;
    private int poolSize;

    public QuantileResponse() {
    }

    public QuantileResponse(double q, int size) {
        this.quantile = q;
        this.poolSize = size;
    }

    public void setQuantile(double quantile) {
        this.quantile = quantile;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public double getQuantile() {
        return quantile;
    }

    public int getPoolSize() {
        return poolSize;
    }
}
