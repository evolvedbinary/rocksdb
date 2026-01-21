package org.rocksdb;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * WideColumn represents a single wide-column entity.
 * A wide column consists of a column name and a column value.
 */
public class WideColumn {
  private final byte[] name;
  private final byte[] value;

  /**
   * Constructs a WideColumn with byte arrays.
   *
   * @param name the column name
   * @param value the column value
   */
  public WideColumn(final byte[] name, final byte[] value) {
    this.name = name;
    this.value = value;
  }

  /**
   * Constructs a WideColumn with strings (UTF-8 encoded).
   *
   * @param name the column ngit fetch upstream pull/12122/head:pr-12122ame
   * @param value the column value
   */
  public WideColumn(final String name, final String value) {
    this.name = name.getBytes(StandardCharsets.UTF_8);
    this.value = value.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Gets the column name.
   *
   * @return the column name as byte array
   */
  public byte[] name() {
    return name;
  }

  /**
   * Gets the column value.
   *
   * @return the column value as byte array
   */
  public byte[] value() {
    return value;
  }

  /**
   * Gets the column name as a UTF-8 string.
   *
   * @return the column name as string
   */
  public String nameAsString() {
    return new String(name, StandardCharsets.UTF_8);
  }

  /**
   * Gets the column value as a UTF-8 string.
   *
   * @return the column value as string
   */
  public String valueAsString() {
    return new String(value, StandardCharsets.UTF_8);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WideColumn that = (WideColumn) o;
    return Arrays.equals(name, that.name) && Arrays.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(name);
    result = 31 * result + Arrays.hashCode(value);
    return result;
  }
}
