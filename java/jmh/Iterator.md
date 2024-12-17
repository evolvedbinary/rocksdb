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

Big run for plotting graphs (1)

Benchmark                             (bytesPerIteration)  (iteratorSequentialCacheSize)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score     Error  Units
IteratorBenchmarks.iteratorKeyScan                 524288                              0     1000000         16           64  thrpt    5   437.086 ±  17.928  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                              0     1000000         16          256  thrpt    5  1058.422 ±  69.151  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           1024     1000000         16           64  thrpt    5   636.144 ±  34.898  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           1024     1000000         16          256  thrpt    5  1263.731 ± 120.287  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           2048     1000000         16           64  thrpt    5   656.666 ±  11.286  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           2048     1000000         16          256  thrpt    5  1363.737 ±  33.461  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           4096     1000000         16           64  thrpt    5   653.071 ±  30.992  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           4096     1000000         16          256  thrpt    5  1400.973 ±  65.660  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           8192     1000000         16           64  thrpt    5   648.862 ±  19.610  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           8192     1000000         16          256  thrpt    5  1429.385 ±  48.132  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                          16384     1000000         16           64  thrpt    5   670.371 ±   8.791  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                          16384     1000000         16          256  thrpt    5  1400.808 ±  46.641  ops/s
IteratorBenchmarks.iteratorScan                    524288                              0     1000000         16           64  thrpt    5   724.134 ±   4.880  ops/s
IteratorBenchmarks.iteratorScan                    524288                              0     1000000         16          256  thrpt    5  1511.474 ±  10.634  ops/s
IteratorBenchmarks.iteratorScan                    524288                           1024     1000000         16           64  thrpt    5   674.269 ±   7.610  ops/s
IteratorBenchmarks.iteratorScan                    524288                           1024     1000000         16          256  thrpt    5  1339.343 ±  70.126  ops/s
IteratorBenchmarks.iteratorScan                    524288                           2048     1000000         16           64  thrpt    5   678.460 ±  14.174  ops/s
IteratorBenchmarks.iteratorScan                    524288                           2048     1000000         16          256  thrpt    5  1389.805 ±  26.266  ops/s
IteratorBenchmarks.iteratorScan                    524288                           4096     1000000         16           64  thrpt    5   684.343 ±  18.779  ops/s
IteratorBenchmarks.iteratorScan                    524288                           4096     1000000         16          256  thrpt    5  1432.842 ±  61.367  ops/s
IteratorBenchmarks.iteratorScan                    524288                           8192     1000000         16           64  thrpt    5   703.054 ±   9.595  ops/s
IteratorBenchmarks.iteratorScan                    524288                           8192     1000000         16          256  thrpt    5  1481.334 ±  12.948  ops/s
IteratorBenchmarks.iteratorScan                    524288                          16384     1000000         16           64  thrpt    5   707.681 ±   6.384  ops/s
IteratorBenchmarks.iteratorScan                    524288                          16384     1000000         16          256  thrpt    5  1521.669 ±  16.586  ops/s
IteratorBenchmarks.iteratorValueScan               524288                              0     1000000         16           64  thrpt    5   323.963 ±   0.757  ops/s
IteratorBenchmarks.iteratorValueScan               524288                              0     1000000         16          256  thrpt    5   860.052 ±   7.853  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           1024     1000000         16           64  thrpt    5   559.227 ±  10.407  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           1024     1000000         16          256  thrpt    5  1211.388 ± 205.236  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           2048     1000000         16           64  thrpt    5   572.644 ±  17.995  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           2048     1000000         16          256  thrpt    5  1295.739 ±  36.246  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           4096     1000000         16           64  thrpt    5   571.367 ±   8.464  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           4096     1000000         16          256  thrpt    5  1343.851 ±  68.257  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           8192     1000000         16           64  thrpt    5   584.271 ±  11.693  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           8192     1000000         16          256  thrpt    5  1340.749 ±  27.781  ops/s
IteratorBenchmarks.iteratorValueScan               524288                          16384     1000000         16           64  thrpt    5   582.278 ±  13.159  ops/s
IteratorBenchmarks.iteratorValueScan               524288                          16384     1000000         16          256  thrpt    5  1392.460 ±  53.395  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                              0     1000000         16           64  thrpt    5    42.765 ±   1.258  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                              0     1000000         16          256  thrpt    5   128.060 ±   1.349  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           1024     1000000         16           64  thrpt    5    46.926 ±   0.696  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           1024     1000000         16          256  thrpt    5   109.706 ±   2.772  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           2048     1000000         16           64  thrpt    5    17.345 ±  62.686  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           2048     1000000         16          256  thrpt    5   105.917 ±   1.797  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           4096     1000000         16           64  thrpt    5    45.571 ±   3.813  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           4096     1000000         16          256  thrpt    5   105.794 ±   1.869  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           8192     1000000         16           64  thrpt    5    44.675 ±   2.217  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           8192     1000000         16          256  thrpt    5   105.735 ±   3.692  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                          16384     1000000         16           64  thrpt    5    44.722 ±   1.938  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                          16384     1000000         16          256  thrpt    5   105.308 ±   7.401  ops/s

