package org.rocksdb;

import java.util.HashSet;
import java.util.Set;

/**
 * Support class that records a collection of open items
 * which are owned by some other item,
 * and which have a lifecycle nested within that of the owner.
 * This blocks the owner from closing until the members are closed.
 * <p>
 *     For example, a {@code RocksDB} should not close until all {@code RocksIterator}s are closed.
 *     To achieve this, the {@code RocksDB} contains a {@code OpenItems<RocksIterator>} which receives
 *     new iterators as they are opened, and from which the iterators are removed as they are closed.
 *     The {@code #close()} call will block until the {@code OpenItems} set is empty.
 * </p>
 * @param <T>
 */
public class OpenItems<T> {

    private final Set<T> openItems = new HashSet<>();
    private boolean isClosed = false;

    /**
     * Record a new open item in the collection
     * @param item the item
     * @throws RocksDBException if the collection is already closed
     */
    synchronized void add(T item) throws RocksDBException {
        if (isClosed) {
            throw new RocksDBException("Cannot add item " + item + " to open items after close() has been called");
        }
        openItems.add(item);
    }

    /**
     * Record that a member item is closed, by removing it from the collection.
     * It is not an error here to remove() an item which is not in the collection.
     *
     * @param item the item that is closed
     */
    synchronized void remove(T item) {
        openItems.remove(item);
        notify();
    }

    /**
     * Close the collection. Blocks until all items have been removed.
     *
     * @throws InterruptedException if the thread is waiting to be notified
     * of a change of state on the collection, and is interrupted.
     */
    synchronized void close() throws InterruptedException {
        isClosed = true;
        while (!openItems.isEmpty()) {
            wait();
        }
    }
}
