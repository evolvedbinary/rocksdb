# JMH Benchmarks for RocksJava

These are micro-benchmarks for RocksJava functionality, using [JMH (Java Microbenchmark Harness)](https://openjdk.java.net/projects/code-tools/jmh/).

## Compiling

**Note**: This uses a specific build of RocksDB that is set in the `<version>` element of the `dependencies` section of the `pom.xml` file. If you are testing local changes you should build and install a SNAPSHOT version of rocksdbjni, and update the `pom.xml` of rocksdbjni-jmh file to test with this.

For instance, this is how to install the OSX jar you just built for 8.11.0

```bash
$ mvn install:install-file -Dfile=./java/target/rocksdbjni-8.11.0-SNAPSHOT-osx.jar -DgroupId=org.rocksdb -DartifactId=rocksdbjni -Dversion=8.11.0-SNAPSHOT -Dpackaging=jar
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
