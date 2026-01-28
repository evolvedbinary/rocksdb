package org.rocksdb;

/**
 * Temperature of a file. Used to pass to FileSystem for a different
 * placement and/or coding.
 * Reserve some numbers in the middle for potential future use.
 */
public enum Temperature {
  /**
   * Unknown temperature, default value
   */
  UNKNOWN((byte) 0),

  /**
   * Hot data, accessed frequently
   */
  HOT((byte) 0x04),

  /**
   * Warm data, accessed occasionally  
   */
  WARM((byte) 0x08),

  /**
   * Cool data, less frequently accessed
   */
  COOL((byte) 0x0A),

  /**
   * Cold data, rarely accessed
   */
  COLD((byte) 0x0C),

  /**
   * Ice data, very rarely accessed (archive tier)
   */
  ICE((byte) 0x10);

  private final byte value;

  Temperature(final byte value) {
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
   * Get Temperature by byte value.
   *
   * @param value the byte value representing Temperature
   * @return {@link Temperature} instance
   * @throws IllegalArgumentException if an invalid value is provided
   */
  public static Temperature getTemperature(final byte value) {
    for (final Temperature temperature : Temperature.values()) {
      if (temperature.getValue() == value) {
        return temperature;
      }
    }
    throw new IllegalArgumentException("Illegal value provided for Temperature: " + value);
  }
}
