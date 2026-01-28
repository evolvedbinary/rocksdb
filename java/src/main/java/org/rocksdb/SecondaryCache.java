// Copyright (c) 2011-present, Facebook, Inc.  All rights reserved.
//  This source code is licensed under both the GPLv2 (found in the
//  COPYING file in the root directory) and Apache 2.0 License
//  (found in the LICENSE.Apache file in the root directory).

package org.rocksdb;

/**
 * SecondaryCache is a Java wrapper class for the C++ SecondaryCache.
 * It provides a persistent cache layer that can be used as a second-tier
 * cache below the block cache.
 * <p>
 * SecondaryCache allows for using non-volatile storage (like SSDs) or
 * compressed RAM as a cache tier, providing more capacity at lower cost
 * than keeping everything in the primary block cache.
 * <p>
 * As a descendent of {@link RocksObject}, this class is {@link AutoCloseable}
 * and will be automatically released if opened in the preamble of a try with
 * resources block.
 */
public abstract class SecondaryCache extends RocksObject {
  
  /**
   * Constructor for SecondaryCache
   *
   * @param nativeHandle the native handle to the C++ SecondaryCache object
   */
  protected SecondaryCache(final long nativeHandle) {
    super(nativeHandle);
  }

  @Override
  protected final void disposeInternal(final long handle) {
    disposeInternalJni(handle);
  }

  /**
   * Creates a new CompressedSecondaryCache with the specified capacity.
   * CompressedSecondaryCache stores cache items in compressed form,
   * allowing more data to fit in memory.
   *
   * @param capacity the capacity of the secondary cache in bytes
   * @return a new CompressedSecondaryCache instance
   */
  public static SecondaryCache newCompressedSecondaryCache(final long capacity) {
    return new CompressedSecondaryCache(capacity);
  }

  /**
   * Creates a new CompressedSecondaryCache with detailed options.
   *
   * @param capacity the capacity of the secondary cache in bytes
   * @param numShardBits the number of bits used for sharding (controls concurrency)
   * @param strictCapacityLimit if true, cache operations fail when capacity is exceeded
   * @param highPriPoolRatio ratio of cache reserved for high priority entries (0.0 to 1.0)
   * @return a new CompressedSecondaryCache instance
   */
  public static SecondaryCache newCompressedSecondaryCache(
      final long capacity,
      final int numShardBits,
      final boolean strictCapacityLimit,
      final double highPriPoolRatio) {
    return new CompressedSecondaryCache(
        capacity, numShardBits, strictCapacityLimit, highPriPoolRatio);
  }

  /**
   * CompressedSecondaryCache implementation that stores cached items
   * in compressed format to maximize capacity.
   */
  private static class CompressedSecondaryCache extends SecondaryCache {
    
    private CompressedSecondaryCache(final long capacity) {
      super(newCompressedSecondaryCacheInstance(capacity));
    }

    private CompressedSecondaryCache(
        final long capacity,
        final int numShardBits,
        final boolean strictCapacityLimit,
        final double highPriPoolRatio) {
      super(newCompressedSecondaryCacheInstance(
          capacity, numShardBits, strictCapacityLimit, highPriPoolRatio));
    }

    private static native long newCompressedSecondaryCacheInstance(long capacity);
    
    private static native long newCompressedSecondaryCacheInstance(
        long capacity, int numShardBits, boolean strictCapacityLimit, double highPriPoolRatio);
  }

  private static native void disposeInternalJni(final long handle);
}
