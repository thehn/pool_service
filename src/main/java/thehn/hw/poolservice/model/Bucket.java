package thehn.hw.poolservice.model;

import it.unimi.dsi.fastutil.ints.Int2IntRBTreeMap;
import thehn.hw.poolservice.exception.EndOfBucketException;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Stores a bucket of sorted values
 */
public class Bucket {
    private final Int2IntRBTreeMap values = new Int2IntRBTreeMap();
    private AtomicInteger size = new AtomicInteger(0);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public Bucket(int val) {
        addValue(val);
    }

    public Bucket(int[] arr) {
        addValues(arr);
    }

    public void append(int[] arr) {
        addValues(arr);
    }

    public void append(int val) {
        addValue(val);
    }

    public int getSize() {
        return this.size.get();
    }

    /**
     * Gets values of 2 consecutive elements at position index and index + 1;
     *
     * @param index first position
     * @return the array contains 2 consecutive elements at position index and index + 1
     */
    public int[] get2ConsecutiveElements(int index) {
        if (index < 0 || index >= getSize() - 1)
            throw new IndexOutOfBoundsException();

        int[] results = new int[2];
        int iter = 0;
        boolean check = false;
        Lock rl = lock.readLock();

        try {
            rl.lock();
            for (Map.Entry<Integer, Integer> entry : values.int2IntEntrySet()) {
                if (check) {
                    results[1] = entry.getKey();
                    return results;
                }
                iter += entry.getValue();
                if (iter > index) {
                    results[0] = entry.getKey();
                    if (iter - index > 1) {
                        results[1] = entry.getKey();
                        return results;
                    }
                    check = true;
                }
            }
            return results;
        } finally {
            rl.unlock();
        }
    }

    public int getAt(int index) throws EndOfBucketException {
        Lock rl = lock.readLock();
        try {
            rl.lock();

            if (index < 0 || index >= getSize())
                throw new IndexOutOfBoundsException();

            if (index == 0)
                return values.firstIntKey();
            if (index == getSize() - 1)
                return values.lastIntKey();

            int iter = 0;
            for (Map.Entry<Integer, Integer> entry : values.int2IntEntrySet()) {
                iter += entry.getValue();
                if (iter > index) return entry.getKey();
            }
            throw new EndOfBucketException("Iterated over bucket but not found value");
        } finally {
            rl.unlock();
        }
    }

    /**
     * Adds a single value to bucket
     */
    private void addValue(int val) {
        Lock wl = lock.writeLock();
        try {
            wl.lock();
            if (values.containsKey(val)) {
                int count = values.get(val) + 1;
                values.put(val, count);
            } else {
                values.put(val, 1);
            }
            size.incrementAndGet();
        } finally {
            wl.unlock();
        }
    }

    /**
     * Adds array of values to bucket
     */
    private void addValues(int[] arr) {
        Lock wl = lock.writeLock();
        try {
            wl.lock();
            for (int val : arr) {
                if (values.containsKey(val)) {
                    int count = values.get(val) + 1;
                    values.put(val, count);
                } else {
                    values.put(val, 1);
                }
                size.incrementAndGet();
            }
        } finally {
            wl.unlock();
        }
    }

}
