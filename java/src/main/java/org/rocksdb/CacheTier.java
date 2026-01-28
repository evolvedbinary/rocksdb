package org.rocksdb;

/**
 * The control option of how the cache tiers will be used.
 * Currently RocksDB supports block cache (volatile tier) and
 * secondary cache (non-volatile tier).
 */
public enum CacheTier {
  /**
   * Volatile tier only (traditional block cache)
   */
  VOLATILE_TIER((byte) 0x00),

  /**
   * Volatile compressed tier
   */
  VOLATILE_COMPRESSED_TIER((byte) 0x01),

  /**
   * Non-volatile block tier (secondary cache)
   */
  NON_VOLATILE_BLOCK_TIER((byte) 0x02);

  private final byte value;

  CacheTier(final byte value) {
    this.value = value;
  }

  /**
   * Get the internal byte value.
   *
   * @return the internal byte value
   */
  public byte getValue() {
    return value;
  }

  /**
   * Get CacheTier by byte value.
   *
   * @param value the byte value representing CacheTier
   * @return {@link CacheTier} instance or null
   * @throws IllegalArgumentException if an invalid value is provided
   */
  public static CacheTier getCacheTier(final byte value) {
    for (final CacheTier cacheTier : CacheTier.values()) {
      if (cacheTier.getValue() == value) {
        return cacheTier;
      }
    }
    throw new IllegalArgumentException("Illegal value provided for CacheTier: " + value);
  }
}
