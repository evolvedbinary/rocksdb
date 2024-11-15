package org.rocksdb;

import org.rocksdb.util.Varint32;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.rocksdb.WriteBatch.flushWriteBatchBuffer;

class WriteBatchBuffer implements Closeable {

    private ByteBuffer buffer;
    private int entryCount;

    public WriteBatchBuffer(final int reservedBytes) {
        buffer = ByteBuffer.allocate(reservedBytes).order(ByteOrder.LITTLE_ENDIAN);
        resetBuffer();
    }

    private void putVarint32(final int value) {
        Varint32.write(buffer, value);
    }

    private void putEntry(final byte[] array) {
        putVarint32(array.length);
        buffer.put(array);
    }

    private boolean bufferAvailable(int requiredSpace) {
        return buffer.remaining() >= requiredSpace;
    }

    void put(final byte[] key, final int keyLen, final byte[] value, final int valueLen) {
        assert key.length == keyLen;
        assert value.length == valueLen;
        final int requiredSpace = 1 // kTypeValue
            + Varint32.numBytes(key.length) + key.length + Varint32.numBytes(value.length)
            + value.length;
        if (bufferAvailable(requiredSpace)) {
            entryCount++;
            buffer.put((byte) WriteBatchInternal.ValueType.kTypeValue.ordinal());
            putEntry(key);
            putEntry(value);
        } else {
            throw new RuntimeException(new RocksDBException("put() (" + keyLen + ", " + valueLen +")" +
                " insufficient space remaining in WriteBatchBuffer [" + requiredSpace + "]"));
        }
    }

    void setSequence(final long sequence) {
        buffer.putLong(WriteBatchInternal.kSequenceOffset, sequence);
    }

    void flush() throws RocksDBException {
        assert nativeHandle == null : "WriteBatchBuffer can only flush once.";
        if (entryCount > 0) {
            buffer.putInt(WriteBatchInternal.kCountOffset, entryCount);

            buffer.flip();
            final long newHandle = flushWriteBatchBuffer(
                buffer.capacity(), buffer.array(), buffer.limit());
            resetBuffer();

            nativeHandle = new NativeHandle(newHandle);
        }
    }

    private void resetBuffer() {
        buffer.clear();
        buffer.putLong(0L);
        buffer.putInt(0);

        entryCount = 0;
    }

    private NativeHandle nativeHandle = null;

    public long writeBatchHandle() {
        if (nativeHandle != null) {
            return nativeHandle.getNativeHandle();
        }
        return 0L;
    }

    @Override
    public void close() {
        if (nativeHandle != null) {
            nativeHandle.close();
        }
    }

    class NativeHandle extends RocksObject {

        protected NativeHandle(long nativeHandle) {
            super(nativeHandle);
        }

        @Override
        protected void disposeInternal(long handle) {
            WriteBatch.disposeInternalJni(handle);
        }
    }
}
