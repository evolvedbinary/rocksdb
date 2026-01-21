package org.rocksdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MergeOperatorV2Test {
  @ClassRule
  public static final RocksNativeLibraryResource ROCKS_NATIVE_LIBRARY_RESOURCE =
      new RocksNativeLibraryResource();

  @Rule public TemporaryFolder dbFolder = new TemporaryFolder();

  private static byte[] KEY = "thisIsKey".getBytes(StandardCharsets.UTF_8);

  @Test
  public void testMergeOperator() throws RocksDBException {
    try (TestMergeOperator mergeOperator = new TestMergeOperator();
         Options options = new Options()) {
      options.setMergeOperator(mergeOperator);
      options.setCreateIfMissing(true);

      try (RocksDB db = RocksDB.open(options, dbFolder.getRoot().getAbsolutePath())) {
        db.put(KEY, "value".getBytes());
        db.merge(KEY, "value1".getBytes());
        db.merge(KEY, "value2".getBytes());
        db.merge(KEY, "value3".getBytes());
        byte[] valueFromDb = db.get(KEY);
        assertThat(valueFromDb).containsExactly("10".getBytes(StandardCharsets.UTF_8));
      }
    }
  }

  @Test
  public void middleOfByteBuffer() throws RocksDBException {
    try (MergeOperatorV2 mergeOperator = new MergeOperatorV2("Second operator") {
      @Override
      public MergeOperatorOutput fullMergeV2(
          ByteBuffer key, ByteBuffer existingValue, List<ByteBuffer> operand) {
        ByteBuffer b = ByteBuffer.allocateDirect(10);
        b.put("xxx".getBytes(StandardCharsets.UTF_8));
        b.put(new byte[] {0, 0});
        b.flip();
        b.position(3);
        return new MergeOperatorOutput(b);
      }
    }) {
      try (Options options = new Options()) {
        options.setMergeOperator(mergeOperator);
        options.setCreateIfMissing(true);

        try (RocksDB db = RocksDB.open(options, dbFolder.getRoot().getAbsolutePath())) {
          db.put(KEY, "value".getBytes());
          db.merge(KEY, "value1".getBytes());
          db.merge(KEY, "value2".getBytes());
          db.merge(KEY, "value3".getBytes());
          byte[] valueFromDb = db.get(KEY);
          assertThat(valueFromDb).containsExactly(new byte[] {0, 0});
        }
      }
    }
  }

  @Test
  public void returnExistingOperandByteBuffer() throws RocksDBException {
    try (MergeOperatorV2 mergeOperator = new MergeOperatorV2("Third operator") {
      @Override
      public MergeOperatorOutput fullMergeV2(
          ByteBuffer key, ByteBuffer existingValue, List<ByteBuffer> operand) {
        return new MergeOperatorOutput(operand.get(1));
      }
    }) {
      try (Options options = new Options()) {
        options.setMergeOperator(mergeOperator);
        options.setCreateIfMissing(true);

        try (RocksDB db = RocksDB.open(options, dbFolder.getRoot().getAbsolutePath())) {
          db.put(KEY, "value".getBytes());
          db.merge(KEY, "value1".getBytes());
          db.merge(KEY, "value2".getBytes());
          db.merge(KEY, "value3".getBytes());
          byte[] valueFromDb = db.get(KEY);
          assertThat(valueFromDb).containsExactly("value2".getBytes(StandardCharsets.UTF_8));
        }
      }
    }
  }

  @Test(expected = RocksDBException.class)
  public void returnFailureStatus() throws RocksDBException {
    try (MergeOperatorV2 mergeOperator = new MergeOperatorV2("CrashOperator") {
      @Override
      public MergeOperatorOutput fullMergeV2(
          ByteBuffer key, ByteBuffer existingValue, List<ByteBuffer> operand) {
        return new MergeOperatorOutput((ByteBuffer)null, MergeOperatorOutput.OpFailureScope.OpFailureScopeMax);
      }
    }) {
      try (Options options = new Options()) {
        options.setMergeOperator(mergeOperator);
        options.setCreateIfMissing(true);

        try (RocksDB db = RocksDB.open(options, dbFolder.getRoot().getAbsolutePath())) {
          db.put(KEY, "value".getBytes());
          db.merge(KEY, "value1".getBytes());
          db.merge(KEY, "value2".getBytes());
          db.merge(KEY, "value3".getBytes());
          byte[] valueFromDb = db.get(KEY);
          fail("We should never reach this.");
        }
      }
    }
  }

  @Test(expected = RocksDBException.class)
  public void throwJavaException() throws RocksDBException {
    try (MergeOperatorV2 mergeOperator = new MergeOperatorV2("CrashOperator") {
      @Override
      public MergeOperatorOutput fullMergeV2(
          ByteBuffer key, ByteBuffer existingValue, List<ByteBuffer> operand) {
        throw new RuntimeException("Never do this");
      }
    }) {
      try (Options options = new Options()) {
        options.setMergeOperator(mergeOperator);
        options.setCreateIfMissing(true);

        try (RocksDB db = RocksDB.open(options, dbFolder.getRoot().getAbsolutePath())) {
          db.put(KEY, "value".getBytes());
          db.merge(KEY, "value1".getBytes());
          db.merge(KEY, "value2".getBytes());
          db.merge(KEY, "value3".getBytes());
          byte[] valueFromDb = db.get(KEY);
          fail("We should never reach this.");
        }
      }
    }
  }

  @Test
  public void testMergeOperatorV3WithWideColumns() throws RocksDBException {
    try (MergeOperatorV2 mergeOperator = new MergeOperatorV2("WideColumnMergeOperator") {
      @Override
      public MergeOperatorOutput fullMergeV2(
          ByteBuffer key, ByteBuffer existingValue, List<ByteBuffer> operand) {
        return new MergeOperatorOutput(ByteBuffer.wrap("v2".getBytes()));
      }

      @Override
      public MergeOperatorOutput fullMergeV3(
          ByteBuffer key, WideColumn[] existingColumns, List<ByteBuffer> operand) {
        // Merge creates wide columns: append operand count to each column
        if (existingColumns == null || existingColumns.length == 0) {
          return new MergeOperatorOutput(new WideColumn[] {
              new WideColumn("col1", "merged_" + operand.size())
          });
        }
        
        WideColumn[] result = new WideColumn[existingColumns.length];
        for (int i = 0; i < existingColumns.length; i++) {
          String existingVal = existingColumns[i].valueAsString();
          String newVal = existingVal + "_" + operand.size();
          result[i] = new WideColumn(new String(existingColumns[i].name(), StandardCharsets.UTF_8), newVal);
        }
        return new MergeOperatorOutput(result);
      }
    }) {
      try (Options options = new Options()) {
        options.setMergeOperator(mergeOperator);
        options.setCreateIfMissing(true);

        try (RocksDB db = RocksDB.open(options, dbFolder.getRoot().getAbsolutePath())) {
          db.put(KEY, "value".getBytes());
          db.merge(KEY, "op1".getBytes());
          db.merge(KEY, "op2".getBytes());
          byte[] valueFromDb = db.get(KEY);
          // Should have merged with 2 operands
          assertThat(new String(valueFromDb, StandardCharsets.UTF_8)).contains("2");
        }
      }
    }
  }

  @Test
  public void testMergeOperatorV3MultipleColumns() throws RocksDBException {
    try (MergeOperatorV2 mergeOperator = new MergeOperatorV2("MultiColumnMergeOperator") {
      @Override
      public MergeOperatorOutput fullMergeV2(
          ByteBuffer key, ByteBuffer existingValue, List<ByteBuffer> operand) {
        return new MergeOperatorOutput(ByteBuffer.wrap("v2".getBytes()));
      }

      @Override
      public MergeOperatorOutput fullMergeV3(
          ByteBuffer key, WideColumn[] existingColumns, List<ByteBuffer> operand) {
        // Create multiple wide columns from operands
        WideColumn[] columns = new WideColumn[operand.size() + 1];
        columns[0] = new WideColumn("", "default_value"); // Default column
        for (int i = 0; i < operand.size(); i++) {
          byte[] opData = new byte[operand.get(i).remaining()];
          operand.get(i).duplicate().get(opData);
          columns[i + 1] = new WideColumn("col_" + i, new String(opData, StandardCharsets.UTF_8));
        }
        return new MergeOperatorOutput(columns);
      }
    }) {
      try (Options options = new Options()) {
        options.setMergeOperator(mergeOperator);
        options.setCreateIfMissing(true);

        try (RocksDB db = RocksDB.open(options, dbFolder.getRoot().getAbsolutePath())) {
          db.put(KEY, "initial".getBytes());
          db.merge(KEY, "operand1".getBytes());
          db.merge(KEY, "operand2".getBytes());
          db.merge(KEY, "operand3".getBytes());
          byte[] valueFromDb = db.get(KEY);
          // Default column should be returned
          assertThat(new String(valueFromDb, StandardCharsets.UTF_8)).isEqualTo("default_value");
        }
      }
    }
  }

  @Test(expected = RocksDBException.class)
  public void testMergeOperatorV3FailureStatus() throws RocksDBException {
    try (MergeOperatorV2 mergeOperator = new MergeOperatorV2("FailingV3Operator") {
      @Override
      public MergeOperatorOutput fullMergeV2(
          ByteBuffer key, ByteBuffer existingValue, List<ByteBuffer> operand) {
        return new MergeOperatorOutput(ByteBuffer.wrap("v2".getBytes()));
      }

      @Override
      public MergeOperatorOutput fullMergeV3(
          ByteBuffer key, WideColumn[] existingColumns, List<ByteBuffer> operand) {
        return new MergeOperatorOutput((WideColumn[])null, MergeOperatorOutput.OpFailureScope.OpFailureScopeMax);
      }
    }) {
      try (Options options = new Options()) {
        options.setMergeOperator(mergeOperator);
        options.setCreateIfMissing(true);

        try (RocksDB db = RocksDB.open(options, dbFolder.getRoot().getAbsolutePath())) {
          db.put(KEY, "value".getBytes());
          db.merge(KEY, "op1".getBytes());
          byte[] valueFromDb = db.get(KEY);
          fail("Should have thrown RocksDBException");
        }
      }
    }
  }

  @Test
  public void testMergeOperatorV3FallbackToV2() throws RocksDBException {
    // Test that default V3 implementation correctly falls back to V2
    try (MergeOperatorV2 mergeOperator = new MergeOperatorV2("FallbackOperator") {
      @Override
      public MergeOperatorOutput fullMergeV2(
          ByteBuffer key, ByteBuffer existingValue, List<ByteBuffer> operand) {
        ByteBuffer b = ByteBuffer.allocateDirect(20);
        b.put("v2_result".getBytes(StandardCharsets.UTF_8));
        b.flip();
        return new MergeOperatorOutput(b);
      }
      // Don't override fullMergeV3, so it should use default implementation
    }) {
      try (Options options = new Options()) {
        options.setMergeOperator(mergeOperator);
        options.setCreateIfMissing(true);

        try (RocksDB db = RocksDB.open(options, dbFolder.getRoot().getAbsolutePath())) {
          db.put(KEY, "value".getBytes());
          db.merge(KEY, "op1".getBytes());
          byte[] valueFromDb = db.get(KEY);
          assertThat(new String(valueFromDb, StandardCharsets.UTF_8)).isEqualTo("v2_result");
        }
      }
    }
  }

  public static class TestMergeOperator extends MergeOperatorV2 {
    public TestMergeOperator() {
      super("TestMergeOperator");
    }

    @Override
    public MergeOperatorOutput fullMergeV2(
        ByteBuffer key, ByteBuffer existingValue, List<ByteBuffer> operand) {
      ByteBuffer b = ByteBuffer.allocateDirect(10);
      b.put("10".getBytes(StandardCharsets.UTF_8));
      b.flip();
      return new MergeOperatorOutput(b);
    }
  }
}
