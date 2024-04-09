package org.rocksdb;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyTest {


    @Rule
    public TemporaryFolder dbFolder = new TemporaryFolder();

    @Test
    public void testGihubIssue() throws Exception {
        final int minBlobSize = 1000;
        final byte[] messageKey = "some_key".getBytes(StandardCharsets.UTF_8);
        final byte[] message = getData(minBlobSize + 1000).getBytes(StandardCharsets.UTF_8);

        final Path mainPath = Paths.get(dbFolder.getRoot().getAbsolutePath());
        final Path checkpointPath = mainPath.resolve("checkpoint");

        try (Options options = new Options().setCreateIfMissing(true).setEnableBlobFiles(true).setMinBlobSize(minBlobSize)) {
            System.out.println(mainPath);

            try (RocksDB rocks = RocksDB.open(options, mainPath.toString())) {
                rocks.put(messageKey, message);
                try (Checkpoint checkpoint = Checkpoint.create(rocks)) {
                    checkpoint.createCheckpoint(checkpointPath.toString());
                }
            }

            try (RocksDB rocks = RocksDB.open(options, checkpointPath.toString())) {
                byte[] read = rocks.get(messageKey);
                System.out.println("read with RockDB.open on checkpoint: " + (read != null));

            }

            try (RocksDB rocks = RocksDB.openReadOnly(options, checkpointPath.toString())) {
                byte[] read = rocks.get(messageKey);
                System.out.println("read with RockDB.openReadOnly on checkpoint: " + (read != null));
            }


            try (RocksDB rocks = RocksDB.open(options, mainPath.toString())) {
                byte[] read = rocks.get(messageKey);
                System.out.println("read with RockDB.open on main: " + (read != null));
            }


            try (RocksDB rocks = RocksDB.openReadOnly(options, mainPath.toString())) {
                byte[] read = rocks.get(messageKey);
                System.out.println("read with RockDB.openReadOnly on main: " + (read != null));
            }

            try (RocksDB rocks = RocksDB.openReadOnly(options, checkpointPath.toString());
                 RocksIterator it = rocks.newIterator()) {
                it.seekToFirst();
                System.out.println("It isValid : " + it.isValid());
                byte[] keyFromIt = it.key();
                byte[] valueFromIt = it.value();
                System.out.println("Key from it : " + (keyFromIt != null));
                System.out.println("value from it : " + (valueFromIt != null));
                System.out.println("key value from it: " + new String(keyFromIt));
                System.out.println("value value from it: " + new String(valueFromIt));
            }

        }
    }


    public String getData(int size) {
        StringBuilder data = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            data.append('a');
        }
        return data.toString();
    }





}
