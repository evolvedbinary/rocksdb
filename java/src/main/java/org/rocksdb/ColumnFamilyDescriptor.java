// Copyright (c) 2011-present, Facebook, Inc.  All rights reserved.
//  This source code is licensed under both the GPLv2 (found in the
//  COPYING file in the root directory) and Apache 2.0 License
//  (found in the LICENSE.Apache file in the root directory).

package org.rocksdb;

/**
 * <p>Describes a column family with a
 * name and respective Options.</p>
 */
public class ColumnFamilyDescriptor extends RocksObject {
  private final ColumnFamilyOptions columnFamilyOptions;
  private final boolean implicitlyCreatedColumnFamilyOptions;

  /**
   * <p>Creates a new Column Family using a name and default
   * options,</p>
   *
   * @param columnFamilyName name of column family.
   * @since 3.10.0
   */
  public ColumnFamilyDescriptor(final byte[] columnFamilyName) throws RocksDBException {
    this(columnFamilyName, new ColumnFamilyOptions(), true);
  }

  /**
   * <p>Creates a new Column Family using a name and custom
   * options. This constructor make copy of ColumnFamilyOptions.</p>
   *
   * @param columnFamilyName name of column family.
   * @param columnFamilyOptions options to be used with column family.
   * @since 3.10.0
   */
  public ColumnFamilyDescriptor(final byte[] columnFamilyName,
      final ColumnFamilyOptions columnFamilyOptions) throws RocksDBException {
    this(columnFamilyName, columnFamilyOptions, false);
  }

  private ColumnFamilyDescriptor(final byte[] columnFamilyName,
      final ColumnFamilyOptions columnFamilyOptions, boolean implicitlyCreatedColumnFamilyOptions)
      throws RocksDBException {
    super(createNativeInstance(columnFamilyName, columnFamilyOptions));
    this.columnFamilyOptions = columnFamilyOptions;
    this.implicitlyCreatedColumnFamilyOptions = implicitlyCreatedColumnFamilyOptions;
  }

  private static long createNativeInstance(final byte[] columnFamilyName,
      final ColumnFamilyOptions columnFamilyOptions) throws RocksDBException {
    final long instance = createNativeInstance(columnFamilyName, columnFamilyOptions.nativeHandle_);
    if (instance == 0) {
      throw new RocksDBException("Can't create instance of ColumnFamilyDescriptor");
    } else {
      return instance;
    }
  }

  /**
   * Retrieve name of column family.
   *
   * @return column family name.
   * @since 3.10.0
   */
  public byte[] getName() {
    return getName(nativeHandle_);
  }

  /**
   * Retrieve assigned options instance.
   *
   * @return Options instance assigned to this instance.
   */
  public ColumnFamilyOptions getOptions() {
    return columnFamilyOptions;
  }
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ColumnFamilyDescriptor that = (ColumnFamilyDescriptor) o; // NOPMD - CloseResource
    return nativeHandle_ == that.getNativeHandle();
  }

  @Override
  public int hashCode() {
    return 31 * ((int) (nativeHandle_ ^ (nativeHandle_ >>> 32)));
  }
  @Override
  protected void disposeInternal(final long handle) {
    disposeJni(nativeHandle_);
    if (implicitlyCreatedColumnFamilyOptions) {
      columnFamilyOptions.close();
    }
  }
  private static native void disposeJni(final long nativeHandle);
  private static native long createNativeInstance(
      final byte[] columnFamilyName, final long columnFamilyOptions);
  private static native byte[] getName(final long nativeHandle);
}
