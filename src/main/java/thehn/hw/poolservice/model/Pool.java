package thehn.hw.poolservice.model;


import thehn.hw.poolservice.exception.EndOfBucketException;
import thehn.hw.poolservice.exception.EndOfPoolException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Stores all values of a pool
 */
public class Pool {
    private int bucketCapacity = 1_000; // default value just for test
    private final AtomicInteger size = new AtomicInteger(0);
    private final Map<Integer, Bucket> buckets = new ConcurrentHashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public Pool(int[] arr) {
        add(arr);
    }

    public Pool(int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
    }

    public Pool(int[] arr, int bucketCapacity) {
        if (bucketCapacity <= 0)
            throw new IllegalArgumentException("Bucket capacity must be greater than 0");

        this.bucketCapacity = bucketCapacity;
        add(arr);
    }

    public void append(int[] arr) {
        add(arr);
    }

    public int getSize() {
        return size.get();
    }

    /**
     * Gets pool element at index
     */
    public int get(int index) throws EndOfBucketException, EndOfPoolException {
        if (index < 0 || index >= getSize())
            throw new IndexOutOfBoundsException();

        int count = 0;
        int k = 0;
        Bucket tmpBucket = null;
        Lock rl = lock.readLock();

        try {
            rl.lock();
            for (Map.Entry<Integer, Bucket> entry : buckets.entrySet()) {
                int lastCount = count;
                count += entry.getValue().getSize();
                tmpBucket = entry.getValue();
                if (count > index) {
                    k = index - lastCount;
                    break;
                }
            }

            if (tmpBucket == null)
                throw new EndOfPoolException();

            return tmpBucket.getAt(k);
        } finally {
            rl.unlock();
        }
    }

    /**
     * Gets values of 2 consecutive elements at position index and index + 1 of the pool
     *
     * @param index first position
     * @return the array contains 2 consecutive elements at position index and index + 1
     */
    public int[] get2ConsecutiveElements(int index) throws EndOfPoolException, EndOfBucketException {
        if (index < 0 || index >= getSize() - 1)
            throw new IndexOutOfBoundsException();

        int count = 0;
        int k = 0;
        Bucket firstBucket = null;
        int nextBucketId = 0;
        boolean locatedOnSameBucket = false;
        Lock rl = lock.readLock();

        try {
            rl.lock();
            for (Map.Entry<Integer, Bucket> entry : buckets.entrySet()) {
                int lastCount = count;
                count += entry.getValue().getSize();
                if (count > index) {
                    firstBucket = entry.getValue();
                    k = index - lastCount;
                    if (count - index > 1) {
                        locatedOnSameBucket = true;
                    }
                    nextBucketId = entry.getKey() + 1;
                    break;
                }
            }

            if (firstBucket == null) {
                throw new EndOfPoolException("Iterated over pool but not found bucket");
            }

            if (locatedOnSameBucket) {
                return firstBucket.get2ConsecutiveElements(k);
            } else {
                int[] results = new int[2];
                Bucket nextBucket = buckets.get(nextBucketId);
                results[0] = firstBucket.getAt(k);
                results[1] = nextBucket.getAt(0);
                return results;
            }
        } finally {
            rl.unlock();
        }
    }

    /**
     * Calculates quantile on current pool values by given percentile
     *
     * @param percentile percentile
     * @return quantile
     */
    public QuantileResult calculateQuantile(double percentile) throws EndOfBucketException, EndOfPoolException {
        if (percentile < 0d || percentile > 100d)
            throw new IllegalArgumentException("Percentile must be in range [0,100]");

        Lock rl = lock.readLock();
        try {
            rl.lock();
            double q = percentile / 100d;
            double position = (getSize() - 1) * q;
            int index = (int) Math.floor(position);
            double fraction = position - index;
            QuantileResult result = new QuantileResult();
            result.setPoolSize(getSize());

            if (index < getSize() - 1) {
                int[] tmp = get2ConsecutiveElements(index);
                double tmpQ = tmp[0] + fraction * (tmp[1] - tmp[0]);
                result.setQuantile(tmpQ);
            } else {
                double tmpQ = get(index);
                result.setQuantile(tmpQ);
            }

            return result;
        } finally {
            rl.unlock();
        }

    }

    private void add(int[] arr) {
        Lock wl = lock.writeLock();
        try {
            wl.lock();
            for (int val : arr) {
                int id = val / bucketCapacity;
                buckets.compute(id, (k, v) -> {
                    if (v == null) {
                        return new Bucket(val);
                    } else {
                        v.append(val);
                    }
                    return v;
                });
                size.incrementAndGet();
            }
        } finally {
            wl.unlock();
        }
    }
}