(2)

Benchmark                             (bytesPerIteration)  (iteratorSequentialCacheSize)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score     Error  Units
IteratorBenchmarks.iteratorKeyScan                 524288                           8192     1000000         16         1024  thrpt    5  2377.628 ± 103.695  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           8192     1000000         16         4096  thrpt    5  2839.913 ±  72.482  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                          16384     1000000         16         1024  thrpt    5  2395.876 ± 103.697  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                          16384     1000000         16         4096  thrpt    5  2979.958 ± 160.428  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                          32768     1000000         16         1024  thrpt    5  2485.218 ±  14.765  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                          32768     1000000         16         4096  thrpt    5  3108.851 ±  56.407  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                          65536     1000000         16         1024  thrpt    5  2495.453 ±  14.101  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                          65536     1000000         16         4096  thrpt    5  3012.627 ±  90.042  ops/s
IteratorBenchmarks.iteratorScan                    524288                           8192     1000000         16         1024  thrpt    5  2455.539 ±  19.108  ops/s
IteratorBenchmarks.iteratorScan                    524288                           8192     1000000         16         4096  thrpt    5  2637.400 ± 682.735  ops/s
IteratorBenchmarks.iteratorScan                    524288                          16384     1000000         16         1024  thrpt    5  2478.621 ± 191.403  ops/s
IteratorBenchmarks.iteratorScan                    524288                          16384     1000000         16         4096  thrpt    5  2992.625 ±  42.383  ops/s
IteratorBenchmarks.iteratorScan                    524288                          32768     1000000         16         1024  thrpt    5  2510.132 ± 122.727  ops/s
IteratorBenchmarks.iteratorScan                    524288                          32768     1000000         16         4096  thrpt    5  3094.405 ± 122.971  ops/s
IteratorBenchmarks.iteratorScan                    524288                          65536     1000000         16         1024  thrpt    5  2538.660 ±  41.222  ops/s
IteratorBenchmarks.iteratorScan                    524288                          65536     1000000         16         4096  thrpt    5  3025.813 ± 128.063  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           8192     1000000         16         1024  thrpt    5  2194.468 ±  48.949  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           8192     1000000         16         4096  thrpt    5  2548.124 ± 106.638  ops/s
IteratorBenchmarks.iteratorValueScan               524288                          16384     1000000         16         1024  thrpt    5  2153.207 ±  57.966  ops/s
IteratorBenchmarks.iteratorValueScan               524288                          16384     1000000         16         4096  thrpt    5  2478.803 ± 159.287  ops/s
IteratorBenchmarks.iteratorValueScan               524288                          32768     1000000         16         1024  thrpt    5  2058.824 ± 211.216  ops/s
IteratorBenchmarks.iteratorValueScan               524288                          32768     1000000         16         4096  thrpt    5  2274.168 ± 214.139  ops/s
IteratorBenchmarks.iteratorValueScan               524288                          65536     1000000         16         1024  thrpt    5  1497.731 ± 474.324  ops/s
IteratorBenchmarks.iteratorValueScan               524288                          65536     1000000         16         4096  thrpt    5  1873.123 ±  96.169  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           8192     1000000         16         1024  thrpt    5   255.470 ±  23.996  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           8192     1000000         16         4096  thrpt    5   776.593 ± 146.105  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                          16384     1000000         16         1024  thrpt    5   249.760 ±  12.725  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                          16384     1000000         16         4096  thrpt    5   714.954 ± 105.891  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                          32768     1000000         16         1024  thrpt    5   242.170 ±  11.280  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                          32768     1000000         16         4096  thrpt    5   727.126 ±  69.808  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                          65536     1000000         16         1024  thrpt    5   232.801 ±  17.505  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                          65536     1000000         16         4096  thrpt    5   661.661 ± 221.486  ops/s

(3)

