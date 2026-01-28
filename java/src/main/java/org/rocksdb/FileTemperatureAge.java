package org.rocksdb;

/**
 * EXPERIMENTAL
 * Age (in seconds) threshold for different file temperatures.
 * When all the data in a file is older than the specified age,
 * RocksDB will compact the file to the specified temperature.
 */
public class FileTemperatureAge {
  private Temperature temperature;
  private long age;

  /**
   * Default constructor with unknown temperature and zero age
   */
  public FileTemperatureAge() {
    this.temperature = Temperature.UNKNOWN;
    this.age = 0;
  }

  /**
   * Constructor with specified temperature and age
   *
   * @param temperature the target temperature for old files
   * @param age the age threshold in seconds
   */
  public FileTemperatureAge(final Temperature temperature, final long age) {
    this.temperature = temperature;
    this.age = age;
  }

  /**
   * Get the temperature
   *
   * @return the temperature
   */
  public Temperature getTemperature() {
    return temperature;
  }

  /**
   * Set the temperature
   *
   * @param temperature the temperature to set
   * @return this FileTemperatureAge instance
   */
  public FileTemperatureAge setTemperature(final Temperature temperature) {
    this.temperature = temperature;
    return this;
  }

  /**
   * Get the age threshold in seconds
   *
   * @return the age in seconds
   */
  public long getAge() {
    return age;
  }

  /**
   * Set the age threshold in seconds
   *
   * @param age the age in seconds
   * @return this FileTemperatureAge instance
   */
  public FileTemperatureAge setAge(final long age) {
    this.age = age;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FileTemperatureAge that = (FileTemperatureAge) o;
    return age == that.age && temperature == that.temperature;
  }

  @Override
  public int hashCode() {
    return 31 * temperature.hashCode() + Long.hashCode(age);
  }

  @Override
  public String toString() {
    return "FileTemperatureAge{temperature=" + temperature + ", age=" + age + "s}";
  }
}
