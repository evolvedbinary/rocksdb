package org.rocksdb.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.rocksdb.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.rocksdb.util.KVUtils.baFillValue;

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 10)
@Measurement(iterations = 5, time = 10)
public class IteratorBenchmarks {

    Path dbDir;
    Options options;
    ReadOptions readOptions;
    RocksDB rocksDB;

    static final int KEY_VALUE_MAX_WIDTH = 10;

    @Param({"1000000"}) int keyCount;

    @Param({"524288"}) int bytesPerIteration;

    @Setup(Level.Trial)
    public void setup(ByteArrayData data) throws IOException, RocksDBException {
        RocksDB.loadLibrary();

        dbDir = Files.createTempDirectory("rocksjava-iterator-benchmarks");
        System.err.println("DB " + dbDir);

        options = new Options()
            .setCreateIfMissing(true)
            .setCreateMissingColumnFamilies(true);
        readOptions = new ReadOptions();

        rocksDB = RocksDB.open(options, dbDir.toAbsolutePath().toString());

        // store initial data for retrieving via get
        byte[] keyArr = new byte[data.keySize];
        byte[] valueArr = new byte[data.valueSize];
        for (long keyIndex = 0; keyIndex < keyCount; keyIndex++) {
            baFillValue(keyArr, "key", keyIndex, KEY_VALUE_MAX_WIDTH, (byte)0x30);
            baFillValue(valueArr, "key", keyIndex, KEY_VALUE_MAX_WIDTH, (byte)0x30);
            rocksDB.put(keyArr, valueArr);
        }

        try (final FlushOptions flushOptions = new FlushOptions().setWaitForFlush(true)) {
            rocksDB.flush(flushOptions);
        }
    }

    @TearDown(Level.Trial)
    public void teardown() throws IOException {
        rocksDB.close();

        try (var files = Files.walk(dbDir).sorted(Comparator.reverseOrder())) {
            files.forEach(file -> {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        Files.deleteIfExists(dbDir);
    }


    /**
     * Holds state for a thread
     */
    @State(Scope.Thread)
    public static class IteratorThread {

        public long count = 0;

        RocksIterator iterator;

        RocksDB rocksDB;

        @Setup
        public void setup(IteratorBenchmarks bm) {

            this.rocksDB = bm.rocksDB;
            iterator = rocksDB.newIterator();
            iterator.seekToFirst();
        }

        @TearDown
        public void teardown() throws RocksDBException {
            iterator.close();
        }
    }

    @State(Scope.Thread)
    public static class ByteArrayData {

        @Param({"16", "64", "128"}) int keySize;

        @Param({"64", "1024", "65536"}) int valueSize;


        private byte[] key;
        private byte[] value;

        @Setup
        public void setup() {
            key = new byte[keySize];
            value = new byte[valueSize];
        }
    }

    @Benchmark public void iteratorScan(IteratorThread iteratorThread, ByteArrayData data) {
        RocksIterator iterator = iteratorThread.iterator;
        int scannedDataSize = 0;
        while (scannedDataSize <= bytesPerIteration) {
            if (!iterator.isValid()) {
                iterator.seekToFirst();
            }
            iterator.next();
            scannedDataSize += data.keySize + data.valueSize;
        }
    }

    @Benchmark public void iteratorKeyScan(IteratorThread iteratorThread, ByteArrayData data, Blackhole blackhole) {
        RocksIterator iterator = iteratorThread.iterator;
        int scannedDataSize = 0;
        while (scannedDataSize <= bytesPerIteration) {
            if (!iterator.isValid()) {
                iterator.seekToFirst();
            }
            blackhole.consume(iterator.key());
            scannedDataSize += data.keySize + data.valueSize;
            iterator.next();
            iteratorThread.count++;
        }
    }

    @Benchmark public void iteratorValueScan(IteratorThread iteratorThread, ByteArrayData data, Blackhole blackhole) {
        RocksIterator iterator = iteratorThread.iterator;
        int scannedDataSize = 0;
        while (scannedDataSize <= bytesPerIteration) {
            if (!iterator.isValid()) {
                iterator.seekToFirst();
            }
            blackhole.consume(iterator.key());
            blackhole.consume(iterator.value());
            scannedDataSize += data.keySize + data.valueSize;
            iterator.next();
            iteratorThread.count++;
        }
    }

    public static void main(String[] args) throws RunnerException {
        org.openjdk.jmh.runner.options.Options opt =
            new OptionsBuilder()
                .param("keySize", "16")
                .param("valueSize", "64")
                .include("IteratorBenchmarks.iteratorScan").forks(0).build();

        new Runner(opt).run();
    }
}