Benchmark                             (bytesPerIteration)  (iteratorSequentialCacheSize)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score      Error  Units
IteratorBenchmarks.iteratorKeyScan                 524288                              0     1000000         16        16384  thrpt    5  6667.378 ±  121.443  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                          32768     1000000         16        16384  thrpt    5  6209.287 ±  109.432  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                          65536     1000000         16        16384  thrpt    5  6325.783 ±   61.160  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                         131072     1000000         16        16384  thrpt    5  6318.906 ±  659.320  ops/s
IteratorBenchmarks.iteratorScan                    524288                              0     1000000         16        16384  thrpt    5  6931.557 ±   79.533  ops/s
IteratorBenchmarks.iteratorScan                    524288                          32768     1000000         16        16384  thrpt    5  6215.769 ±  328.352  ops/s
IteratorBenchmarks.iteratorScan                    524288                          65536     1000000         16        16384  thrpt    5  5704.736 ± 3284.182  ops/s
IteratorBenchmarks.iteratorScan                    524288                         131072     1000000         16        16384  thrpt    5  6239.739 ±  147.977  ops/s
IteratorBenchmarks.iteratorValueScan               524288                              0     1000000         16        16384  thrpt    5  4901.072 ±  325.580  ops/s
IteratorBenchmarks.iteratorValueScan               524288                          32768     1000000         16        16384  thrpt    5  5137.492 ±  234.609  ops/s
IteratorBenchmarks.iteratorValueScan               524288                          65536     1000000         16        16384  thrpt    5  4839.555 ±   97.384  ops/s
IteratorBenchmarks.iteratorValueScan               524288                         131072     1000000         16        16384  thrpt    5  5109.995 ±  121.884  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                              0     1000000         16        16384  thrpt    5  3190.061 ±  223.838  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                          32768     1000000         16        16384  thrpt    5  3369.911 ±   27.666  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                          65536     1000000         16        16384  thrpt    5  3370.781 ±   30.410  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                         131072     1000000         16        16384  thrpt    5  3354.358 ±   14.785  ops/s

(4)

Benchmark                             (bytesPerIteration)  (iteratorSequentialCacheSize)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score     Error  Units
IteratorBenchmarks.iteratorKeyScan                 524288                              0     1000000         16         1024  thrpt    5  2171.570 ±  67.728  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                              0     1000000         16         4096  thrpt    5  2870.703 ± 111.355  ops/s
IteratorBenchmarks.iteratorScan                    524288                              0     1000000         16         1024  thrpt    5  2524.496 ±  96.988  ops/s
IteratorBenchmarks.iteratorScan                    524288                              0     1000000         16         4096  thrpt    5  3060.415 ±  41.743  ops/s
IteratorBenchmarks.iteratorValueScan               524288                              0     1000000         16         1024  thrpt    5  1758.537 ± 200.594  ops/s
IteratorBenchmarks.iteratorValueScan               524288                              0     1000000         16         4096  thrpt    5  2362.106 ± 136.769  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                              0     1000000         16         1024  thrpt    5   363.372 ±  27.031  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                              0     1000000         16         4096  thrpt    5  1159.309 ±  31.750  ops/s

(5)

Benchmark                             (bytesPerIteration)  (iteratorSequentialCacheSize)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score      Error  Units
IteratorBenchmarks.iteratorKeyScan                 524288                              0     1000000         16        16384  thrpt    5  6209.077 ± 1157.270  ops/s
IteratorBenchmarks.iteratorScan                    524288                              0     1000000         16        16384  thrpt    5  6605.644 ± 1258.432  ops/s
IteratorBenchmarks.iteratorValueScan               524288                              0     1000000         16        16384  thrpt    5  4996.238 ±  597.886  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                              0     1000000         16        16384  thrpt    5  3008.901 ±  258.860  ops/s

(6)

Benchmark                             (bytesPerIteration)  (iteratorSequentialCacheSize)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score     Error  Units
IteratorBenchmarks.iteratorKeyScan                 524288                           2048     1000000         16         1024  thrpt    5  2016.157 ±  97.373  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           4096     1000000         16         1024  thrpt    5  2221.005 ± 156.704  ops/s
IteratorBenchmarks.iteratorKeyScan                 524288                           2304     1000000         16         1024  thrpt    5  2178.403 ±  92.698  ops/s
IteratorBenchmarks.iteratorScan                    524288                           2048     1000000         16         1024  thrpt    5  2085.205 ± 166.958  ops/s
IteratorBenchmarks.iteratorScan                    524288                           4096     1000000         16         1024  thrpt    5  2333.096 ± 183.625  ops/s
IteratorBenchmarks.iteratorScan                    524288                           2304     1000000         16         1024  thrpt    5  2273.644 ± 102.096  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           2048     1000000         16         1024  thrpt    5  1946.234 ±  96.936  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           4096     1000000         16         1024  thrpt    5  2168.114 ±  71.405  ops/s
IteratorBenchmarks.iteratorValueScan               524288                           2304     1000000         16         1024  thrpt    5  2090.010 ±  20.078  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           2048     1000000         16         1024  thrpt    5   394.006 ±  38.644  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           4096     1000000         16         1024  thrpt    5   391.015 ±  23.612  ops/s
IteratorBenchmarks.iteratorValueSeek               524288                           2304     1000000         16         1024  thrpt    5   395.316 ±   5.663  ops/s



