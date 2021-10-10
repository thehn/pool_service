## Requirements analysis. Data structures & system architecture design

### Requirements-based implementation

1. Pools need to access and update by `poolId` as quickly as possible, and these operations are concurrent. So
   ConcurrentHashMap could be a proper data structure (access / search by key: O(1)).
2. To create data structure to store pool values, we need to take these criteria into account: <br />
    1. Pool values keep update frequently, especially the pool size is not limited, so the pool capacity must be easily
       extendable.
    2. Performance of inserting or appending new values to pool has the highest priority.
    3. To calculate the quantile, the pool values must be sorted. Sorting a big collection is very expensive, so it
       would be better to maintain a pre-sorted one.
       <br />
       For the above reasons, a pool value should be stored in buckets. Each bucket is a Red-Black-Tree implementation
       which is a self-balancing binary search tree. AVL Tree could be an option, but AVL trees store the balance factor
       at each node. This takes O(N) extra space. For an insert intensive tasks, Red-Black tree is winner. <br />
       When a value is appended to a pool, it will be inserted to one bucket by its value. The insertion time complexity
       is O(logN), but we have all buckets sorted. <br />
       In my opinions, using independent buckets storage it can not only leverage parallel processing where it can
       reduce a lot of computation time, but also it can partitioning to other node if the workload is not fit in memory
       of a single node.
3. Quantile calculation is not cheap, it needs to cache the calculated values in memory to improve the UX.

### High availability, scalability & resiliency.

1. Database <br />
    - It requires semi-structured data, no relational at all => we can use NoSQL DB to achieve scalability, high
      availability and high performance over SQL Database. <br />
    - To reduce data access latency and I/O load => use in-memory cache like Redis or Memcached.
    - We can also use Redis as primary database because besides using as an in-memory caching service, it can be
      configured to persist data to persistent storage like files or other databases such as MongoDB, PostgresDB ...
    - It has data replication, partitioning
    - Redis is single threaded => atomic operations (atomicity) are provided in natural way without any overhead or
      extra cost of synchronization). <br />

2. Make use of Kubernetes services <br />
   - Our program is stateless, very easy to deploy to Kubernetes to utilize load balancer and many other services (
  quick deployment, health check, auto-scaling, auto start-over ...) => Resiliency improved !

#### This architecture can turn the application into a cloud service without much effort.

### Consideration.

    Can redis single threaded become a bottleneck issue?
    https://stackoverflow.com/questions/49304856/how-redis-deal-with-1000-requests-in-concurrency/49346017
    https://stackoverflow.com/questions/10489298/redis-is-single-threaded-then-how-does-it-do-concurrent-i-o
    "It's not very frequent that CPU becomes your bottleneck with Redis, as usually Redis is either memory or
    network bound. For instance, using pipelining Redis running on an average Linux system can deliver even 1 million
    requests per second, so if your application mainly uses O(N) or O(log(N)) commands, it is hardly going to use too
    much CPU"

### Future researches.

- How to update quantile efficiently: Consecutively calculate quantile without cache clearance
- Multiple instances of Redis & treating them as different servers

-------------

Please kindly analyze, evaluate and if it is possible, I would appreciate if you could provide your feedback on my
deliverables.