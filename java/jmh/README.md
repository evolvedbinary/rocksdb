# JMH Benchmarks for RocksJava

These are micro-benchmarks for RocksJava functionality, using [JMH (Java Microbenchmark Harness)](https://openjdk.java.net/projects/code-tools/jmh/).

## Compiling

**Note**: This uses a specific build of RocksDB that is set in the `<version>` element of the `dependencies` section of the `pom.xml` file. If you are testing local changes you should build and install a SNAPSHOT version of rocksdbjni, and update the `pom.xml` of rocksdbjni-jmh file to test with this.

For instance, this is how to install the OSX jar you just built for 9.7.0

```bash
$ mvn install:install-file -Dfile=./java/target/rocksdbjni-9.7.0-SNAPSHOT-osx.jar -DgroupId=org.rocksdb -DartifactId=rocksdbjni -Dversion=9.7.0-SNAPSHOT -Dpackaging=jar
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

Try on ubuntu box; had to build without jemalloc as it was crapping out.
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

