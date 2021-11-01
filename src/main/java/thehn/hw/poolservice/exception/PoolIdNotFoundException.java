package thehn.hw.poolservice.exception;

public class PoolIdNotFoundException extends RuntimeException {
    public PoolIdNotFoundException() {
        super("Given pool id not found");
    }
}
