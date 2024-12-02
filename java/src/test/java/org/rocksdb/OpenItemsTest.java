package org.rocksdb;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


public class OpenItemsTest {

    @Test
    public void addAndRemove() throws RocksDBException, InterruptedException {
        OpenItems<String> items = new OpenItems<>();
        items.add("one");
        items.add("two");
        items.remove("two");
        items.remove("three");
        items.remove("one");
        items.close();
        try {
            items.add("four");
            fail("Add item after close() should throw an exception");
        } catch (RocksDBException e) {
            //expected
        }
    }

    @Test
    public void blockingItem() throws RocksDBException, InterruptedException {
        AtomicLong after = new AtomicLong();
        OpenItems<String> items = new OpenItems<>();
        items.add("one");

        // start a thread that closes, but is blocked by "one"
        Thread thread = new Thread(() -> {
            try {
                items.close();
                after.set(System.currentTimeMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        Thread.sleep(100);
        long before = System.currentTimeMillis();
        items.remove("one");
        Thread.sleep(100);
        thread.join();

        // checks that close() succeeded after remove("one")
        assertThat(before).isLessThanOrEqualTo(after.get());
    }
}
