package thehn.hw.poolservice.model;

import it.unimi.dsi.fastutil.ints.Int2IntRBTreeMap;
import thehn.hw.poolservice.exception.EndOfBucketException;

import java.util.Arrays;
import java.util.Map;

/**
 * Stores a bucket of sorted values
 */
public class Bucket {
    private final Int2IntRBTreeMap values = new Int2IntRBTreeMap();
    private int size;

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
        return this.size;
    }

    public void show() {
        System.out.println(values);
        System.out.println("Size: " + size);
    }

    /**
     * Gets values of 2 consecutive elements at position index and index + 1;
     *
     * @param index first position
     * @return the array contains 2 consecutive elements at position index and index + 1
     */
    public int[] get2ConsecutiveElements(int index) {
        if (index < 0 || index >= size - 1)
            throw new IndexOutOfBoundsException();

        int[] results = new int[2];
        int iter = 0;
        boolean check = false;
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
    }

    public int getAt(int index) throws EndOfBucketException {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        if (index == 0)
            return values.firstIntKey();
        if (index == size - 1)
            return values.lastIntKey();

        int iter = 0;
        for (Map.Entry<Integer, Integer> entry : values.int2IntEntrySet()) {
            iter += entry.getValue();
            if (iter > index) return entry.getKey();
        }

        throw new EndOfBucketException("Iterated over bucket but not found value");
    }

    private void addValue(int val) {
        synchronized (values) {
            if (values.containsKey(val)) {
                int count = values.get(val) + 1;
                values.put(val, count);
            } else {
                values.put(val, 1);
            }
            ++size;
        }

    }

    private void addValues(int[] arr) {
        synchronized (values) {
            for (int val : arr) {
                if (values.containsKey(val)) {
                    int count = values.get(val) + 1;
                    values.put(val, count);
                } else {
                    values.put(val, 1);
                }
                ++size;
            }
        }
    }

//    public static void main(String[] args) throws Exception {
//        int[] arr = new int[]{5, 6, 4, 6, 8, 9, 0, 5, 1, 5, 8, 0};
//        Bucket bucket = new Bucket(arr);
//        System.out.println(bucket.getSize());
//        bucket.show();
//        for (int i = 0; i < 12; ++i) {
//            System.out.println(bucket.getAt(i));
//        }
//        for (int i = 0; i < 11; ++i) {
//            System.out.println(Arrays.toString(bucket.get2ConsecutiveElements(i)));
//        }
//    }

}
