// Copyright (c) 2011-present, Facebook, Inc.  All rights reserved.
//  This source code is licensed under both the GPLv2 (found in the
//  COPYING file in the root directory) and Apache 2.0 License
//  (found in the LICENSE.Apache file in the root directory).

package org.rocksdb;

/**
 * Advanced cache configuration options for fine-tuning cache behavior.
 * These options control memory allocation, metadata charging, and
 * secondary cache integration.
 */
public class AdvancedCacheOptions {
  private long capacity;
  private int numShardBits;
  private boolean strictCapacityLimit;
  private CacheMetadataChargePolicy metadataChargePolicy;
  private SecondaryCache secondaryCache;
  private MemoryAllocator memoryAllocator;

  /**
   * Default constructor with sensible defaults
   */
  public AdvancedCacheOptions() {
    this.capacity = 8 * 1024 * 1024; // 8MB default
    this.numShardBits = -1; // Auto-determined
    this.strictCapacityLimit = false;
    this.metadataChargePolicy = CacheMetadataChargePolicy.FULL_CHARGE_CACHE_METADATA;
    this.secondaryCache = null;
    this.memoryAllocator = null;
  }

  /**
   * Get the cache capacity in bytes
   *
   * @return the capacity
   */
  public long getCapacity() {
    return capacity;
  }

  /**
   * Set the cache capacity in bytes
   *
   * @param capacity the capacity to set
   * @return this options instance
   */
  public AdvancedCacheOptions setCapacity(final long capacity) {
    this.capacity = capacity;
    return this;
  }

  /**
   * Get the number of shard bits.
   * Cache is sharded into 2^numShardBits shards by hash of key.
   *
   * @return the number of shard bits
   */
  public int getNumShardBits() {
    return numShardBits;
  }

  /**
   * Set the number of shard bits.
   * If &lt; 0, a good default is chosen based on capacity.
   *
   * @param numShardBits the number of shard bits
   * @return this options instance
   */
  public AdvancedCacheOptions setNumShardBits(final int numShardBits) {
    this.numShardBits = numShardBits;
    return this;
  }

  /**
   * Check if strict capacity limit is enabled
   *
   * @return true if strict capacity limit is enabled
   */
  public boolean isStrictCapacityLimit() {
    return strictCapacityLimit;
  }

  /**
   * Set strict capacity limit.
   * If true, insert operations fail when capacity is exceeded.
   * If false, insert never fails (unreferenced entries are evicted).
   *
   * @param strictCapacityLimit whether to enforce strict capacity limit
   * @return this options instance
   */
  public AdvancedCacheOptions setStrictCapacityLimit(final boolean strictCapacityLimit) {
    this.strictCapacityLimit = strictCapacityLimit;
    return this;
  }

  /**
   * Get the metadata charge policy
   *
   * @return the metadata charge policy
   */
  public CacheMetadataChargePolicy getMetadataChargePolicy() {
    return metadataChargePolicy;
  }

  /**
   * Set the metadata charge policy.
   * Controls whether cache metadata overhead counts against capacity.
   *
   * @param metadataChargePolicy the policy to set
   * @return this options instance
   */
  public AdvancedCacheOptions setMetadataChargePolicy(
      final CacheMetadataChargePolicy metadataChargePolicy) {
    this.metadataChargePolicy = metadataChargePolicy;
    return this;
  }

  /**
   * Get the secondary cache instance
   *
   * @return the secondary cache, or null if not set
   */
  public SecondaryCache getSecondaryCache() {
    return secondaryCache;
  }

  /**
   * Set the secondary cache for non-volatile tier caching
   *
   * @param secondaryCache the secondary cache instance
   * @return this options instance
   */
  public AdvancedCacheOptions setSecondaryCache(final SecondaryCache secondaryCache) {
    this.secondaryCache = secondaryCache;
    return this;
  }

  /**
   * Get the memory allocator
   *
   * @return the memory allocator, or null for system allocator
   */
  public MemoryAllocator getMemoryAllocator() {
    return memoryAllocator;
  }

  /**
   * Set a custom memory allocator for cache blocks
   *
   * @param memoryAllocator the memory allocator instance
   * @return this options instance
   */
  public AdvancedCacheOptions setMemoryAllocator(final MemoryAllocator memoryAllocator) {
    this.memoryAllocator = memoryAllocator;
    return this;
  }

  /**
   * Metadata charge policy enum
   */
  public enum CacheMetadataChargePolicy {
    /**
     * Only the charge of each entry counts against capacity
     */
    DONT_CHARGE_CACHE_METADATA((byte) 0),

    /**
     * Cache metadata overhead also counts against capacity
     */
    FULL_CHARGE_CACHE_METADATA((byte) 1);

    private final byte value;

    CacheMetadataChargePolicy(final byte value) {
      this.value = value;
    }

    public byte getValue() {
      return value;
    }

    public static CacheMetadataChargePolicy fromValue(final byte value) {
      for (final CacheMetadataChargePolicy policy : CacheMetadataChargePolicy.values()) {
        if (policy.getValue() == value) {
          return policy;
        }
      }
      throw new IllegalArgumentException("Unknown CacheMetadataChargePolicy value: " + value);
    }
  }
}
