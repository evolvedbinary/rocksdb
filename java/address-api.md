### Running

First time
```
make rocksdbjavageneratepom
```

Every time (but the ls stuff is just sanity checks)
```
(make clean jclean; make -j12 rocksdbjava)
ls -l ./java/target/rocksdbjni-8.4.0-osx.jar
(cd java; mvn install:install-file -Dfile=./target/rocksdbjni-8.4.0-osx.jar -DgroupId=org.rocksdb -DartifactId=rocksdbjni -Dversion=8.4.0 -Dpackaging=jar)
ls -l ~/.m2/repository/org/rocksdb/rocksdbjni/8.4.0
cd java/jmh
mvn clean package
ls -l /Users/alan/swProjects/evolvedBinary/rocksdb-evolved/java/jmh/target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar -wi 2 -i 2 -f 1 org.rocksdb.jmh.GetBenchmarks -p keyCount=100000 -p keySize=12,1024 -p valueSize=64,2048 -p columnFamilyTestType=no_column_family
cd ../..
```

### First run
```
java -jar target/rocksdbjni-jmh-1.0-SNAPSHOT-benchmarks.jar -wi 2 -i 2 -f 1 org.rocksdb.jmh.GetBenchmarks -p keyCount=100000 -p keySize=12,1024 -p valueSize=64,2048 -p columnFamilyTestType=no_column_family
```

Benchmark                                (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt       Score   Error  Units
GetBenchmarks.get                              no_column_family      100000         12           64  thrpt    2  708818.708          ops/s
GetBenchmarks.get                              no_column_family      100000         12         2048  thrpt    2  398094.879          ops/s
GetBenchmarks.get                              no_column_family      100000       1024           64  thrpt    2  438344.884          ops/s
GetBenchmarks.get                              no_column_family      100000       1024         2048  thrpt    2  358253.817          ops/s
GetBenchmarks.preallocatedByteBufferGet        no_column_family      100000         12           64  thrpt    2  714297.437          ops/s
GetBenchmarks.preallocatedByteBufferGet        no_column_family      100000         12         2048  thrpt    2  417248.820          ops/s
GetBenchmarks.preallocatedByteBufferGet        no_column_family      100000       1024           64  thrpt    2  449639.851          ops/s
GetBenchmarks.preallocatedByteBufferGet        no_column_family      100000       1024         2048  thrpt    2  381580.720          ops/s
GetBenchmarks.preallocatedGet                  no_column_family      100000         12           64  thrpt    2  705291.099          ops/s
GetBenchmarks.preallocatedGet                  no_column_family      100000         12         2048  thrpt    2  415300.751          ops/s
GetBenchmarks.preallocatedGet                  no_column_family      100000       1024           64  thrpt    2  442114.744          ops/s
GetBenchmarks.preallocatedGet                  no_column_family      100000       1024         2048  thrpt    2  372076.056          ops/s
GetBenchmarks.preallocatedMemoryAddr           no_column_family      100000         12           64  thrpt    2  907820.767          ops/s
GetBenchmarks.preallocatedMemoryAddr           no_column_family      100000         12         2048  thrpt    2  482658.664          ops/s
GetBenchmarks.preallocatedMemoryAddr           no_column_family      100000       1024           64  thrpt    2  437412.588          ops/s
GetBenchmarks.preallocatedMemoryAddr           no_column_family      100000       1024         2048  thrpt    2  420415.349          ops/s

### Thoughts

I was seeing some wrong looking numbers. Not sure why. Upping the keyCount from `1000` to `100000` seems to sort it. The figures above make sense. Let's see if we can repair the `ByteBuffer` code ?

### Removing checks on `ByteBuffer`

Removed `env->GetDirectBufferCapacity(jval)` checks on `get()` and `put()` helpers.
Compared to the above, it's clear that this makes a very significant difference to the performance of preallocated `ByteBuffer` methods.
If we were to remove the checks used by `env->GetDirectBufferAddress()` we would probably get close to the `preallocatedMemoryAddr` performance.
Here are the results with the length check removed; that's only half of it.

Benchmark                                (columnFamilyTestType)  (keyCount)  (keySize)  (valueSize)   Mode  Cnt       Score   Error  Units
GetBenchmarks.get                              no_column_family      100000         12           64  thrpt    2  709606.758          ops/s
GetBenchmarks.get                              no_column_family      100000         12         2048  thrpt    2  395924.289          ops/s
GetBenchmarks.get                              no_column_family      100000       1024           64  thrpt    2  436953.401          ops/s
GetBenchmarks.get                              no_column_family      100000       1024         2048  thrpt    2  355661.312          ops/s
GetBenchmarks.preallocatedByteBufferGet        no_column_family      100000         12           64  thrpt    2  775674.744          ops/s
GetBenchmarks.preallocatedByteBufferGet        no_column_family      100000         12         2048  thrpt    2  434625.358          ops/s
GetBenchmarks.preallocatedByteBufferGet        no_column_family      100000       1024           64  thrpt    2  473163.259          ops/s
GetBenchmarks.preallocatedByteBufferGet        no_column_family      100000       1024         2048  thrpt    2  396361.765          ops/s
GetBenchmarks.preallocatedGet                  no_column_family      100000         12           64  thrpt    2  707178.534          ops/s
GetBenchmarks.preallocatedGet                  no_column_family      100000         12         2048  thrpt    2  412958.088          ops/s
GetBenchmarks.preallocatedGet                  no_column_family      100000       1024           64  thrpt    2  434633.239          ops/s
GetBenchmarks.preallocatedGet                  no_column_family      100000       1024         2048  thrpt    2  370436.341          ops/s
GetBenchmarks.preallocatedMemoryAddr           no_column_family      100000         12           64  thrpt    2  892209.778          ops/s
GetBenchmarks.preallocatedMemoryAddr           no_column_family      100000         12         2048  thrpt    2  480389.913          ops/s
GetBenchmarks.preallocatedMemoryAddr           no_column_family      100000       1024           64  thrpt    2  428906.700          ops/s
GetBenchmarks.preallocatedMemoryAddr           no_column_family      100000       1024         2048  thrpt    2  421665.276          ops/s

### Proposals

#### Better ByteBuffer

If we use the proposed raw address methods to write an alternative `ByteBuffer` implementation, do we still pay the checking costs ? If not,
that could be a good way forward; we could propose making the raw methods `protected` members of a `RocksDBAddr` class.
