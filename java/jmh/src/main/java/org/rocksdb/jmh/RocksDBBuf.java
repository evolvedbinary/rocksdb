package org.rocksdb.jmh;

import io.netty.buffer.ByteBuf;
import org.rocksdb.*;

public class RocksDBBuf extends RocksDBAddr {
    
    public RocksDBBuf(final RocksDB db) {
        super(db);
    }

    public void put(final ColumnFamilyHandle columnFamilyHandle, final WriteOptions writeOpts,
                       final ByteBuf key, final ByteBuf value) throws RocksDBException {
        putAddr(columnFamilyHandle, writeOpts, key.memoryAddress(), key.readableBytes(), value.memoryAddress(), value.readableBytes());
    }

    protected void put(final ColumnFamilyHandle columnFamilyHandle, final ByteBuf key, final ByteBuf value) throws RocksDBException {
        putAddr(columnFamilyHandle, key.memoryAddress(), key.readableBytes(), value.memoryAddress(), value.readableBytes());
    }

    protected void put(final WriteOptions writeOpts, final ByteBuf key, final ByteBuf value) throws RocksDBException {
        putAddr(writeOpts, key.memoryAddress(), key.readableBytes(), value.memoryAddress(), value.readableBytes());
    }
}
