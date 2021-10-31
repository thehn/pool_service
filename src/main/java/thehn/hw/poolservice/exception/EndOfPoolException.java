package thehn.hw.poolservice.exception;

public class EndOfPoolException extends Exception {
    public EndOfPoolException(String msg) {
        super(msg);
    }

    public EndOfPoolException() {
        super("Got end of pool");
    }
}
