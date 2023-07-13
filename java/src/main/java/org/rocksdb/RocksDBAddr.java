package org.rocksdb;

public class RocksDBAddr {

    private final RocksDB db;

    protected RocksDBAddr(final RocksDB db) {
        this.db = db;
    }

    protected void putAddr(final ColumnFamilyHandle columnFamilyHandle, final WriteOptions writeOpts,
                       final long keyAddr, final int keyLen, final long valueAddr, final int valueLen) throws RocksDBException {
        db.putAddr(columnFamilyHandle, writeOpts, keyAddr, keyLen, valueAddr, valueLen);
    }

    protected void putAddr(final ColumnFamilyHandle columnFamilyHandle, final long keyAddr, final int keyLen,
                           final long valueAddr, final int valueLen) throws RocksDBException {
        db.putAddr(columnFamilyHandle, keyAddr, keyLen, valueAddr, valueLen);
    }

    protected void putAddr(final WriteOptions writeOpts, final long keyAddr, final int keyLen,
                       final long valueAddr, final int valueLen) throws RocksDBException {
        db.putAddr(writeOpts, keyAddr, keyLen, valueAddr, valueLen);
    }
}
