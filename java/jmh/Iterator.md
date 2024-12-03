# Results

## Current Implementation

Benchmark                             (keyCount)  (keySize)  (readBytesPerBatch)  (valueSize)   Mode  Cnt     Score     Error  Units
IteratorBenchmarks.iteratorKeyScan         10000         16               524288           64  thrpt    5   524.908 ±  13.435  ops/s
IteratorBenchmarks.iteratorKeyScan         10000         16               524288         1024  thrpt    5  5803.964 ±  45.107  ops/s
IteratorBenchmarks.iteratorKeyScan       1000000         16               524288           64  thrpt    5   437.161 ±   3.106  ops/s
IteratorBenchmarks.iteratorKeyScan       1000000         16               524288         1024  thrpt    5  2162.167 ±  73.307  ops/s
IteratorBenchmarks.iteratorScan            10000         16               524288           64  thrpt    5   942.340 ±  26.454  ops/s
IteratorBenchmarks.iteratorScan            10000         16               524288         1024  thrpt    5  9451.685 ± 661.875  ops/s
IteratorBenchmarks.iteratorScan          1000000         16               524288           64  thrpt    5   708.997 ±  29.603  ops/s
IteratorBenchmarks.iteratorScan          1000000         16               524288         1024  thrpt    5  2531.501 ± 170.458  ops/s
IteratorBenchmarks.iteratorValueScan       10000         16               524288           64  thrpt    5   363.825 ±  10.251  ops/s
IteratorBenchmarks.iteratorValueScan       10000         16               524288         1024  thrpt    5  4019.556 ±  77.641  ops/s
IteratorBenchmarks.iteratorValueScan     1000000         16               524288           64  thrpt    5   317.747 ±  23.718  ops/s
IteratorBenchmarks.iteratorValueScan     1000000         16               524288         1024  thrpt    5  1739.230 ± 108.017  ops/s
