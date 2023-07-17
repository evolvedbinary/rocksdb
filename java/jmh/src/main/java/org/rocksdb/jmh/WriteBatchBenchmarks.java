/**
 * Copyright (c) 2011-present, Facebook, Inc.  All rights reserved.
 *  This source code is licensed under both the GPLv2 (found in the
 *  COPYING file in the root directory) and Apache 2.0 License
 *  (found in the LICENSE.Apache file in the root directory).
 */
package org.rocksdb.jmh;

import static org.rocksdb.util.KVUtils.ba;
import static org.rocksdb.util.KVUtils.writeToByteBuffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.openjdk.jmh.annotations.*;
import org.rocksdb.*;
import org.rocksdb.util.FileUtils;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(1)
@Warmup(iterations = 1, time = 10)
@Measurement(iterations = 3, time = 10)
public class WriteBatchBenchmarks {
  @Setup(Level.Trial)
  public void setup() throws IOException, RocksDBException {
    RocksDB.loadLibrary();

    nioKeyBuffer = ByteBuffer.allocateDirect(keySize);
    nioValueBuffer = ByteBuffer.allocateDirect(valueSize);

    keyArray = new byte[keySize];
    valueArray = new byte[valueSize];

    wrapKey = ByteBuffer.wrap(keyArray);
    wrapValue = ByteBuffer.wrap(valueArray);

    valueFill = new byte[valueSize];
    for (int i = 0; i < valueSize; i++) {
      valueFill[i] = (byte) 0x30;
    }
  }

  private static final int N = 10_000;
  
  @Param({"16","64"}) int keySize;

  @Param({"16", "64", "1024"}) int valueSize;

  @Benchmark
  @OperationsPerInvocation(N)
  public void put() throws RocksDBException {
    WriteBatch wb = new WriteBatch();
    try {
      for (int i = 0; i < N; i++) {
        wb.put(ba("key" + i, keySize), ba("value" + i, valueSize));
      }
    } finally {
      wb.close();
    }
  }

  private byte[] keyArray;
  private byte[] valueArray;
  private byte[] keyStringBytes = "key".getBytes(StandardCharsets.UTF_8);
  private byte[] valueStringBytes = "value".getBytes(StandardCharsets.UTF_8);
  private ByteBuffer wrapKey;
  private ByteBuffer wrapValue;
  private byte[] valueFill;

  @Benchmark
  @OperationsPerInvocation(N)
  public void putCached() throws RocksDBException {
    WriteBatch wb = new WriteBatch();
    try {
      for (int i = 0; i < N; i++) {
        wrapKey.clear();
        wrapKey.put(keyStringBytes);
        wrapKey.putInt(i);
        wrapKey.put(valueFill, wrapKey.position(), keySize - wrapKey.position());

        wrapValue.clear();
        wrapValue.put(valueStringBytes);
        wrapValue.putInt(i);
        wrapValue.put(valueFill, wrapValue.position(), valueSize - wrapValue.position());

        wb.put(keyArray, valueArray);
      }
    } finally {
      wb.close();
    }
  }

  private ByteBuffer nioKeyBuffer;
  private ByteBuffer nioValueBuffer;

  @Benchmark
  @OperationsPerInvocation(N)
  public void putWithByteBuffer() throws RocksDBException {
    WriteBatch wb = new WriteBatch();
    try {
      for (int i = 0; i < N; i++) {
        nioKeyBuffer.clear();
        nioKeyBuffer.put("key".getBytes(StandardCharsets.UTF_8));
        nioKeyBuffer.putInt(i);
        nioKeyBuffer.put(valueFill, nioKeyBuffer.position(), keySize - nioKeyBuffer.position());
        nioKeyBuffer.flip();

        nioValueBuffer.clear();
        nioValueBuffer.put("value".getBytes(StandardCharsets.UTF_8));
        nioValueBuffer.putInt(i);
        nioValueBuffer.put(valueFill, nioValueBuffer.position(), valueSize - nioValueBuffer.position());
        nioValueBuffer.flip();

        wb.put(nioKeyBuffer, nioValueBuffer);
      }
    } finally {
      wb.close();
    }
  }

  private final ByteBuf keyBuffer = PooledByteBufAllocator.DEFAULT.directBuffer(keySize);
  private final ByteBuf valueBuffer = PooledByteBufAllocator.DEFAULT.directBuffer(valueSize);

  @Benchmark
  @OperationsPerInvocation(N)
  public void putWithAddress() throws RocksDBException {
    WriteBatch wb = new WriteBatch();
    try {
      for (int i = 0; i < N; i++) {
        keyBuffer.clear();
        ByteBufUtil.writeAscii(keyBuffer, "key");
        keyBuffer.writeInt(i);
        keyBuffer.writeBytes(valueFill, keyBuffer.writerIndex(), keySize - keyBuffer.writerIndex());

        valueBuffer.clear();
        ByteBufUtil.writeAscii(valueBuffer, "value");
        valueBuffer.writeInt(i);
        valueBuffer.writeBytes(valueFill, valueBuffer.writerIndex(), valueSize - valueBuffer.writerIndex());

        wb.put(keyBuffer.memoryAddress(), keySize, valueBuffer.memoryAddress(),
            valueSize);
      }
    } finally {
      wb.close();
    }
  }
}
