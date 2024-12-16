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

## Prefetch Buffer

Hard coded to 4K prefetch buffer size; the prototype fills the buffer with (k,v) pairs if a current buffer does not exist, or is exhausted,
whenever `isValid()` is called or `seekToFirst()` is called. The latter is *just* an optimization, but can probably be used in general.
```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar IteratorBenchmarks -p keySize="16" -p valueSize="64","1024" -p keyCount="10000","1000000
```
Benchmark                             (bytesPerIteration)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score     Error  Units
IteratorBenchmarks.iteratorKeyScan                 524288       10000         16           64  thrpt    5   858.904 ±  38.678  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288       10000         16         1024  thrpt    5  7406.196 ± 238.905  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288     1000000         16           64  thrpt    5   653.191 ±  30.103  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288     1000000         16         1024  thrpt    5  2291.894 ± 161.647  ops/s
IteratorBenchmarks.iteratorScan                    524288       10000         16           64  thrpt    5   910.780 ±  38.874  ops/s
IteratorBenchmarks.iteratorScan                    524288       10000         16         1024  thrpt    5  7508.076 ±  79.459  ops/s
IteratorBenchmarks.iteratorScan                    524288     1000000         16           64  thrpt    5   681.176 ±  22.412  ops/s
IteratorBenchmarks.iteratorScan                    524288     1000000         16         1024  thrpt    5  2328.703 ±  55.765  ops/s
IteratorBenchmarks.iteratorValueScan               524288       10000         16           64  thrpt    5   712.922 ±  16.278  ops/s
IteratorBenchmarks.iteratorValueScan               524288       10000         16         1024  thrpt    5  6220.670 ±  30.919  ops/s
IteratorBenchmarks.iteratorValueScan               524288     1000000         16           64  thrpt    5   560.444 ±  65.903  ops/s
IteratorBenchmarks.iteratorValueScan               524288     1000000         16         1024  thrpt    5  2108.495 ±  71.026  ops/s
IteratorBenchmarks.iteratorValueSeek               524288       10000         16           64  thrpt    5   120.347 ±   3.400  ops/s
IteratorBenchmarks.iteratorValueSeek               524288       10000         16         1024  thrpt    5  1736.110 ± 283.765  ops/s
IteratorBenchmarks.iteratorValueSeek               524288     1000000         16           64  thrpt    5    47.495 ±   2.019  ops/s
IteratorBenchmarks.iteratorValueSeek               524288     1000000         16         1024  thrpt    5   392.246 ±   9.930  ops/s

So, it is clear that we can make performance a lot better with the prefetch.
I will re-run with a bigger batch (16384). This can be configured into the test.

Benchmark                             (bytesPerIteration)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score     Error  Units
IteratorBenchmarks.iteratorKeyScan                 524288       10000         16           64  thrpt    5   833.313 ±  10.854  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288       10000         16         1024  thrpt    5  8213.562 ±  45.692  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288     1000000         16           64  thrpt    5   632.933 ±  19.567  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288     1000000         16         1024  thrpt    5  2325.860 ± 208.356  ops/s
IteratorBenchmarks.iteratorScan                    524288       10000         16           64  thrpt    5   915.513 ±  23.271  ops/s
IteratorBenchmarks.iteratorScan                    524288       10000         16         1024  thrpt    5  8586.596 ± 358.772  ops/s
IteratorBenchmarks.iteratorScan                    524288     1000000         16           64  thrpt    5   670.497 ±  24.691  ops/s
IteratorBenchmarks.iteratorScan                    524288     1000000         16         1024  thrpt    5  2503.754 ±  37.481  ops/s
IteratorBenchmarks.iteratorValueScan               524288       10000         16           64  thrpt    5   736.573 ±   4.972  ops/s
IteratorBenchmarks.iteratorValueScan               524288       10000         16         1024  thrpt    5  6986.401 ± 379.970  ops/s
IteratorBenchmarks.iteratorValueScan               524288     1000000         16           64  thrpt    5   576.508 ±  22.535  ops/s
IteratorBenchmarks.iteratorValueScan               524288     1000000         16         1024  thrpt    5  2139.957 ±  97.899  ops/s
IteratorBenchmarks.iteratorValueSeek               524288       10000         16           64  thrpt    5   120.708 ±   1.550  ops/s
IteratorBenchmarks.iteratorValueSeek               524288       10000         16         1024  thrpt    5  1826.593 ±  70.991  ops/s
IteratorBenchmarks.iteratorValueSeek               524288     1000000         16           64  thrpt    5    44.350 ±   2.458  ops/s
IteratorBenchmarks.iteratorValueSeek               524288     1000000         16         1024  thrpt    5   376.191 ±  32.057  ops/s

