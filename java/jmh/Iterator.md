# Results

## Current Implementation

```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar IteratorBenchmarks -p keySize="16" -p valueSize="64","1024" -p keyCount="10000","1000000
```

Benchmark                             (bytesPerIteration)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score     Error  Units
IteratorBenchmarks.iteratorKeyScan                 524288       10000         16           64  thrpt    5   526.255 ±  12.616  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288       10000         16         1024  thrpt    5  5652.495 ± 510.005  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288     1000000         16           64  thrpt    5   438.088 ±  17.073  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288     1000000         16         1024  thrpt    5  2086.086 ± 193.404  ops/s
IteratorBenchmarks.iteratorScan                    524288       10000         16           64  thrpt    5   944.886 ±  23.248  ops/s
IteratorBenchmarks.iteratorScan                    524288       10000         16         1024  thrpt    5  9175.818 ± 852.137  ops/s
IteratorBenchmarks.iteratorScan                    524288     1000000         16           64  thrpt    5   715.450 ±  20.996  ops/s
IteratorBenchmarks.iteratorScan                    524288     1000000         16         1024  thrpt    5  2523.650 ± 114.243  ops/s
IteratorBenchmarks.iteratorValueScan               524288       10000         16           64  thrpt    5   367.063 ±   6.271  ops/s
IteratorBenchmarks.iteratorValueScan               524288       10000         16         1024  thrpt    5  4022.381 ±  10.224  ops/s
IteratorBenchmarks.iteratorValueScan               524288     1000000         16           64  thrpt    5   319.660 ±   1.474  ops/s
IteratorBenchmarks.iteratorValueScan               524288     1000000         16         1024  thrpt    5  1800.656 ±  21.449  ops/s
IteratorBenchmarks.iteratorValueSeek               524288       10000         16           64  thrpt    5   102.818 ±   1.384  ops/s
IteratorBenchmarks.iteratorValueSeek               524288       10000         16         1024  thrpt    5  1316.241 ±   9.982  ops/s
IteratorBenchmarks.iteratorValueSeek               524288     1000000         16           64  thrpt    5    42.290 ±   0.292  ops/s
IteratorBenchmarks.iteratorValueSeek               524288     1000000         16         1024  thrpt    5   358.978 ±   8.323  ops/s

Benchmark                             (bytesPerIteration)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score     Error  Units
IteratorBenchmarks.iteratorKeyScan                 524288       10000         16           64  thrpt    5   450.626 ±   1.337  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288       10000         16         1024  thrpt    5  5026.139 ± 147.923  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288     1000000         16           64  thrpt    5   383.615 ±   3.294  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288     1000000         16         1024  thrpt    5  2050.581 ± 101.891  ops/s
IteratorBenchmarks.iteratorScan                    524288       10000         16           64  thrpt    5   451.510 ±   2.523  ops/s
IteratorBenchmarks.iteratorScan                    524288       10000         16         1024  thrpt    5  4919.945 ±  62.091  ops/s
IteratorBenchmarks.iteratorScan                    524288     1000000         16           64  thrpt    5   375.768 ±  22.891  ops/s
IteratorBenchmarks.iteratorScan                    524288     1000000         16         1024  thrpt    5  2055.013 ±  18.676  ops/s
IteratorBenchmarks.iteratorValueScan               524288       10000         16           64  thrpt    5   324.237 ±   0.738  ops/s
IteratorBenchmarks.iteratorValueScan               524288       10000         16         1024  thrpt    5  3604.156 ±   8.090  ops/s
IteratorBenchmarks.iteratorValueScan               524288     1000000         16           64  thrpt    5   288.765 ±   0.940  ops/s
IteratorBenchmarks.iteratorValueScan               524288     1000000         16         1024  thrpt    5  1712.433 ±  13.794  ops/s
IteratorBenchmarks.iteratorValueSeek               524288       10000         16           64  thrpt    5   102.069 ±   2.327  ops/s
IteratorBenchmarks.iteratorValueSeek               524288       10000         16         1024  thrpt    5  1281.491 ±  32.292  ops/s
IteratorBenchmarks.iteratorValueSeek               524288     1000000         16           64  thrpt    5    39.346 ±   6.037  ops/s
IteratorBenchmarks.iteratorValueSeek               524288     1000000         16         1024  thrpt    5   343.079 ±  33.315  ops/s


