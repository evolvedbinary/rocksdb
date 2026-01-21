package org.rocksdb;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MergeOperatorV2 extends RocksCallbackObject implements MergeOperator {
  private final String operatorName;

  public MergeOperatorV2(final String operatorName) {
    super(new long[] {toCString(operatorName)});
    this.operatorName = operatorName;
  }

  /**
   * This is the callback method which supplies the values to be merged for the provided key.
   * The parameters are only valid until the method returns, and must be deep copied if you wish to
   * retain them.
   *
   * @param key The key associated with the merge operation.
   * @param existingValue The existing value of the current key, null means that the value doesn't
   *     exist.
   * @param operand A list of operands to apply
   * @return The method should return the result of merging the supplied values with the implemented
   *     merge algorithm.
   */
  public abstract MergeOperatorOutput fullMergeV2(
      final ByteBuffer key, final ByteBuffer existingValue, final List<ByteBuffer> operand);

  /**
   * This is the callback method for FullMergeV3 which supports wide-column entities.
   * Override this method to enable wide-column merge support.
   * The parameters are only valid until the method returns, and must be deep copied if you wish to retain them.
   *
   * <p>Default implementation: extracts plain value from existingColumns and delegates to fullMergeV2.
   *
   * @param key The key associated with the merge operation.
   * @param existingColumns The existing wide columns, null if no existing value.
   * @param operand A list of operands to apply
   * @return The method should return the result of merging with wide columns support.
   */
  public MergeOperatorOutput fullMergeV3(
      final ByteBuffer key, final WideColumn[] existingColumns, final List<ByteBuffer> operand) {
    // Default: extract plain value from default column and delegate to V2
    ByteBuffer existingValue = null;
    if (existingColumns != null && existingColumns.length > 0) {
      // Find default column (empty name)
      for (WideColumn col : existingColumns) {
        if (col.name().length == 0) {
          existingValue = ByteBuffer.wrap(col.value());
          break;
        }
      }
    }
    return fullMergeV2(key, existingValue, operand);
  }

  final MergeOperatorOutput mergeInternal(
      final ByteBuffer key, final ByteBuffer existingValue, final ByteBuffer[] operand) {
    List<ByteBuffer> operandList =
        Arrays.stream(operand).map(ByteBuffer::asReadOnlyBuffer).collect(Collectors.toList());

    return fullMergeV2(key.asReadOnlyBuffer(), existingValue.asReadOnlyBuffer(), operandList);
  }

  final MergeOperatorOutput mergeInternalV3(
      final ByteBuffer key, final WideColumn[] existingColumns, final ByteBuffer[] operand) {
    List<ByteBuffer> operandList =
        Arrays.stream(operand).map(ByteBuffer::asReadOnlyBuffer).collect(Collectors.toList());

    return fullMergeV3(key.asReadOnlyBuffer(), existingColumns, operandList);
  }

  @Override
  public long getNativeHandle() {
    return nativeHandle_;
  }

  @Override
  protected long initializeNative(long... nativeParameterHandles) {
    return newMergeOperator(nativeParameterHandles[0]);
  }

  @Override
  protected void disposeInternal() {
    disposeInternal(nativeHandle_);
  }

  public String getOperatorName() {
    return operatorName;
  }

  private native long newMergeOperator(long operatorName);

  private static native long toCString(String operatorName);

  private static native void disposeInternal(long nativeHandle);
}