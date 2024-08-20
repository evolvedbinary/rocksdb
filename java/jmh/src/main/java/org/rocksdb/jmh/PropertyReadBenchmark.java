package org.rocksdb.jmh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.rocksdb.*;



@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(1)
@Warmup(iterations = 1, time = 5)
@Measurement(iterations = 5, time = 5)
@BenchmarkMode(Mode.All)
public class PropertyReadBenchmark {

    @Setup(Level.Trial)
    public void setup() throws Exception {
        RocksDB.loadLibrary();
    }

    private Path dbPath;
    private RocksDB rocksDB;
    private Options dbOptions;

    @Param({ "0", "10000000"})
    public long keysInDB;

    @Benchmark
    public long get() throws RocksDBException {
        return rocksDB.getLongProperty("rocksdb.estimate-num-keys");
    }


    @Setup(Level.Iteration)
    public void createCreateDb() throws IOException, RocksDBException {
        dbPath = Files.createTempDirectory("JMH").toAbsolutePath();
        System.out.println("temp dir: " + dbPath);
        System.out.println("Keys in db: " + keysInDB);

        dbOptions = new Options();
        dbOptions.setCreateIfMissing(true);
        dbOptions.setCreateMissingColumnFamilies(true);
        rocksDB = RocksDB.open(dbOptions, dbPath.toString());
        ByteBuffer key = ByteBuffer.allocateDirect(128);
        ByteBuffer value = ByteBuffer.allocateDirect(128);
        Random r = new Random(1123125124l);
        try(WriteOptions writeOptions = new WriteOptions().setDisableWAL(true)) {
            for (long l = 0; l < keysInDB; l++) {
                key.clear().putLong(r.nextLong()).putLong(r.nextLong()).putLong(r.nextLong()).putLong(r.nextLong()).flip();
                value.clear().putLong(r.nextLong()).putLong(r.nextLong()).putLong(r.nextLong()).putLong(r.nextLong()).flip();
                rocksDB.put(writeOptions, key, value);
                if (l % 100_000 == 0) {
                    System.out.println("keys : " + l);
                }
            }
        }
    }

    @TearDown(Level.Iteration)
    public void closeDb() throws IOException {
        rocksDB.close();
        dbOptions.close();

        Files.walk(dbPath).sorted(Comparator.reverseOrder()).forEach(x -> {
            try {
                Files.delete(x);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Files.deleteIfExists(dbPath);
    }




}
