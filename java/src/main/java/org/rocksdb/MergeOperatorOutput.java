package org.rocksdb;

import java.nio.ByteBuffer;

/*
    std::string& new_value;
    Slice& existing_operand;
    OpFailureScope op_failure_scope = OpFailureScope::kDefault;
 */
public class MergeOperatorOutput {
  public enum OpFailureScope {
    Default(0),
    TryMerge(1),
    MustMerge(2),
    OpFailureScopeMax(3);
    private final int status;
    OpFailureScope(int status) {
      this.status = status;
    }
  }

  private ByteBuffer directValue;
  private WideColumn[] wideColumns;
  private OpFailureScope op_failure_scope = OpFailureScope.Default;

  public MergeOperatorOutput(final ByteBuffer directValue) {
    this.directValue = directValue;
  }

  public MergeOperatorOutput(final ByteBuffer directValue, final OpFailureScope op_failure_scope) {
    this.directValue = directValue;
    this.op_failure_scope = op_failure_scope;
  }

  /**
   * Constructor for wide columns output (FullMergeV3).
   *
   * @param wideColumns the wide columns result
   */
  public MergeOperatorOutput(final WideColumn[] wideColumns) {
    this.wideColumns = wideColumns;
  }

  /**
   * Constructor for wide columns output with failure scope (FullMergeV3).
   *
   * @param wideColumns the wide columns result
   * @param op_failure_scope the failure scope
   */
  public MergeOperatorOutput(final WideColumn[] wideColumns, final OpFailureScope op_failure_scope) {
    this.wideColumns = wideColumns;
    this.op_failure_scope = op_failure_scope;
  }

  public ByteBuffer getDirectValue() {
    return directValue;
  }

  /**
   * Gets the wide columns result (FullMergeV3).
   *
   * @return the wide columns array, or null if result is a plain value
   */
  public WideColumn[] getWideColumns() {
    return wideColumns;
  }

  /**
   * Checks if this output contains wide columns.
   *
   * @return true if wide columns, false if plain value
   */
  public boolean isWideColumns() {
    return wideColumns != null;
  }

  public OpFailureScope getOp_failure_scope() {
    return op_failure_scope;
  }

  /**
   * For JNI. Called from JniMergeOperatorV2
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private int getOpStatus() {
    return this.op_failure_scope.status;
  }
}