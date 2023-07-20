## Introduction

We have taken the `address-api` branch and carried out some further tests and experiments. These live in the [Evolved Binary branch](https://github.com/evolvedbinary/rocksdb/tree/address-api-experiments)
## Conceptual Review

I re-ran the tests and confirmed your results. That really looks like a big gain; we had not looked enough at performance of small buffers to see the problems with `ByteBuffer` range checking.

I rebuilt the `ByteBuffer` code with the `GetDirectBufferCapacity()` check turned off, and confirmed that we immediately get half of the performance gain you have seen; this strongly supports your profiling results.

I also tried to adapt the `ByteBuffer` code to use JNI field access to the `address` field instead of `GetDirectBufferAddress()`, but it doesn't help. I can imagine that taking the address on the Java side and passing it through to C++ would work, but this is more or less replicating what you have done to use Netty `ByteBuf`, and applying it to `ByteBuffer`

The main criticism I have is that accepting the PR as-is would expose an entirely unchecked interface with raw `address` and `length` fields at the API. We haven't done this before, and in particular we hope that we can in the long term move to an API that uses
the [`MemorySegment`](https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/lang/foreign/MemorySegment.html) class as a buffer parameter as and when we can push the Java version far enough to support Project Panama.

One alternative mechanism I looked at was making the new `put()` methods package private
```java
void put(final ColumnFamilyHandle columnFamilyHandle, final WriteOptions writeOpts,
      final long keyAddr, final int keyLen, final long valueAddr, final int valueLen)
      throws RocksDBException {
    put(nativeHandle_, writeOpts.nativeHandle_, keyAddr, keyLen, valueAddr, valueLen,
        columnFamilyHandle.nativeHandle_);
      }
```
and implementing a base class in the `org.rocksdb` package which could be extended by 3rd-parties to implement (for instance) a
`ByteBuf` API. Something like this (the RocksJNI part):
```java
package org.rocksdb;

public abstract class RocksDBAddr {

    private final RocksDB db;

    protected RocksDBAddr(final RocksDB db) {
        this.db = db;
    }

    protected void put(final ColumnFamilyHandle columnFamilyHandle, final WriteOptions writeOpts,
                       final long keyAddr, final int keyLen, final long valueAddr, final int valueLen) throws RocksDBException {
        db.put(columnFamilyHandle, writeOpts, keyAddr, keyLen, valueAddr, valueLen);
    }

    protected void put(final ColumnFamilyHandle columnFamilyHandle, final long keyAddr, final int keyLen,
                       final long valueAddr, final int valueLen) throws RocksDBException {
        db.put(columnFamilyHandle, keyAddr, keyLen, valueAddr, valueLen);
    }

    protected void put(final WriteOptions writeOpts, final long keyAddr, final int keyLen,
                       final long valueAddr, final int valueLen) throws RocksDBException {
        db.put(writeOpts, keyAddr, keyLen, valueAddr, valueLen);
    }
}
```
and the class subclassed by the client, something like this:
```java
package org.rocksdb.jmh;

import io.netty.buffer.ByteBuf;
import org.rocksdb.*;

public class RocksDBBuf extends RocksDBAddr {
    
    public RocksDBBuf(final RocksDB db) {
        super(db);
    }

    public void put(final ColumnFamilyHandle columnFamilyHandle, final WriteOptions writeOpts,
                    final ByteBuf key, final ByteBuf value) throws RocksDBException {
        put(columnFamilyHandle, writeOpts, key.memoryAddress(), key.readableBytes(), value.memoryAddress(), value.readableBytes());
    }

    protected void put(final ColumnFamilyHandle columnFamilyHandle, final ByteBuf key, final ByteBuf value) throws RocksDBException {
        put(columnFamilyHandle, key.memoryAddress(), key.readableBytes(), value.memoryAddress(), value.readableBytes());
    }

    protected void put(final WriteOptions writeOpts, final ByteBuf key, final ByteBuf value) throws RocksDBException {
        put(writeOpts, key.memoryAddress(), key.readableBytes(), value.memoryAddress(), value.readableBytes());
    }
}
```
and then the accessing client code looks like this:
```java
var dbBuf = new RocksDBBuf(db);
...
@Benchmark
  public void putWithMemoryAddr(final ComparatorBenchmarks.Counter counter)
      throws RocksDBException {
    final int i = counter.next();
    keyBuffer.clear();
    keyBuffer.writeBytes(ba("key" + i));
    valueBuffer.clear();
    valueBuffer.writeBytes(ba("value" + i));
    dbBuf.put(getColumnFamily(), keyBuffer, valueBuffer);
  }
```
This would make it very hard for someone to use the address-based API unless they really meant to. Plus we could use exactly the same mechanism to offer a `MemorySegment`-based API.

I benchmarked that for `put()` and it seems indistinguishable from your version, unsuprisingly as it is just a couple of extra calls which are probably being inlined anyway.

## Concrete Items

### Helper Consistency/Address Method Refactor

I would prefer that `kv_op_direct_addr()` in `portal.h` should mirror the form of the other
kv_op methods, taking the slice `op` function as a first parameter and doing the job of
converting the address and length parameters into slices. So approximately this:
```java
  static void kv_op_direct_addr(
      std::function<void(ROCKSDB_NAMESPACE::Slice&, ROCKSDB_NAMESPACE::Slice&)> op,
      JNIEnv* env,
      jlong jkey_addr, jint jkey_len,
      jlong jval_addr, jint jval_len) {

    char* key = reinterpret_cast<char*>(jkey_addr);
    if (key == nullptr) {
      ROCKSDB_NAMESPACE::RocksDBExceptionJni::ThrowNew(env,
                                                       "Invalid key argument");
      return;
    }

    char* value = reinterpret_cast<char*>(jval_addr);
    if (value == nullptr) {
      ROCKSDB_NAMESPACE::RocksDBExceptionJni::ThrowNew(
          env, "Invalid value argument");
      return;
    }

    ROCKSDB_NAMESPACE::Slice key_slice(reinterpret_cast<char*>(jkey_addr), jkey_len);
    ROCKSDB_NAMESPACE::Slice value_slice(reinterpret_cast<char*>(jval_addr), jval_len);

    op(key_slice, value_slice);
  }
```
And then the `Java_org_rocksdb_RocksDB_putAddr` methods in `rocksjni.cc`, and the
`Java_org_rocksdb_WriteBatchWithIndex_putAddr` method(s) in `write_batch_with_index.cc` should
look approximately like this:
```java
/*
 * Class:     org_rocksdb_RocksDB
 * Method:    putAddr
 * Signature: (JJJIJIJ)V
 */
void Java_org_rocksdb_RocksDB_putAddr__JJJIJIJ(JNIEnv* env, jobject /*jdb*/,
                                               jlong jdb_handle,
                                               jlong jwrite_options_handle,
                                               jlong jkey_addr, jint jkey_len,
                                               jlong jval_addr, jint jval_len,
                                               jlong jcf_handle) {
  auto* db = reinterpret_cast<ROCKSDB_NAMESPACE::DB*>(jdb_handle);
  auto* write_options =
      reinterpret_cast<ROCKSDB_NAMESPACE::WriteOptions*>(jwrite_options_handle);
  auto* cf_handle =
      reinterpret_cast<ROCKSDB_NAMESPACE::ColumnFamilyHandle*>(jcf_handle);

  auto put = [&env, &db, &cf_handle, &write_options](
                 ROCKSDB_NAMESPACE::Slice& key,
                 ROCKSDB_NAMESPACE::Slice& value) {
    ROCKSDB_NAMESPACE::Status s;
    if (cf_handle == nullptr) {
      s = db->Put(*write_options, key, value);
    } else {
      s = db->Put(*write_options, cf_handle, key, value);
    }
    if (s.ok()) {
      return;
    }
    ROCKSDB_NAMESPACE::RocksDBExceptionJni::ThrowNew(env, s);
  };
  ROCKSDB_NAMESPACE::JniUtil::kv_op_direct_addr(
      put, env, jkey_addr, jkey_len, jval_addr, jval_len);
}
```

### WriteBatch Test Consistency

We would like to see the write batch tests encode the sizes of the keys and values that are written.
- We have reworked them to do this, including implementing some reasonably efficient helper code to
pad keys and values to the required size.
- We have also added a cached version of the `byte[]` benchmark, `putCached()` which avoids re-allocation
of the result `byte[]` on every `put()`; the `nio` and `addr` benchmarks already do this.

The core of each benchmark now writes the distinguished key and value prefixes to the key and value buffers,
then appends filler to the required size. So for `nio` the value buffer is prepared thus:
```java
        nioValueBuffer.clear();
        nioValueBuffer.put("value".getBytes(StandardCharsets.UTF_8));
        nioValueBuffer.putInt(i);
        nioValueBuffer.put(valueFill, nioValueBuffer.position(), valueSize - nioValueBuffer.position());
        nioValueBuffer.flip();
```
and the key buffer is prepared in exactly the same way.
And for `addr` the value buffer is prepared thus:
```java
        valueBuffer.clear();
        ByteBufUtil.writeAscii(valueBuffer, "value");
        valueBuffer.writeInt(i);
        valueBuffer.writeBytes(valueFill, valueBuffer.writerIndex(), valueSize - valueBuffer.writerIndex());
```
Results from my 64GB M1 Max Macbook Pro:
```
Benchmark                               (keySize)  (valueSize)   Mode  Cnt   Score   Error   Units
WriteBatchBenchmarks.put                       16           16  thrpt    2   1.956          ops/us
WriteBatchBenchmarks.put                       16           64  thrpt    2   1.966          ops/us
WriteBatchBenchmarks.put                       16         1024  thrpt    2   1.351          ops/us
WriteBatchBenchmarks.put                       64           16  thrpt    2   1.691          ops/us
WriteBatchBenchmarks.put                       64           64  thrpt    2   1.682          ops/us
WriteBatchBenchmarks.put                       64         1024  thrpt    2   1.333          ops/us
WriteBatchBenchmarks.putCached                 16           16  thrpt    2   2.473          ops/us
WriteBatchBenchmarks.putCached                 16           64  thrpt    2   2.371          ops/us
WriteBatchBenchmarks.putCached                 16         1024  thrpt    2   1.802          ops/us
WriteBatchBenchmarks.putCached                 64           64  thrpt    2   2.329          ops/us
WriteBatchBenchmarks.putCached                 64         1024  thrpt    2   1.797          ops/us
WriteBatchBenchmarks.putWithAddress            16           16  thrpt    2  20.426          ops/us
WriteBatchBenchmarks.putWithAddress            16           64  thrpt    2  16.043          ops/us
WriteBatchBenchmarks.putWithAddress            16         1024  thrpt    2   5.541          ops/us
WriteBatchBenchmarks.putWithAddress            64           16  thrpt    2  15.191          ops/us
WriteBatchBenchmarks.putWithAddress            64           64  thrpt    2  16.367          ops/us
WriteBatchBenchmarks.putWithAddress            64         1024  thrpt    2   5.207          ops/us
WriteBatchBenchmarks.putWithByteBuffer         16           16  thrpt    2   5.398          ops/us
WriteBatchBenchmarks.putWithByteBuffer         16           64  thrpt    2   4.932          ops/us
WriteBatchBenchmarks.putWithByteBuffer         16         1024  thrpt    2   3.306          ops/us
WriteBatchBenchmarks.putWithByteBuffer         64           64  thrpt    2   5.007          ops/us
WriteBatchBenchmarks.putWithByteBuffer         64         1024  thrpt    2   3.157          ops/us
```


