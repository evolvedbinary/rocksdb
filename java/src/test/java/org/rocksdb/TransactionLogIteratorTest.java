// Copyright (c) Facebook, Inc. and its affiliates. All Rights Reserved.
package org.rocksdb;

import java.io.File;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionLogIteratorTest {
  @RegisterExtension
  public static final RocksNativeLibraryResource ROCKS_NATIVE_LIBRARY_RESOURCE =
      new RocksNativeLibraryResource();

  @TempDir
  public File dbFolder;

  @Test
  public void transactionLogIterator() throws RocksDBException {
    try (final Options options = new Options()
        .setCreateIfMissing(true);
         final RocksDB db = RocksDB.open(options,
             dbFolder.getAbsolutePath());
         final TransactionLogIterator transactionLogIterator =
             db.getUpdatesSince(0)) {
      //no-op
    }
  }

  @Test
  public void getBatch() throws RocksDBException {
    final int numberOfPuts = 5;
    try (final Options options = new Options()
        .setCreateIfMissing(true)
        .setWalTtlSeconds(1000)
        .setWalSizeLimitMB(10);
         final RocksDB db = RocksDB.open(options,
             dbFolder.getAbsolutePath())) {

      for (int i = 0; i < numberOfPuts; i++) {
        db.put(String.valueOf(i).getBytes(),
            String.valueOf(i).getBytes());
      }
      db.flush(new FlushOptions().setWaitForFlush(true));

      // the latest sequence number is 5 because 5 puts
      // were written beforehand
      assertThat(db.getLatestSequenceNumber()).
          isEqualTo(numberOfPuts);

      // insert 5 writes into a cf
      try (final ColumnFamilyHandle cfHandle = db.createColumnFamily(
          new ColumnFamilyDescriptor("new_cf".getBytes()))) {
        for (int i = 0; i < numberOfPuts; i++) {
          db.put(cfHandle, String.valueOf(i).getBytes(),
              String.valueOf(i).getBytes());
        }
        // the latest sequence number is 10 because
        // (5 + 5) puts were written beforehand
        assertThat(db.getLatestSequenceNumber()).
            isEqualTo(numberOfPuts + numberOfPuts);

        // Get updates since the beginning
        try (final TransactionLogIterator transactionLogIterator =
                 db.getUpdatesSince(0)) {
          assertThat(transactionLogIterator.isValid()).isTrue();
          transactionLogIterator.status();

          // The first sequence number is 1
          final TransactionLogIterator.BatchResult batchResult =
              transactionLogIterator.getBatch();
          assertThat(batchResult.sequenceNumber()).isEqualTo(1);
        }
      }
    }
  }

  @Test
  public void transactionLogIteratorStallAtLastRecord()
      throws RocksDBException {
    try (final Options options = new Options()
        .setCreateIfMissing(true)
        .setWalTtlSeconds(1000)
        .setWalSizeLimitMB(10);
         final RocksDB db = RocksDB.open(options,
             dbFolder.getAbsolutePath())) {

      db.put("key1".getBytes(), "value1".getBytes());
      // Get updates since the beginning
      try (final TransactionLogIterator transactionLogIterator =
               db.getUpdatesSince(0)) {
        transactionLogIterator.status();
        assertThat(transactionLogIterator.isValid()).isTrue();
        transactionLogIterator.next();
        assertThat(transactionLogIterator.isValid()).isFalse();
        transactionLogIterator.status();
        db.put("key2".getBytes(), "value2".getBytes());
        transactionLogIterator.next();
        transactionLogIterator.status();
        assertThat(transactionLogIterator.isValid()).isTrue();
      }
    }
  }

  @Test
  public void transactionLogIteratorCheckAfterRestart()
      throws RocksDBException {
    final int numberOfKeys = 2;
    try (final Options options = new Options()
        .setCreateIfMissing(true)
        .setWalTtlSeconds(1000)
        .setWalSizeLimitMB(10)) {

      try (final RocksDB db = RocksDB.open(options,
          dbFolder.getAbsolutePath())) {
        db.put("key1".getBytes(), "value1".getBytes());
        db.put("key2".getBytes(), "value2".getBytes());
        db.flush(new FlushOptions().setWaitForFlush(true));

      }

      // reopen
      try (final RocksDB db = RocksDB.open(options,
          dbFolder.getAbsolutePath())) {
        assertThat(db.getLatestSequenceNumber()).isEqualTo(numberOfKeys);

        try (final TransactionLogIterator transactionLogIterator =
                 db.getUpdatesSince(0)) {
          for (int i = 0; i < numberOfKeys; i++) {
            transactionLogIterator.status();
            assertThat(transactionLogIterator.isValid()).isTrue();
            transactionLogIterator.next();
          }
        }
      }
    }
  }
}
