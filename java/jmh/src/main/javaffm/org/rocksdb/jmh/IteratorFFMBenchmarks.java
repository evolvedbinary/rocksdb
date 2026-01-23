package org.rocksdb.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.options.Options;
import org.rocksdb.*;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Random;

import static org.rocksdb.util.KVUtils.baFillValue;

/**
 * Benchmark for Iterator using Java's Foreign Function & Memory (FFM) API.
 * This benchmark demonstrates how MemorySegment can be used with RocksDB Iterators.
 */
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgsAppend = {"--enable-native-access=ALL-UNNAMED"})
@Warmup(iterations = 3, time = 10)
@Measurement(iterations = 5, time = 10)
public class IteratorFFMBenchmarks {

    Path dbDir;
    Options options;
    RocksDB rocksDB;

    static final int KEY_VALUE_MAX_WIDTH = 10;

    @Param({"1000000"})
    int keyCount;

    @Param({"524288"})
    int bytesPerIteration;

    @Setup(Level.Trial)
    public void setup(FFMData data) throws IOException, RocksDBException {
        RocksDB.loadLibrary();

        dbDir = Files.createTempDirectory("rocksjava-iterator-ffm-benchmarks");

        options = new Options()
                .setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true);

        rocksDB = RocksDB.open(options, dbDir.toAbsolutePath().toString());

        // Populate the database
        byte[] keyArr = new byte[data.keySize];
        byte[] valueArr = new byte[data.valueSize];
        for (long keyIndex = 0; keyIndex < keyCount; keyIndex++) {
            baFillValue(keyArr, "key", keyIndex, KEY_VALUE_MAX_WIDTH, (byte) 0x30);
            baFillValue(valueArr, "key", keyIndex, KEY_VALUE_MAX_WIDTH, (byte) 0x30);
            rocksDB.put(keyArr, valueArr);
        }

        try (final FlushOptions flushOptions = new FlushOptions().setWaitForFlush(true)) {
            rocksDB.flush(flushOptions);
        }
    }

    @TearDown(Level.Trial)
    public void teardown() throws IOException {
        if (rocksDB != null) {
            rocksDB.close();
        }

        if (dbDir != null && Files.exists(dbDir)) {
            try (var files = Files.walk(dbDir).sorted(Comparator.reverseOrder())) {
                files.forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    @State(Scope.Thread)
    public static class IteratorThread {
        RocksIterator iterator;

        @Setup
        public void setup(IteratorFFMBenchmarks bm) {
            iterator = bm.rocksDB.newIterator();
            iterator.seekToFirst();
        }

        @TearDown
        public void teardown() {
            iterator.close();
        }
    }

    @State(Scope.Thread)
    public static class FFMData {
        @Param({"16", "64", "128"})
        int keySize;

        @Param({"64", "1024", "65536"})
        int valueSize;

        Arena arena;
        MemorySegment keySegment;
        MemorySegment valueSegment;
        ByteBuffer keyBuffer;
        ByteBuffer valueBuffer;

        @Setup
        public void setup() {
            arena = Arena.ofConfined();
            keySegment = arena.allocate(keySize);
            valueSegment = arena.allocate(valueSize);

            // Bridge to ByteBuffer for current RocksDB API compatibility
            keyBuffer = keySegment.asByteBuffer();
            valueBuffer = valueSegment.asByteBuffer();
        }

        @TearDown
        public void teardown() {
            arena.close();
        }
    }

    @Benchmark
    public void iteratorFFMScan(IteratorThread iteratorThread, FFMData data, Blackhole blackhole) {
        RocksIterator iterator = iteratorThread.iterator;
        int scannedDataSize = 0;

        while (scannedDataSize <= bytesPerIteration) {
            if (!iterator.isValid()) {
                iterator.seekToFirst();
            }

            // Using ByteBuffer view of the MemorySegment
            data.keyBuffer.clear();
            iterator.key(data.keyBuffer);
            blackhole.consume(data.keyBuffer);

            data.valueBuffer.clear();
            iterator.value(data.valueBuffer);
            blackhole.consume(data.valueBuffer);

            scannedDataSize += data.keySize + data.valueSize;
            iterator.next();
        }
    }
}
