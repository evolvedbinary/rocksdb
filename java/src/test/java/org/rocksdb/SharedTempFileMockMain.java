package org.rocksdb;

import org.rocksdb.util.SharedTempFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rocksdb.LeakedSharedObjectTest.compare;

public class SharedTempFileMockMain {


    public static void main(final String[] args) throws IOException, InterruptedException {

        final Random random = new Random();
        try {
            Thread.sleep(random.nextInt(100));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        SharedTempFile.Instance instance = new SharedTempFile.Instance("rocksdbmock", "jnilib");
        final List<SharedTempFile> existing = instance.search();
        SharedTempFile sharedTemp;
        if (existing.isEmpty()) {
            sharedTemp = instance.create();
        } else {
            sharedTemp = existing.get(0);
        }
        System.err.println(sharedTemp);
        Path content;
        try (SharedTempFile.Lock ignored = sharedTemp.lock(LeakedSharedObjectTest::mockContent)) {
            content = sharedTemp.getContent();
            assertThat(Files.exists(content)).isTrue();
            try (BufferedReader shared = new BufferedReader(new InputStreamReader(Files.newInputStream(content))); BufferedReader resource = LeakedSharedObjectTest.mockContentReader()) {
                compare(resource, shared);
            }
        }
        assertThat(Files.exists(content)).isFalse();

        Thread.sleep(2000);
    }

}
