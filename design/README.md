## Requirements analysis. Data structures & system architecture design

### Requirements-based analysis

1. Pools need to access and update by `poolId` as quickly as possible, and these operations also need to be concurrent. 
   Therefore `ConcurrentHashMap` could be a appropriate data structure (access / search by key: `O(1)`).
2. To create data structure to store pool values, we need to take these criteria into account: <br>
    - Pool values keep updating frequently, especially the pool size is not limited. 
      So the program should be able to update pool value efficiently and pool capacity must be easily
       extendable.
    - Performance of inserting or appending new values to pool should be considered with the highest priority.
    - To calculate the quantile, the pool values must be sorted. Sorting a big collection is very expensive and time-consuming, so it
       would be better to maintain a pre-sorted one.

### Design & Implementation

#### Pool data structures
For the above analyzed of requirements, I decided to choose buckets as a main data structure of pool values storage. 

- A pool values will be stored into a hashmap of buckets with key is bucket id and value is a bucket instance. 
- Each bucket is constructed by <mark>Red-Black-Tree</mark> implementation which is a <mark><b>self-balancing binary search tree</b></mark>. The <b>RBTree</b> can not only preserve the sorted state, and more important it can also provide most common data structure operations with only `O(logN)` in time complexity and `O(N)` in space complexity. (See [Big O Cheat Sheet](https://www.bigocheatsheet.com/) )
- AVL Tree could be an option, but AVL trees store the balance factor at each node. This takes `O(N)` extra space. 
For an insert intensive tasks, Red-Black tree is winner.
- When a value is appended to a pool, it will be inserted to one bucket by its value. 
The insertion time complexity is O(logN), but we have all buckets sorted.  
- Each node of bucket is actually a map entry. Each entry is a pair of `<key,value>` 
where `key` and `value` represent for an unique `poolValue` and number of its occurrences in the pool respectively.  
Thanks to this implementation, the number of iteration needs to traverse a pool is always less than or equal the pool size.
It equals to number of unique values in pool.

In my opinions, using independent buckets to store data can not only leverage parallel processing where it can reduce computation time,
but also it can partitioning to other node if the workload is not fit in memory of a single node.

#### Quantile calculation

In most case, quantile calculation needs to access 2 consecutive elements of a pool values. 
With current implementation, accessing to an element at index `i-th` takes `O(k + B)` where `k` = id of bucket containing `i-th` element and `B` is that bucket size.  
To reduce the accessing time, I implemented an extension of `get(int index)`, 
it is `get2ConsecutiveElements(int index)` where it can return `i-th` and `i-th + 1` in one single access.

Quantile calculation is not cheap, it is better to cache the calculated values in memory to improve the UX.

#### Time complexity
Let assume N is total values (to insert/append to pool), B is bucket capacity, U is number of unique values in a bucket (U <= B <= N),
then the time complexity of each operation can be estimated as below:

- Insert: `O(N log B)`
- Append: `O(N log B)`
- Access: `O(U)`
- Quantile calculation: `O(U)`


### High availability, scalability & resiliency.

#### Using Redis as main caching service and primary database <br />
- It requires semi-structured data, no relationship at all => we can use NoSQL DB to achieve scalability, high
  availability and high performance over SQL Database. <br />
- To reduce data access latency and I/O load => use in-memory cache like Redis or Memcached.
- We can also use Redis as primary database because besides using as an in-memory caching service, it can be
  configured to persist data to persistent storage like files or other databases such as MongoDB, PostgresDB ...
- It has data replication, partitioning
- Redis is single threaded => atomic operations (atomicity) are provided in natural way without any overhead or
  extra cost of synchronization). <br />

#### Make use of Kubernetes cluster services <br />

Our program is stateless, very easy to deploy to Kubernetes to utilize load balancer and many other services (
quick deployment, health check, auto-scaling, auto start-over ...)  


<b><i> By using using Redis and Kubernetes cluster, our serivce can achieve all high availability, scalability & resiliency.  
   This architecture can also turn the application into a cloud service without much effort </i></b>

### Consideration.

* [Can redis single threaded become a bottleneck issue?](https://stackoverflow.com/questions/49304856/how-redis-deal-with-1000-requests-in-concurrency/49346017)

> "It's not very frequent that CPU becomes your bottleneck with Redis, as usually Redis is either memory or network bound. For instance, using pipelining Redis running on an average Linux system can deliver even 1 million requests per second, so if your application mainly uses O(N) or O(log(N)) commands, it is hardly going to use too much CPU"

* [Redis is single threaded, but how does it do concurrent?](https://stackoverflow.com/questions/10489298/redis-is-single-threaded-then-how-does-it-do-concurrent-i-o)

### Future researches.

- How to update quantile efficiently: Consecutively calculate quantile without cache clearance
- Multiple instances of Redis & treating them as different servers

-------------

Please kindly analyze, evaluate and if it is possible, I would appreciate if you could provide your feedback on my
deliverables.
