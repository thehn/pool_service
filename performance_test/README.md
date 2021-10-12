# Performance test

### A simple performance test was performed as below:

1. Input workload. Array (pool) size: 1 millions Value range: 0~10 millions
2. Test results (on average):
    1. Insertion and Append took 686 ms
    2. Quantile calculation took 10.05 ms at very first time. <br />
       Since second time thanks to caching technique, it took 5ms on average.
