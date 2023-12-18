// Copyright (c) 2011-present, Facebook, Inc.  All rights reserved.
//  This source code is licensed under both the GPLv2 (found in the
//  COPYING file in the root directory) and Apache 2.0 License
//  (found in the LICENSE.Apache file in the root directory).

package org.rocksdb;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.rocksdb.util.SizeUnit.MB;


@RunWith(Parameterized.class)
public class TTLExpiryTest {

    @ClassRule
    public static final RocksNativeLibraryResource ROCKS_NATIVE_LIBRARY_RESOURCE =
            new RocksNativeLibraryResource();

    @Rule
    public TemporaryFolder dbFolder = new TemporaryFolder();

    @Parameterized.Parameters
    public static Object[][]  data() {
        return new Object[10][0];
    }

    @Test
    public void rocksDB() throws RocksDBException {
        final int TTL_SECONDS = 1;
        final int REPEATS = 3;
        final int BATCH = 5;
        System.out.println("Directory : " + dbFolder.getRoot().getAbsolutePath());


        try (final Options options = new Options()) {

            options.setCreateIfMissing(true);
            options.setEnv(Env.getDefault());

            //fifo related
            CompactionOptionsFIFO compactionOptionsFIFO = new CompactionOptionsFIFO();
            compactionOptionsFIFO.setAllowCompaction(true);
            compactionOptionsFIFO.setMaxTableFilesSize(5 * MB);
            options.setCompactionOptionsFIFO(compactionOptionsFIFO);

            options.setTtl(TTL_SECONDS);
            options.setPeriodicCompactionSeconds(TTL_SECONDS);
            options.setMaxBackgroundJobs(4);
            options.setMaxOpenFiles(-1);


            try (final RocksDB db = RocksDB.open(options, dbFolder.getRoot().getAbsolutePath())) {

                db.put("pMidKey".getBytes(UTF_8), "pMidValue".getBytes(UTF_8));

                Thread.sleep(TimeUnit.SECONDS.toMillis(TTL_SECONDS + 1));

                try(FlushOptions fOptions = new FlushOptions()) {
                    db.flush(fOptions);
                }

                for (int i = 0; i < REPEATS; i++) {
                    byte key[] = db.get("pMidKey".getBytes(UTF_8));
                    System.out.println("iteration : " + i + " key present : " + (key != null));

                    assertThat((key != null && i < 2) ^ (key != null && i == 2)).isTrue();
                    if(key == null) {
                        return;
                    }

                    WriteBatch wb = new WriteBatch();
                    for (int j = 0; j < BATCH; j++) {
                        wb.put(("p"+j).getBytes(UTF_8), ("begin"+j).getBytes(UTF_8));
                        wb.put(("q"+j).getBytes(UTF_8), ("end"+j).getBytes(UTF_8));
                    }
                    try(WriteOptions writeOptions = new WriteOptions()) {
                        db.write(writeOptions, wb);
                    }
                    try(FlushOptions fOptions = new FlushOptions()) {
                        db.flush(fOptions);
                    }

                    Thread.sleep(TimeUnit.SECONDS.toMillis(TTL_SECONDS + 1));
                }

            } catch (InterruptedException ie) {
                fail("interrupted", ie);
            }
        }
    }


    @Test
    public void ttlDB() throws RocksDBException {
        final int TTL_SECONDS = 1;
        final int REPEATS = 3;
        final int BATCH = 5;
        System.out.println("Directory : " + dbFolder.getRoot().getAbsolutePath());


        try (final Options options = new Options()) {

            options.setCreateIfMissing(true);
            options.setEnv(Env.getDefault());

            //fifo related
            CompactionOptionsFIFO compactionOptionsFIFO = new CompactionOptionsFIFO();
            compactionOptionsFIFO.setAllowCompaction(true);
            compactionOptionsFIFO.setMaxTableFilesSize(5 * MB);
            options.setCompactionOptionsFIFO(compactionOptionsFIFO);

            options.setTtl(TTL_SECONDS);
            options.setPeriodicCompactionSeconds(TTL_SECONDS);
            options.setMaxBackgroundJobs(4);
            options.setMaxOpenFiles(-1);

            try (final RocksDB db = TtlDB.open(options, dbFolder.getRoot().getAbsolutePath(), TTL_SECONDS, false)) {


                db.put("pMidKey".getBytes(UTF_8), "pMidValue".getBytes(UTF_8));

                Thread.sleep(TimeUnit.SECONDS.toMillis(TTL_SECONDS + 1));

                try(FlushOptions fOptions = new FlushOptions()) {
                    db.flush(fOptions);
                }

                for (int i = 0; i < REPEATS; i++) {
                    byte key[] = db.get("pMidKey".getBytes(UTF_8));
                    System.out.println("iteration : " + i + " key present : " + (key != null));

                    assertThat((key != null && i < 2) ^ (key == null && i == 2)).isTrue();
                    if(key == null) {
                        return;
                    }

                    WriteBatch wb = new WriteBatch();
                    for (int j = 0; j < BATCH; j++) {
                        wb.put(("p"+j).getBytes(UTF_8), ("begin"+j).getBytes(UTF_8));
                        wb.put(("q"+j).getBytes(UTF_8), ("end"+j).getBytes(UTF_8));
                    }
                    try(WriteOptions writeOptions = new WriteOptions()) {
                        db.write(writeOptions, wb);
                    }
                    try(FlushOptions fOptions = new FlushOptions()) {
                        db.flush(fOptions);
                    }

                    Thread.sleep(TimeUnit.SECONDS.toMillis(TTL_SECONDS + 1));
                }

            } catch (InterruptedException ie) {
                fail("interrupted", ie);
            }
        }
    }
}