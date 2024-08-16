package org.rocksdb;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class LongWriteTest {

    @Rule
    public TemporaryFolder dbFolder = new TemporaryFolder();

    private final Random r = new Random(1004235235l);

    private static final char[] characters = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '@','{','!'
    };

    public String getRandomString() {
        int len = 4 + r.nextInt(15);
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = characters[r.nextInt(characters.length)];
        }
        return new String(chars);
    }

    @Test
    public void wiriteByteArray() throws RocksDBException {
        System.out.println("DB dir : " + dbFolder.getRoot().getAbsolutePath());
        try(RocksDB db = RocksDB.open(dbFolder.getRoot().getAbsolutePath());
            ColumnFamilyHandle cf = db.getDefaultColumnFamily()
        ) {
            for (long i = 0; i < 3_000_000; i++) {
                String prefix = getRandomString();
                for (int j = 0; j < 100; j++) {
                    String key = prefix + "." + getRandomString();
                    String value = getRandomString();
                    db.put(cf, key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8));
                }
                if((i % 10_000) == 0) {
                    System.out.println(Long.toString(i) + "   prefix : " + prefix   );
                }
            }

        }
        System.out.println("End");
    }


    @Test
    public void writeDataByteBuffer() throws RocksDBException {
        System.out.println("DB dir for byte Buffer : " + dbFolder.getRoot().getAbsolutePath());
        ByteBuffer key = ByteBuffer.allocateDirect(1024);
        ByteBuffer value = ByteBuffer.allocateDirect(1024);
        long time = System.currentTimeMillis() - 5000;
        try(RocksDB db = RocksDB.open(dbFolder.getRoot().getAbsolutePath());
        WriteOptions writeOptions = new WriteOptions().setDisableWAL(true)) {
            for (long i = 0; i < 3_000_000; i++) {
                String prefix = getRandomString();
                key.clear();
                key.put(prefix.getBytes(StandardCharsets.UTF_8));
                int position = key.position();

                for (int j = 0; j < 100; j++) {
                    key.limit(key.capacity());
                    key.position(position);
                    key.put(getRandomString().getBytes(StandardCharsets.UTF_8)).flip();

                    value.clear();
                    value.put(getRandomString().getBytes(StandardCharsets.UTF_8));
                    value.flip();
                    db.put(writeOptions, key, value);
                }
                if((i % 10_000) == 0) {
                    long duration = System.currentTimeMillis() - time;
                    time = System.currentTimeMillis();
                    System.out.println(Long.toString(i) + " op/s : "  + (10_000 * 100) / ((double)duration / 1000)   + " prefix : " + prefix );
                }
            }
        }
        System.out.println("End");
    }


    @BeforeClass
    public static void before() throws RocksDBException {
        RocksDB.loadLibrary();
    }
}