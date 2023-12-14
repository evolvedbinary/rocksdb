// Copyright (c) 2011-present, Facebook, Inc.  All rights reserved.
//  This source code is licensed under both the GPLv2 (found in the
//  COPYING file in the root directory) and Apache 2.0 License
//  (found in the LICENSE.Apache file in the root directory).

package org.rocksdb;

/**
 * The operation stage.
 */
public enum OperationStage {
  /**
   * Unknown.
   */
  STAGE_UNKNOWN((byte)0x0),

  /**
   * Flush.
   */
  STAGE_FLUSH_RUN((byte)0x1),

  /**
   * Flush writing Level 0.
   */
  STAGE_FLUSH_WRITE_L0((byte)0x2),

  /**
   * Preparing compaction.
   */
  STAGE_COMPACTION_PREPARE((byte)0x3),

  /**
   * Compaction.
   */
  STAGE_COMPACTION_RUN((byte)0x4),

  /**
   * Compaction processing a key-value.
   */
  STAGE_COMPACTION_PROCESS_KV((byte)0x5),

  /**
   * Installing compaction.
   */
  STAGE_COMPACTION_INSTALL((byte)0x6),

  /**
   * Compaction syncing a file.
   */
  STAGE_COMPACTION_SYNC_FILE((byte)0x7),

  /**
   * Picking Memtable(s) to flush.
   */
  STAGE_PICK_MEMTABLES_TO_FLUSH((byte)0x8),

  /**
   * Rolling back Memtable(s).
   */
  STAGE_MEMTABLE_ROLLBACK((byte)0x9),

  /**
   * Installing Memtable flush results.
   */
  STAGE_MEMTABLE_INSTALL_FLUSH_RESULTS((byte)0xA);

  private final byte value;

  OperationStage(final byte value) {
    this.value = value;
  }

  /**
   * Get the internal representation value.
   *
   * @return the internal representation value.
   */
  byte getValue() {
    return value;
  }

  /**
   * Get the Operation stage from the internal representation value.
   *
   * @param value the internal representation value.
   *
   * @return the operation stage
   *
   * @throws IllegalArgumentException if the value does not match
   *     an OperationStage
   */
  static OperationStage fromValue(final byte value)
      throws IllegalArgumentException {
    for (final OperationStage threadType : OperationStage.values()) {
      if (threadType.value == value) {
        return threadType;
      }
    }
    throw new IllegalArgumentException(
        "Unknown value for OperationStage: " + value);
  }
}
