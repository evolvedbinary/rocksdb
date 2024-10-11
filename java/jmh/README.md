# JMH Benchmarks for RocksJava

These are micro-benchmarks for RocksJava functionality, using [JMH (Java Microbenchmark Harness)](https://openjdk.java.net/projects/code-tools/jmh/).

## Compiling

**Note**: This uses a specific build of RocksDB that is set in the `<version>` element of the `dependencies` section of the `pom.xml` file. If you are testing local changes you should build and install a SNAPSHOT version of rocksdbjni, and update the `pom.xml` of rocksdbjni-jmh file to test with this.

For instance, this is how to install the OSX jar you just built for 9.7.0

```bash
$ mvn install:install-file -Dfile=./java/target/rocksdbjni-9.8.0-SNAPSHOT-osx.jar -DgroupId=org.rocksdb -DartifactId=rocksdbjni -Dversion=9.8.0-SNAPSHOT -Dpackaging=jar
```

```bash
$ mvn package
```

## Running
```bash
$ java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar
```

NOTE: you can append `-help` to the command above to see all of the JMH runtime options.

## Get Functionality

Baseline numbers for jmh tests, before changes:

```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar GetBenchmarks.get GetBenchmarks.preallocatedGet GetBenchmarks.preallocatedByteBufferGet PutBenchmarks.putByteBuffers -p columnFamilyTestType="no_column_family" -p keyCount=1000,100000 -p keySize=128 -p valueSize=4096,65536 bufferListSize=16,256
```

Benchmark                                (bufferListSize)  (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt       Score       Error  Units
GetBenchmarks.get                                     N/A        no_column_family        1000        128         4096  thrpt   25  822117.146 ±  4895.283  ops/s
GetBenchmarks.get                                     N/A        no_column_family        1000        128        65536  thrpt   25   77286.267 ±  3892.710  ops/s
GetBenchmarks.get                                     N/A        no_column_family      100000        128         4096  thrpt   25  244264.105 ±  3056.605  ops/s
GetBenchmarks.get                                     N/A        no_column_family      100000        128        65536  thrpt   25   72025.890 ±  3780.128  ops/s
GetBenchmarks.preallocatedByteBufferGet               N/A        no_column_family        1000        128         4096  thrpt   25  925785.181 ± 13614.194  ops/s
GetBenchmarks.preallocatedByteBufferGet               N/A        no_column_family        1000        128        65536  thrpt   25   87767.815 ±  3877.660  ops/s
GetBenchmarks.preallocatedByteBufferGet               N/A        no_column_family      100000        128         4096  thrpt   25  276576.324 ±  6306.692  ops/s
GetBenchmarks.preallocatedByteBufferGet               N/A        no_column_family      100000        128        65536  thrpt   25   78139.260 ±  2352.717  ops/s
GetBenchmarks.preallocatedGet                         N/A        no_column_family        1000        128         4096  thrpt   25  925073.527 ± 11924.587  ops/s
GetBenchmarks.preallocatedGet                         N/A        no_column_family        1000        128        65536  thrpt   25   84933.816 ±  2990.339  ops/s
GetBenchmarks.preallocatedGet                         N/A        no_column_family      100000        128         4096  thrpt   25  280870.039 ±  1374.643  ops/s
GetBenchmarks.preallocatedGet                         N/A        no_column_family      100000        128        65536  thrpt   25   78946.597 ±  2532.712  ops/s
PutBenchmarks.putByteBuffers                           16        no_column_family        1000        128         4096  thrpt   25   93115.662 ±  1381.303  ops/s
PutBenchmarks.putByteBuffers                           16        no_column_family        1000        128        65536  thrpt   25    7792.304 ±  1447.510  ops/s
PutBenchmarks.putByteBuffers                           16        no_column_family      100000        128         4096  thrpt   25   92017.278 ±  1357.971  ops/s
PutBenchmarks.putByteBuffers                           16        no_column_family      100000        128        65536  thrpt   25    8006.399 ±  1579.384  ops/s

Adding `critical` method implementation, after refactor to threaded jmh tests,

```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar GetBenchmarks.preallocatedGet GetBenchmarks.preallocatedGetCritical -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=65536 bufferListSize=256
```

Benchmark                                    (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt       Score      Error  Units
GetBenchmarks.preallocatedGet                      no_column_family        1000        128        65536  thrpt   25   90407.832 ± 1420.685  ops/s
GetBenchmarks.preallocatedGetCritical              no_column_family        1000        128        65536  thrpt   25   92791.082 ± 1956.233  ops/s
GetBenchmarks.preallocatedGetRandom                no_column_family        1000        128        65536  thrpt   25  175417.241 ± 4264.823  ops/s
GetBenchmarks.preallocatedGetRandomCritical        no_column_family        1000        128        65536  thrpt   25  156321.417 ± 1621.874  ops/s

Now try multi threaded (as above, but -t 8)
```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar GetBenchmarks.preallocated GetBenchmarks.preallocatedGetCritical -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=65536 bufferListSize=256 -t 8
```

Benchmark                                      (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt       Score       Error  Units
GetBenchmarks.preallocatedByteBufferGet              no_column_family        1000        128        65536  thrpt   25  318943.016 ±  4407.365  ops/s
GetBenchmarks.preallocatedByteBufferGetRandom        no_column_family        1000        128        65536  thrpt   25  608072.892 ± 13152.582  ops/s
GetBenchmarks.preallocatedGet                        no_column_family        1000        128        65536  thrpt   25  319422.862 ±  3693.119  ops/s
GetBenchmarks.preallocatedGetCritical                no_column_family        1000        128        65536  thrpt   25  318453.877 ±  2519.218  ops/s
GetBenchmarks.preallocatedGetRandom                  no_column_family        1000        128        65536  thrpt   25  612645.818 ±  6203.882  ops/s
GetBenchmarks.preallocatedGetRandomCritical          no_column_family        1000        128        65536  thrpt   25  606301.335 ±  5731.483  ops/s

Looks like minimal differences..

Try on ubuntu box; had to build without jemalloc as it was crapping out. From `lscpu`
```
Vendor ID:                          GenuineIntel
CPU family:                         6
Model:                              6
Model name:                         QEMU Virtual CPU version 2.5+
Stepping:                           3
CPU MHz:                            3491.912
BogoMIPS:                           6983.82
Hypervisor vendor:                  KVM
Virtualization type:                full
L1d cache:                          128 KiB
L1i cache:                          128 KiB
L2 cache:                           16 MiB
L3 cache:                           64 MiB
```
Do this:
```
DISABLE_JEMALLOC=1 make -j4
DISABLE_JEMALLOC=1 make -j4 rocksdbjava
```
...
```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar GetBenchmarks.preallocatedGet GetBenchmarks.preallocatedGetCritical -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=65536 bufferListSize=256
```

Benchmark                                    (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt      Score     Error  Units
GetBenchmarks.preallocatedGet                      no_column_family        1000        128        65536  thrpt   25  26371.815 ± 283.231  ops/s
GetBenchmarks.preallocatedGetCritical              no_column_family        1000        128        65536  thrpt   25  26446.532 ± 266.897  ops/s
GetBenchmarks.preallocatedGetRandom                no_column_family        1000        128        65536  thrpt   25  60889.568 ± 371.162  ops/s
GetBenchmarks.preallocatedGetRandomCritical        no_column_family        1000        128        65536  thrpt   25  60006.909 ± 542.036  ops/s

Multithreaded:

```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar GetBenchmarks.preallocatedGet GetBenchmarks.preallocatedGetCritical -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=65536 bufferListSize=256 -t 4
```

Benchmark                                    (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt       Score      Error  Units
GetBenchmarks.preallocatedGet                      no_column_family        1000        128        65536  thrpt   25  118521.898 ±  814.669  ops/s
GetBenchmarks.preallocatedGetCritical              no_column_family        1000        128        65536  thrpt   25  118564.242 ± 1110.357  ops/s
GetBenchmarks.preallocatedGetRandom                no_column_family        1000        128        65536  thrpt   25  196274.073 ± 4162.104  ops/s
GetBenchmarks.preallocatedGetRandomCritical        no_column_family        1000        128        65536  thrpt   25  194944.334 ± 5256.324  ops/s

## Put Functionality

```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar putCritical putByteArrays -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=65536
```
before threaded refactor

Benchmark                            (bufferListSize)  (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score      Error  Units
PutBenchmarks.putByteArrays                        16        no_column_family        1000        128        65536  thrpt   25  6766.549 ± 1411.906  ops/s
PutBenchmarks.putCriticalByteArrays                16        no_column_family        1000        128        65536  thrpt   25  6601.626 ± 1651.342  ops/s

Same incantation, threaded refactor (make explicit the implicit `-t 1`) - should be same results as above.
```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar putCritical putByteArrays -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=65536 -t 1
```

Benchmark                            (bufferListSize)  (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score      Error  Units
PutBenchmarks.putByteArrays                        16        no_column_family        1000        128        65536  thrpt   25  6298.227 ± 1799.921  ops/s
PutBenchmarks.putCriticalByteArrays                16        no_column_family        1000        128        65536  thrpt   25  6116.060 ± 1491.642  ops/s

Within margin of error, anyway. But that is a big error bar. We should try a smaller k/v size.

Benchmark                            (bufferListSize)  (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt     Score      Error  Units
PutBenchmarks.putByteArrays                        16        no_column_family        1000        128        65536  thrpt   25  6712.957 ± 1214.746  ops/s
PutBenchmarks.putCriticalByteArrays                16        no_column_family        1000        128        65536  thrpt   25  6203.038 ± 1745.341  ops/s

No improvement... try with smaller sizes, and maybe `-t 4` ?

```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar putCritical putByteArrays -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=4096 -t 4
```

Benchmark                            (bufferListSize)  (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt      Score      Error  Units
PutBenchmarks.putByteArrays                        16        no_column_family        1000        128         4096  thrpt   25  57069.556 ± 6661.129  ops/s
PutBenchmarks.putCriticalByteArrays                16        no_column_family        1000        128         4096  thrpt   25  58576.759 ± 6923.890  ops/s

and vary the number of threads..

```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar putCritical putByteArrays -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=4096 -t 2
```

Benchmark                            (bufferListSize)  (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt      Score      Error  Units
PutBenchmarks.putByteArrays                        16        no_column_family        1000        128         4096  thrpt   25  57449.449 ± 4772.881  ops/s
PutBenchmarks.putCriticalByteArrays                16        no_column_family        1000        128         4096  thrpt   25  58895.204 ± 3275.122  ops/s

But are those Like vs Like ? Do some bigger runs:
```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar PutBenchmarks -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=4096 -t 1
```

Benchmark                            (bufferListSize)  (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt      Score       Error  Units
PutBenchmarks.put                                  16        no_column_family        1000        128         4096  thrpt   25  46126.736 ± 10717.984  ops/s
PutBenchmarks.putByteArrays                        16        no_column_family        1000        128         4096  thrpt   25  38029.538 ±  5177.602  ops/s
PutBenchmarks.putByteBuffers                       16        no_column_family        1000        128         4096  thrpt   25  39138.315 ±  5065.749  ops/s
PutBenchmarks.putCritical                          16        no_column_family        1000        128         4096  thrpt   25  40407.900 ±  4236.854  ops/s
PutBenchmarks.putCriticalByteArrays                16        no_column_family        1000        128         4096  thrpt   25  39461.806 ±  5161.390  ops/s

```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar PutBenchmarks -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=4096 -t 4
```

Benchmark                            (bufferListSize)  (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt      Score      Error  Units
PutBenchmarks.put                                  16        no_column_family        1000        128         4096  thrpt   25  38142.975 ± 4993.479  ops/s
PutBenchmarks.putByteArrays                        16        no_column_family        1000        128         4096  thrpt   25  37255.331 ± 5108.133  ops/s
PutBenchmarks.putByteBuffers                       16        no_column_family        1000        128         4096  thrpt   25  37730.824 ± 4715.435  ops/s
PutBenchmarks.putCritical                          16        no_column_family        1000        128         4096  thrpt   25  38027.659 ± 4587.852  ops/s
PutBenchmarks.putCriticalByteArrays                16        no_column_family        1000        128         4096  thrpt   25  76469.730 ± 7903.579  ops/s

That is strange for `putCriticalByteArrays` - check the code is not broken.

```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar PutBenchmarks -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=4096 -t 2
```

Benchmark                            (bufferListSize)  (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt      Score      Error  Units
PutBenchmarks.put                                  16        no_column_family        1000        128         4096  thrpt   25  71708.576 ± 3811.488  ops/s
PutBenchmarks.putByteArrays                        16        no_column_family        1000        128         4096  thrpt   25  73479.303 ± 1703.478  ops/s
PutBenchmarks.putByteBuffers                       16        no_column_family        1000        128         4096  thrpt   25  72794.596 ± 2078.256  ops/s
PutBenchmarks.putCritical                          16        no_column_family        1000        128         4096  thrpt   25  73582.245 ± 2222.525  ops/s
PutBenchmarks.putCriticalByteArrays                16        no_column_family        1000        128         4096  thrpt   25  73296.812 ± 2138.232  ops/s

Curioser and curioser. `4` seems broken.
```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar PutBenchmarks -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=4096 -t 8
```

Benchmark                            (bufferListSize)  (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt      Score      Error  Units
PutBenchmarks.put                                  16        no_column_family        1000        128         4096  thrpt   25  76879.783 ±  946.790  ops/s
PutBenchmarks.putByteArrays                        16        no_column_family        1000        128         4096  thrpt   25  76941.336 ± 1512.668  ops/s
PutBenchmarks.putByteBuffers                       16        no_column_family        1000        128         4096  thrpt   25  76978.087 ± 1492.692  ops/s
PutBenchmarks.putCritical                          16        no_column_family        1000        128         4096  thrpt   25  78736.512 ± 1187.041  ops/s
PutBenchmarks.putCriticalByteArrays                16        no_column_family        1000        128         4096  thrpt   25  76832.689 ± 1084.203  ops/s

Retried the 4x threads version; this one seems more believable. But still very hard to interpret.
```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar PutBenchmarks -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=4096 -t 4
```

Benchmark                            (bufferListSize)  (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt      Score      Error  Units
PutBenchmarks.put                                  16        no_column_family        1000        128         4096  thrpt   25  85390.061 ± 2097.887  ops/s
PutBenchmarks.putByteArrays                        16        no_column_family        1000        128         4096  thrpt   25  81344.588 ± 3760.936  ops/s
PutBenchmarks.putByteBuffers                       16        no_column_family        1000        128         4096  thrpt   25  78762.476 ± 1287.871  ops/s
PutBenchmarks.putCritical                          16        no_column_family        1000        128         4096  thrpt   25  75680.153 ± 3412.451  ops/s
PutBenchmarks.putCriticalByteArrays                16        no_column_family        1000        128         4096  thrpt   25  82381.473 ± 3163.506  ops/s

Fixed some issues with `get()`; added possibly relevant blockholes and fixed a case where the key values might overlay into something unplanned.
Why does random work so much faster ?

```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar GetBenchmarks.preallocated GetBenchmarks.preallocatedGetCritical -p columnFamilyTestType="no_column_family" -p keyCount=1000 -p keySize=128 -p valueSize=65536 bufferListSize=256 -t 8
```

Benchmark                                      (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt       Score      Error  Units
GetBenchmarks.preallocatedByteBufferGet              no_column_family        1000        128        65536  thrpt   25  349054.481 ± 4022.765  ops/s
GetBenchmarks.preallocatedByteBufferGetRandom        no_column_family        1000        128        65536  thrpt   25  592513.805 ± 9067.836  ops/s
GetBenchmarks.preallocatedGet                        no_column_family        1000        128        65536  thrpt   25  337448.211 ± 1675.154  ops/s
GetBenchmarks.preallocatedGetCritical                no_column_family        1000        128        65536  thrpt   25  336112.920 ± 1513.136  ops/s
GetBenchmarks.preallocatedGetRandom                  no_column_family        1000        128        65536  thrpt   25  607826.739 ± 3398.069  ops/s
GetBenchmarks.preallocatedGetRandomCritical          no_column_family        1000        128        65536  thrpt   25  602986.432 ± 4028.814  ops/s
