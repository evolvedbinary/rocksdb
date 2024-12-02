package org.rocksdb;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class IteratorClosedDBTest {
  @ClassRule
  public static final RocksNativeLibraryResource ROCKS_NATIVE_LIBRARY_RESOURCE =
      new RocksNativeLibraryResource();

  @Rule public TemporaryFolder dbFolder = new TemporaryFolder();

  @Test
  public void ownedIterators() throws RocksDBException {
    try (Options options = new Options().setCreateIfMissing(true);
         RocksDB db = RocksDB.open(options, dbFolder.getRoot().getAbsolutePath())) {
      byte[] key = {0x1};
      byte[] value = {0x2};
      db.put(key, value);

      try (RocksIterator it = db.newIterator()) {
        it.seekToFirst();
        assertThat(it.key()).isEqualTo(key);
        assertThat(it.value()).isEqualTo(value);

        it.next();
        assertThat(it.isValid()).isFalse();
      }
    } // if iterator were still open when we close the DB, we would see a C++ assertion in
      // DEBUG_LEVEL=1
  }

  @Test
  public void iteratorStaysOpen() throws RocksDBException, InterruptedException {
    try (Options options = new Options().setCreateIfMissing(true)) {
      RocksDB db = RocksDB.open(options, dbFolder.getRoot().getAbsolutePath());
      byte[] key = {0x1};
      byte[] value = {0x11};
      db.put(key, value);
      byte[] key2 = {0x2};
      byte[] value2 = {0x22};
      db.put(key2, value2);

      RocksIterator it = db.newIterator();
      assertThat(it.isValid()).isFalse();
      it.seekToFirst();
      assertThat(it.isValid()).isTrue();

      byte[] valueOK = it.value();
      assertThat(valueOK).isEqualTo(value);

      // Now we attempt to close the database, and it blocks because the iterator is open
      Thread closeThread = new Thread(() -> db.close());
      closeThread.start();
      Thread.sleep(0);

      it.next();
      assertThat(it.isValid()).isTrue();
      valueOK = it.value();
      assertThat(valueOK).isEqualTo(value2);

      // But we cannot create a new iterator
      // Because we have asked to close
      try {
        RocksIterator it2 = db.newIterator();
      } catch (RuntimeException re) {
        assertThat(re).hasCauseInstanceOf(RocksDBException.class);
        assertThat(re).hasMessageContaining("Could not create iterator");
      }

      // Now let the DB close
      it.close();
      closeThread.join();

      try {
        byte[] valueShouldAssert = it.value();
        throw new RuntimeException("it.value() should cause an assertion");
      } catch (AssertionError ignored) {
      }

      // should assert
      try {
        boolean isValidShouldAssert = it.isValid();
        throw new RuntimeException("it.isValid() should cause an assertion");
      } catch (AssertionError ignored) {
      }

      // Multiple close() should be fine/no-op
      it.close();
    }
  }
}