Updated to handle both cache and uncached, depending on the configuration of `iteratorSequentialCacheSize`
* 0 means don't cache
* 4096 means use cache size of 4096
Points to note are that performance of the non-cached version is similar to before,
while performance of the cached version is generally better.
```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar IteratorBenchmarks -p keySize="16" -p valueSize="64","1024" -p keyCount="10000","1000000"
```

Benchmark                             (bytesPerIteration)  (iteratorSequentialCacheSize)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score     Error  Units
IteratorBenchmarks.iteratorKeyScan                 524288                              0       10000         16           64  thrpt    5   521.717 ±   8.935  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                              0       10000         16         1024  thrpt    5  5676.672 ±  82.262  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                              0     1000000         16           64  thrpt    5   426.696 ±  24.140  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                              0     1000000         16         1024  thrpt    5  2125.271 ±  15.869  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           4096       10000         16           64  thrpt    5   874.140 ±   1.701  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           4096       10000         16         1024  thrpt    5  7320.275 ±  13.117  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           4096     1000000         16           64  thrpt    5   660.608 ±   8.666  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           4096     1000000         16         1024  thrpt    5  2314.944 ±  36.040  ops/s
IteratorBenchmarks.iteratorScan                    524288                              0       10000         16           64  thrpt    5   962.996 ±   5.099  ops/s
IteratorBenchmarks.iteratorScan                    524288                              0       10000         16         1024  thrpt    5  9646.594 ± 234.583  ops/s
IteratorBenchmarks.iteratorScan                    524288                              0     1000000         16           64  thrpt    5   715.867 ±   9.472  ops/s
IteratorBenchmarks.iteratorScan                    524288                              0     1000000         16         1024  thrpt    5  2480.759 ± 119.447  ops/s
IteratorBenchmarks.iteratorScan                    524288                           4096       10000         16           64  thrpt    5   908.529 ±   7.248  ops/s
IteratorBenchmarks.iteratorScan                    524288                           4096       10000         16         1024  thrpt    5  7435.856 ± 138.763  ops/s
IteratorBenchmarks.iteratorScan                    524288                           4096     1000000         16           64  thrpt    5   687.512 ±   6.512  ops/s
IteratorBenchmarks.iteratorScan                    524288                           4096     1000000         16         1024  thrpt    5  2344.236 ±  16.391  ops/s
IteratorBenchmarks.iteratorValueScan               524288                              0       10000         16           64  thrpt    5   367.525 ±   1.949  ops/s
IteratorBenchmarks.iteratorValueScan               524288                              0       10000         16         1024  thrpt    5  3976.520 ± 136.696  ops/s
IteratorBenchmarks.iteratorValueScan               524288                              0     1000000         16           64  thrpt    5   318.168 ±   8.107  ops/s
IteratorBenchmarks.iteratorValueScan               524288                              0     1000000         16         1024  thrpt    5  1813.172 ±  17.528  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           4096       10000         16           64  thrpt    5   701.360 ±  10.584  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           4096       10000         16         1024  thrpt    5  6158.622 ± 125.184  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           4096     1000000         16           64  thrpt    5   568.317 ±   3.157  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           4096     1000000         16         1024  thrpt    5  2107.584 ±  64.368  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                              0       10000         16           64  thrpt    5   103.140 ±   1.355  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                              0       10000         16         1024  thrpt    5  1298.181 ±  15.908  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                              0     1000000         16           64  thrpt    5    42.003 ±   2.650  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                              0     1000000         16         1024  thrpt    5   348.983 ± 115.855  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           4096       10000         16           64  thrpt    5   125.093 ±   1.624  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           4096       10000         16         1024  thrpt    5  1884.667 ±  36.595  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           4096     1000000         16           64  thrpt    5    46.797 ±   0.403  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           4096     1000000         16         1024  thrpt    5   395.710 ±   6.830  ops/s




