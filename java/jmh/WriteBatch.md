# Results
## Current Implementation

Benchmark                           (keyCount)  (keySize)  (valueSize)  (writeBatchAllocation)  (writeBytesPerBatch)  (writeToDB)   Mode  Cnt      Score      Error  Units
WriteBatchBenchmarks.putWriteBatch   100000000         16           32                 1048576                524288        false  thrpt    5    183.888 ±   14.306  ops/s
WriteBatchBenchmarks.putWriteBatch   100000000         16           64                 1048576                524288        false  thrpt    5    303.095 ±   27.853  ops/s
WriteBatchBenchmarks.putWriteBatch   100000000         16          128                 1048576                524288        false  thrpt    5    537.497 ±   55.410  ops/s
WriteBatchBenchmarks.putWriteBatch   100000000         16          256                 1048576                524288        false  thrpt    5    947.626 ±    5.697  ops/s
WriteBatchBenchmarks.putWriteBatch   100000000         16          512                 1048576                524288        false  thrpt    5   1921.478 ±  111.597  ops/s
WriteBatchBenchmarks.putWriteBatch   100000000         16         1024                 1048576                524288        false  thrpt    5   3393.499 ±   77.845  ops/s
WriteBatchBenchmarks.putWriteBatch   100000000         16         2048                 1048576                524288        false  thrpt    5   6539.973 ±  188.373  ops/s
WriteBatchBenchmarks.putWriteBatch   100000000         16         4096                 1048576                524288        false  thrpt    5  10469.043 ±  316.770  ops/s
WriteBatchBenchmarks.putWriteBatch   100000000         16         8192                 1048576                524288        false  thrpt    5  19266.853 ± 2447.249  ops/s
WriteBatchBenchmarks.putWriteBatch   100000000         16        16384                 1048576                524288        false  thrpt    5  25524.471 ± 3618.922  ops/s
WriteBatchBenchmarks.putWriteBatch   100000000         16        32768                 1048576                524288        false  thrpt    5  33861.138 ± 9831.394  ops/s
WriteBatchBenchmarks.putWriteBatch   100000000         16        65536                 1048576                524288        false  thrpt    5  24996.840 ± 5355.147  ops/s

## Buffered Put Requests


