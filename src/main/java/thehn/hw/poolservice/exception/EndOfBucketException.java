package thehn.hw.poolservice.exception;

public class EndOfBucketException extends Exception {
    public EndOfBucketException(String msg) {
        super(msg);
    }

    public EndOfBucketException() {
        super("Got end of bucket exception");
    }
}
