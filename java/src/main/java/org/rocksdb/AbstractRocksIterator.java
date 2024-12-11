// Copyright (c) 2011-present, Facebook, Inc.  All rights reserved.
//  This source code is licensed under both the GPLv2 (found in the
//  COPYING file in the root directory) and Apache 2.0 License
//  (found in the LICENSE.Apache file in the root directory).

package org.rocksdb;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;

/**
 * Base class implementation for Rocks Iterators
 * in the Java API
 *
 * <p>Multiple threads can invoke const methods on an RocksIterator without
 * external synchronization, but if any of the threads may call a
 * non-const method, all threads accessing the same RocksIterator must use
 * external synchronization.</p>
 *
 * @param <P> The type of the Parent Object from which the Rocks Iterator was
 *          created. This is used by disposeInternal to avoid double-free
 *          issues with the underlying C++ object.
 * @see org.rocksdb.RocksObject
 */
public abstract class AbstractRocksIterator<P extends RocksObject>
    extends RocksObject implements RocksIteratorInterface {
  final P parent_;
  Optional<SequentialCache> sequentialCache;

  private final static int MIN_SEQUENTIAL_CACHE_SIZE = 1024;

  protected AbstractRocksIterator(
      final P parent, final long nativeHandle, final int sequentialCacheSize) {
    super(nativeHandle);
    // parent must point to a valid RocksDB instance.
    assert (parent != null);
    // RocksIterator must hold a reference to the related parent instance
    // to guarantee that while a GC cycle starts RocksIterator instances
    // are freed prior to parent instances.
    parent_ = parent;

    if (sequentialCacheSize == 0) {
      sequentialCache = Optional.empty();
    } else if (sequentialCacheSize >= MIN_SEQUENTIAL_CACHE_SIZE) {
      sequentialCache = Optional.of(new SequentialCache(sequentialCacheSize));
    } else {
      throw new IllegalArgumentException(
          "RocksDB Iterator cache size must be 0, or a value >= " + MIN_SEQUENTIAL_CACHE_SIZE);
    }
  }

  @Override
  public boolean isValid() {
    Optional<Boolean> cacheValid = sequentialCache.map(cache -> {
      if (cache.beforeEnd()) {
        return true;
      }

      // try to fetch a new batch
      assert (isOwningHandle());
      cache.reset(prefetch0(nativeHandle_, cache.getBuffer()));
      // if we got 0 elements, we really are at the end
      // TODO FIX (AP) there is an edge case where (k,v) doesn't fit in the cache,
      // which will report FALSE erroneously. It needs a solution for production,
      // we have not fixed it for the performance testing case.
      return cache.beforeEnd();
    });
    return cacheValid.orElseGet(() -> {
      assert (isOwningHandle());
      return isValid0(nativeHandle_);
    });
  }

  @Override
  public void seekToFirst() {
    Optional<Boolean> cacheValid = sequentialCache.map(cache -> {
      assert (isOwningHandle());
      cache.reset(seekToFirst0(nativeHandle_, cache.getBuffer()));
      return true;
    });
    cacheValid.orElseGet(() -> {
      seekToFirst0(nativeHandle_);
      return true;
    });
  }

  @Override
  public void seekToLast() {
    sequentialCache.ifPresent(SequentialCache::clear);
    assert (isOwningHandle());
    seekToLast0(nativeHandle_);
  }

  @Override
  public void seek(final byte[] target) {
    // TODO (AP) - potential optimization to load prefetch cache on seek
    sequentialCache.ifPresent(SequentialCache::clear);
    assert (isOwningHandle());
    seek0(nativeHandle_, target, target.length);
  }

  @Override
  public void seekForPrev(final byte[] target) {
    // TODO (AP) - potential optimization to load prefetch cache on seek
    sequentialCache.ifPresent(SequentialCache::clear);
    assert (isOwningHandle());
    seekForPrev0(nativeHandle_, target, target.length);
  }

  @Override
  public void seek(final ByteBuffer target) {
    // TODO (AP) - potential optimization to load prefetch cache on seek
    sequentialCache.ifPresent(SequentialCache::clear);
    assert (isOwningHandle());
    if (target.isDirect()) {
      seekDirect0(nativeHandle_, target, target.position(), target.remaining());
    } else {
      seekByteArray0(nativeHandle_, target.array(), target.arrayOffset() + target.position(),
          target.remaining());
    }
    target.position(target.limit());
  }

  @Override
  public void seekForPrev(final ByteBuffer target) {
    // TODO (AP) - potential optimization to load prefetch cache on seek
    sequentialCache.ifPresent(SequentialCache::clear);
    assert (isOwningHandle());
    if (target.isDirect()) {
      seekForPrevDirect0(nativeHandle_, target, target.position(), target.remaining());
    } else {
      seekForPrevByteArray0(nativeHandle_, target.array(), target.arrayOffset() + target.position(),
          target.remaining());
    }
    target.position(target.limit());
  }

  @Override
  public void next() {
    Optional<Boolean> cacheValid = sequentialCache.map(cache -> {
      assert cache.beforeEnd();
      cache.next();
      return true;
    });
    cacheValid.orElseGet(() -> {
      assert (isOwningHandle());
      next0(nativeHandle_);
      return true;
    });
  }

  @Override
  public void prev() {
    // TODO (AP) - potential optimization to load prefetch cache on seek
    sequentialCache.ifPresent(SequentialCache::clear);
    assert (isOwningHandle());
    prev0(nativeHandle_);
  }

  @Override
  public void refresh() throws RocksDBException {
    sequentialCache.ifPresent(SequentialCache::clear);
    assert (isOwningHandle());
    refresh0(nativeHandle_);
  }

  @Override
  public void refresh(final Snapshot snapshot) throws RocksDBException {
    sequentialCache.ifPresent(SequentialCache::clear);
    assert (isOwningHandle());
    refresh1(nativeHandle_, snapshot.getNativeHandle());
  }

  @Override
  public void status() throws RocksDBException {
    assert (isOwningHandle());
    status0(nativeHandle_);
  }

  /**
   * <p>Deletes underlying C++ iterator pointer.</p>
   *
   * <p>Note: the underlying handle can only be safely deleted if the parent
   * instance related to a certain RocksIterator is still valid and initialized.
   * Therefore {@code disposeInternal()} checks if the parent is initialized
   * before freeing the native handle.</p>
   */
  @Override
  protected void disposeInternal() {
      if (parent_.isOwningHandle()) {
        disposeInternal(nativeHandle_);
      }
  }

  protected static class SequentialCache {
    // content instantiated by the C++ native code is an array of bytes
    // There are n {@code kvCount} key,value pairs
    // The structure of the buffer is therefore:
    // flags (4) - currently just bit 0 for kFlagNextIsValid,
    //   so we don't explicitly twiddle a flags record yet
    // kv-count (4)
    // (key0-size,value0-size),key0,value0,
    // (key1-size,value1-size),key1,value1,
    // ...
    // (keyn-1-size,valuen-1-size),...
    private final static int kFlagsOffset = 0;
    private final static int kCountOffset = Integer.BYTES;
    private ByteBuffer buffer;
    private int kvCount = 0;
    private int kvPos = 0;

    private int entryOffset;

    SequentialCache(final int size) {
      if (size > 0) {
        buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
      } else {
        buffer = null;
      }
    }

    final ByteBuffer getBuffer() {
      return buffer;
    }

    /**
     * Apply a fetched as the new value of the cache
     *
     * @param newBuffer the buffer containing some cached data
     */
    void reset(final ByteBuffer newBuffer) {
      if (newBuffer != buffer) {
        buffer = newBuffer;
      }
      kvPos = 0;
      if (buffer == null) {
        kvCount = 0;
      } else {
        kvCount = buffer.getInt(kCountOffset);
        entryOffset = Integer.BYTES * 2;
      }
    }

    void clear() {
      kvCount = 0;
      kvPos = 0;
    }

    /**
     * Does this cache have relevant cached data ?
     *
     * @return true iff the cache contents are relevant/correct for the current position
     */
    boolean beforeEnd() {
      return kvPos < kvCount;
    }

    public void next() {
      kvPos++;
      int keySize = buffer.getInt(entryOffset);
      int valueSize = buffer.getInt(entryOffset + Integer.BYTES);
      entryOffset +=
          2 * Integer.BYTES + pad(keySize, Integer.BYTES) + pad(valueSize, Integer.BYTES);
    }

    public byte[] key() {
      int keySize = buffer.getInt(entryOffset);
      byte[] keyBuffer = new byte[keySize];
      buffer.position(entryOffset + 2 * Integer.BYTES);
      buffer.get(keyBuffer);

      return keyBuffer;
    }

    public int key(byte[] key, int offset, int length) {
      int keySize = buffer.getInt(entryOffset);
      buffer.position(entryOffset + 2 * Integer.BYTES);
      buffer.get(key, offset, Math.min(keySize, length));
      return keySize;
    }

    public int key(ByteBuffer key) {
      int keySize = buffer.getInt(entryOffset);
      buffer.position(entryOffset + 2 * Integer.BYTES);
      ByteBuffer keySlice = buffer.slice();
      keySlice.limit(Math.min(keySize, key.capacity()));
      key.put(keySlice).flip();
      return keySize;
    }

    public byte[] value() {
      int keySize = buffer.getInt(entryOffset);
      int valueSize = buffer.getInt(entryOffset + Integer.BYTES);
      byte[] valueBuffer = new byte[valueSize];
      buffer.position(entryOffset + 2 * Integer.BYTES + pad(keySize, Integer.BYTES));
      buffer.get(valueBuffer);

      return valueBuffer;
    }

    public int value(byte[] value, int offset, int length) {
      int keySize = buffer.getInt(entryOffset);
      int valueSize = buffer.getInt(entryOffset + Integer.BYTES);
      buffer.position(entryOffset + 2 * Integer.BYTES + pad(keySize, Integer.BYTES));
      buffer.get(value, offset, Math.min(valueSize, length));
      return valueSize;
    }

    public int value(ByteBuffer value) {
      int keySize = buffer.getInt(entryOffset);
      int valueSize = buffer.getInt(entryOffset + Integer.BYTES);
      buffer.position(entryOffset + 2 * Integer.BYTES + pad(keySize, Integer.BYTES));
      ByteBuffer valueSlice = buffer.slice();
      valueSlice.limit(Math.min(valueSize, value.capacity()));
      value.put(valueSlice).flip();
      return valueSize;
    }
  }

  private static int pad(final int size, final int padding) {
    return (size + padding - 1) & -padding;
  }

  abstract boolean isValid0(long handle);
  abstract void seekToFirst0(long handle);
  abstract ByteBuffer seekToFirst0(long handle, ByteBuffer buffer);
  abstract void seekToLast0(long handle);
  abstract void next0(long handle);
  abstract ByteBuffer prefetch0(long handle, ByteBuffer buffer);
  abstract void prev0(long handle);
  abstract void refresh0(long handle) throws RocksDBException;
  abstract void refresh1(long handle, long snapshotHandle) throws RocksDBException;
  abstract void seek0(long handle, byte[] target, int targetLen);
  abstract void seekForPrev0(long handle, byte[] target, int targetLen);
  abstract void seekDirect0(long handle, ByteBuffer target, int targetOffset, int targetLen);
  abstract void seekForPrevDirect0(long handle, ByteBuffer target, int targetOffset, int targetLen);
  abstract void seekByteArray0(long handle, byte[] target, int targetOffset, int targetLen);
  abstract void seekForPrevByteArray0(long handle, byte[] target, int targetOffset, int targetLen);

  abstract void status0(long handle) throws RocksDBException;
}
