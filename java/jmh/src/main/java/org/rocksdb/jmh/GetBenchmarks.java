/**
 * Copyright (c) 2011-present, Facebook, Inc.  All rights reserved.
 * This source code is licensed under both the GPLv2 (found in the
 * COPYING file in the root directory) and Apache 2.0 License
 * (found in the LICENSE.Apache file in the root directory).
 */
package org.rocksdb.jmh;

import static org.rocksdb.util.KVUtils.ba;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.openjdk.jmh.annotations.*;
import org.rocksdb.*;
import org.rocksdb.util.FileUtils;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class GetBenchmarks {

  @Param({
      "no_column_family",
      "1_column_family",
      "20_column_families",
      "100_column_families"
  })
  String columnFamilyTestType;

  @Param({"1000", "100000"}) int keyCount;

  @Param({"12", "64", "128"}) int keySize;

  @Param({"64", "1024", "65536"}) int valueSize;

  Path dbDir;
  DBOptions options;
  ReadOptions readOptions;
  int cfs = 0;  // number of column families
  private AtomicInteger cfHandlesIdx;
  ColumnFamilyHandle[] cfHandles;
  RocksDB db;

  @State(Scope.Thread)
  public static class ThreadBuffers {
    private ByteBuffer keyBuf;
    private ByteBuffer valueBuf;
    private byte[] keyArr;
    private byte[] valueArr;

    private Random random = new Random();
    private int keyIndex = 0;

    @Param({"12", "64", "128"}) int keySize;
    @Param({"64", "1024", "65536"}) int valueSize;
    @Param({"1000", "100000"}) int keyCount;

    @Setup(Level.Trial) public void setup() {
      keyArr = new byte[keySize];
      valueArr = new byte[valueSize];
      keyBuf = ByteBuffer.allocateDirect(keySize);
      valueBuf = ByteBuffer.allocateDirect(valueSize);
      Arrays.fill(keyArr, (byte) 0x30);
      Arrays.fill(valueArr, (byte) 0x30);
      keyBuf.put(keyArr);
      keyBuf.flip();
      valueBuf.put(valueArr);
      valueBuf.flip();  
    }

    private int nextKeyRandom() {
      return random.nextInt() % keyCount;
    }

    private int nextKey() {
      int result = keyIndex;
      keyIndex = (keyIndex + 1) % keyCount;
      return result;
    }

    private byte[] getKeyArr(final int keyIdx) {
      final int MAX_LEN = 9; // key100000
      final byte[] keyPrefix = ba("key" + keyIdx);
      System.arraycopy(keyPrefix, 0, keyArr, 0, keyPrefix.length);
      Arrays.fill(keyArr, keyPrefix.length, MAX_LEN, (byte) 0x30);
      return keyArr;
    }

    private byte[] getValueArr() {
      return valueArr;
    }  

    private ByteBuffer getKeyBuf(final int keyIdx) {
      final int MAX_LEN = 9; // key100000
      final String keyStr = "key" + keyIdx;
      for (int i = 0; i < keyStr.length(); ++i) {
        keyBuf.put(i, (byte) keyStr.charAt(i));
      }
      for (int i = keyStr.length(); i < MAX_LEN; ++i) {
        keyBuf.put(i, (byte) 0x30);
      }
      // Reset position for future reading
      keyBuf.position(0);
      return keyBuf;
    }
  
    private ByteBuffer getValueBuf() {
      return valueBuf;
    }
   
  }

  @Setup(Level.Trial)
  public void setup() throws IOException, RocksDBException {
    RocksDB.loadLibrary();

    dbDir = Files.createTempDirectory("rocksjava-get-benchmarks");

    options = new DBOptions()
        .setCreateIfMissing(true)
        .setCreateMissingColumnFamilies(true);
    readOptions = new ReadOptions();

    final List<ColumnFamilyDescriptor> cfDescriptors = new ArrayList<>();
    cfDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY));

    if ("1_column_family".equals(columnFamilyTestType)) {
      cfs = 1;
    } else if ("20_column_families".equals(columnFamilyTestType)) {
      cfs = 20;
    } else if ("100_column_families".equals(columnFamilyTestType)) {
      cfs = 100;
    }

    if (cfs > 0) {
      cfHandlesIdx = new AtomicInteger(1);
      for (int i = 1; i <= cfs; i++) {
        cfDescriptors.add(new ColumnFamilyDescriptor(ba("cf" + i)));
      }
    }

    final List<ColumnFamilyHandle> cfHandlesList = new ArrayList<>(cfDescriptors.size());
    db = RocksDB.open(options, dbDir.toAbsolutePath().toString(), cfDescriptors, cfHandlesList);
    cfHandles = cfHandlesList.toArray(new ColumnFamilyHandle[0]);

    // store initial data for retrieving via get
    byte[] keyArr = new byte[keySize];
    byte[] valueArr = new byte[valueSize];
    Arrays.fill(keyArr, (byte) 0x30);
    Arrays.fill(valueArr, (byte) 0x30);
    for (int i = 0; i <= cfs; i++) {
      for (int j = 0; j < keyCount; j++) {
        final byte[] keyPrefix = ba("key" + j);
        final byte[] valuePrefix = ba("value" + j);
        System.arraycopy(keyPrefix, 0, keyArr, 0, keyPrefix.length);
        System.arraycopy(valuePrefix, 0, valueArr, 0, valuePrefix.length);
        db.put(cfHandles[i], keyArr, valueArr);
      }
    }

    try (final FlushOptions flushOptions = new FlushOptions().setWaitForFlush(true)) {
      db.flush(flushOptions);
    }

    ByteBuffer keyBuf = ByteBuffer.allocateDirect(keySize);
    ByteBuffer valueBuf = ByteBuffer.allocateDirect(valueSize);
    Arrays.fill(keyArr, (byte) 0x30);
    Arrays.fill(valueArr, (byte) 0x30);
    keyBuf.put(keyArr);
    keyBuf.flip();
    valueBuf.put(valueArr);
    valueBuf.flip();
  }

  @TearDown(Level.Trial)
  public void cleanup() throws IOException {
    for (final ColumnFamilyHandle cfHandle : cfHandles) {
      cfHandle.close();
    }
    db.close();
    options.close();
    readOptions.close();
    FileUtils.delete(dbDir);
  }

  private ColumnFamilyHandle getColumnFamily() {
    if (cfs == 0) {
      return cfHandles[0];
    } else if (cfs == 1) {
      return cfHandles[1];
    } else {
      int idx = cfHandlesIdx.getAndIncrement();
      if (idx > cfs) {
        cfHandlesIdx.set(1); // doesn't ensure a perfect distribution, but it's ok
        idx = 0;
      }
      return cfHandles[idx];
    }
  }

  @Benchmark
  public void get(Blackhole blackHole, ThreadBuffers buffers) throws RocksDBException {
    blackHole.consume( db.get(getColumnFamily(), buffers.getKeyArr(buffers.nextKey())));
  }

  @Benchmark
  public void getRandom(Blackhole blackHole, ThreadBuffers buffers) throws RocksDBException {
    blackHole.consume( db.get(getColumnFamily(), buffers.getKeyArr(buffers.nextKeyRandom())));
  }

  @Benchmark
  public void preallocatedGet(Blackhole blackHole, ThreadBuffers buffers) throws RocksDBException {
    byte[] value = buffers.getValueArr();
    blackHole.consume( db.get(getColumnFamily(), readOptions, buffers.getKeyArr(buffers.nextKey()), value));
    blackHole.consume( value);
  }

  @Benchmark
  public void preallocatedGetCritical(Blackhole blackHole, ThreadBuffers buffers) throws RocksDBException {
    byte[] value = buffers.getValueArr();
    blackHole.consume( db.getCritical(getColumnFamily(), readOptions, buffers.getKeyArr(buffers.nextKey()), value));
    blackHole.consume( value);
  }

  @Benchmark
  public void preallocatedGetRandom(Blackhole blackHole, ThreadBuffers buffers) throws RocksDBException {
    byte[] value = buffers.getValueArr();
    blackHole.consume( db.get(getColumnFamily(), readOptions, buffers.getKeyArr(buffers.nextKeyRandom()), value));
    blackHole.consume( value);
  }

  @Benchmark
  public void preallocatedGetRandomCritical(Blackhole blackHole, ThreadBuffers buffers) throws RocksDBException {
    byte[] value = buffers.getValueArr();
    blackHole.consume( db.getCritical(getColumnFamily(), readOptions, buffers.getKeyArr(buffers.nextKeyRandom()), value));
    blackHole.consume( value);
  }

  @Benchmark
  public void preallocatedByteBufferGet(Blackhole blackHole, ThreadBuffers buffers) throws RocksDBException {
    ByteBuffer valueBB = buffers.getValueBuf();
    blackHole.consume( db.get(getColumnFamily(), readOptions, buffers.getKeyBuf(buffers.nextKey()), valueBB));
    blackHole.consume( valueBB);
  }

  @Benchmark
  public void preallocatedByteBufferGetRandom(Blackhole blackHole, ThreadBuffers buffers) throws RocksDBException {
    ByteBuffer valueBB = buffers.getValueBuf();
    blackHole.consume( db.get(getColumnFamily(), readOptions, buffers.getKeyBuf(buffers.nextKeyRandom()), valueBB));
    blackHole.consume( valueBB);
  }

}